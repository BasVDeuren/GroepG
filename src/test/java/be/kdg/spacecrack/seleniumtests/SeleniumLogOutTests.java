package be.kdg.spacecrack.seleniumtests;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertEquals;

/**
 * Created by Arno on 18/02/14.
 */
public class SeleniumLogOutTests extends SeleniumBaseTestCase {

    @Test
    public void LogOutOK() throws InterruptedException {
        login();

        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement btnAction = driver.findElement(By.name("btnAction"));
        wait.until(ExpectedConditions.visibilityOf(btnAction));
        btnAction.click();
        WebElement btnLogout = driver.findElement(By.name("btnLogout"));
        wait.until(ExpectedConditions.visibilityOf(btnLogout));
        btnLogout.click();
        Thread.sleep(1000);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("login")));

        assertEquals(baseUrl +"login", driver.getCurrentUrl());
        WebElement uname = driver.findElement(By.name("uname"));
        wait.until(ExpectedConditions.visibilityOf(uname));

        assertEquals(false, btnAction.isDisplayed());
    }
}
