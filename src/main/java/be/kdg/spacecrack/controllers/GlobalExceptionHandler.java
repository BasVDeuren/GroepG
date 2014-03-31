package be.kdg.spacecrack.controllers;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.Exceptions.SpaceCrackUnexpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.xml.bind.ValidationException;

@ControllerAdvice
public class GlobalExceptionHandler {
    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public String handleMethodArgumentNotValidException(MethodArgumentNotValidException e) throws Exception {
        return e.getMessage();
    }

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e) throws Exception {
        if(e instanceof ValidationException)
        {
            throw new SpaceCrackNotAcceptableException("Constraint violated");
        }
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null){
            throw e;
        }
        logger.error("Unexpected exception occurred: " + e.toString());
        for(StackTraceElement ste : e.getStackTrace())
        {
            logger.error(ste.toString());
        }
        throw new SpaceCrackUnexpectedException("Something bad happened");
       // return "Something bad happened";
    }
}
