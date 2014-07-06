package be.kdg.spacecrack.seleniumtests;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertEquals;

public class SeleniumEditProfileTests extends SeleniumBaseTestCase {
    @Test
    public void navigateToEditProfile_loggedIn_ok() throws Exception {
        login();
        WebDriverWait wait = new WebDriverWait(driver,10);

        WebElement btnAction = driver.findElement(By.name("btnAction"));
        wait.until(ExpectedConditions.visibilityOf(btnAction));
        btnAction.click();
        WebElement lnkEditProfile = driver.findElement(By.name("lnkEditProfile"));
        lnkEditProfile.click();

        WebElement firstNameTextBox = driver.findElement(By.id("firstname"));
        wait.until(ExpectedConditions.visibilityOf(firstNameTextBox));

        assertEquals(baseUrl+ "spacecrack/editProfile", driver.getCurrentUrl());

        WebElement lnkpssword = driver.findElement(By.linkText("Change password"));
        lnkpssword.click();

        WebElement passwordTextBox = driver.findElement(By.id("new_password"));
        wait.until(ExpectedConditions.visibilityOf(passwordTextBox));
        assertEquals(baseUrl +"spacecrack/editProfile", driver.getCurrentUrl());

        WebElement lnkProfile = driver.findElement(By.linkText("Profile"));
        lnkProfile.click();

        firstNameTextBox = driver.findElement(By.id("firstname"));
        wait.until(ExpectedConditions.visibilityOf(firstNameTextBox));

        assertEquals(baseUrl +"spacecrack/editProfile", driver.getCurrentUrl());
    }

    @Test
    public void ChangePassword_Oké() throws Exception {
        login();
        navigateToEditProfile();

        WebElement lnkpssword = driver.findElement(By.linkText("Change password"));
        lnkpssword.click();

        WebDriverWait wait = new WebDriverWait(driver,10);
        WebElement passwordTextBox = driver.findElement(By.id("new_password"));
        wait.until(ExpectedConditions.visibilityOf(passwordTextBox));
        WebElement password2TextBox = driver.findElement(By.id("password2"));

        passwordTextBox.sendKeys("test");
        password2TextBox.sendKeys("test");
        WebElement btnSave = driver.findElement(By.id("save"));
        btnSave.click();

        WebElement profileSuccesMsg = driver.findElement(By.id("profileSuccesMsg"));

        wait.until(ExpectedConditions.visibilityOf(profileSuccesMsg));

        assertEquals(true,profileSuccesMsg.isDisplayed());
    }
    @Test
    public void ChangePassword_Fail() throws Exception {
        login();
        navigateToEditProfile();

        WebElement lnkpssword = driver.findElement(By.linkText("Change password"));
        lnkpssword.click();

        WebDriverWait wait = new WebDriverWait(driver,10);
        WebElement passwordTextBox = driver.findElement(By.id("new_password"));
        wait.until(ExpectedConditions.visibilityOf(passwordTextBox));
        WebElement password2TextBox = driver.findElement(By.id("password2"));

        passwordTextBox.sendKeys("gelukt");
        password2TextBox.sendKeys("nietgelukt");

        WebElement btnSave = driver.findElement(By.id("save"));

        assertEquals(false,btnSave.isEnabled());
    }

    public void fillProfileFields() throws InterruptedException {

        WebElement firstname = driver.findElement(By.id("firstname"));
        firstname.sendKeys(Keys.CONTROL + "a");
        firstname.sendKeys(Keys.DELETE);
        firstname.sendKeys("Arno");

        WebElement lastName = driver.findElement(By.id("lastName"));
        lastName.sendKeys(Keys.CONTROL + "a");
        lastName.sendKeys(Keys.DELETE);
        lastName.sendKeys("Flerie");

        WebElement lnkProfile = driver.findElement(By.linkText("Profile"));
        lnkProfile.click();
        Thread.sleep(500);
        lnkProfile.click();
    }
}
