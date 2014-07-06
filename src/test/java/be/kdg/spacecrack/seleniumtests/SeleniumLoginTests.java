package be.kdg.spacecrack.seleniumtests;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertEquals;

public class SeleniumLoginTests extends SeleniumBaseTestCase {

    @Test
    public void Login_validCredentials_spacecrackhome() throws Exception {
        driver.get(baseUrl);
        WebElement uname = driver.findElement(By.name("uname"));
        WebElement password = driver.findElement(By.name("password"));
        uname.sendKeys("test@gmail.com ");
        password.sendKeys("test");
        WebElement loginbutton = driver.findElement(By.name("login"));
        loginbutton.click();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.name("createGame")));
        assertEquals(baseUrl +"", driver.getCurrentUrl());
    }

    @Test
    public void Login_invalidCredentials_errormessage() throws Exception {
        driver.get(baseUrl);
        WebElement uname = driver.findElement(By.name("uname"));
        WebElement password = driver.findElement(By.name("password"));
        uname.sendKeys("invalidUser");
        password.sendKeys("invalidPassword");
        WebElement loginbutton = driver.findElement(By.name("login"));
        loginbutton.click();
        WebDriverWait wait = new WebDriverWait(driver,10);
        WebElement loginfailedDiv = driver.findElement(By.name("loginfailed"));
        wait.until(ExpectedConditions.visibilityOf(loginfailedDiv));
        assertEquals(baseUrl +"login", driver.getCurrentUrl());
    }
}
