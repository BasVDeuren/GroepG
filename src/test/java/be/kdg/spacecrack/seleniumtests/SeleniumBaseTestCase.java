package be.kdg.spacecrack.seleniumtests;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.concurrent.TimeUnit;
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml", "file:src/main/resources/application-context.xml"})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public abstract class SeleniumBaseTestCase {
    @Autowired
    protected SessionFactory sessionFactory;
    protected WebDriver driver;
    protected String baseUrl="http://localhost:8080/#/";

    @Before
    public void setUp() throws Exception {
        URL resource = this.getClass().getResource("/chromedriver.exe");
        String path = resource.getPath();
        path = path.replaceAll("%20", " ");
        System.setProperty("webdriver.chrome.driver", path);
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    }
    protected void login() {
        driver.get(baseUrl);
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement uname = driver.findElement(By.name("uname"));
        WebElement password = driver.findElement(By.name("password"));
        uname.sendKeys("test@gmail.com");
        password.sendKeys("test");
        WebElement loginbutton = driver.findElement(By.name("login"));
        loginbutton.click();

//        uname = driver.findElement(By.name("uname"));
//        password = driver.findElement(By.name("password"));
//        uname.sendKeys("test@gmail.com");
//        password.sendKeys("test");
//        loginbutton = driver.findElement(By.name("login"));
//        loginbutton.click();

        wait.until(ExpectedConditions.elementToBeClickable(By.name("createGame")));
    }

    protected void navigateToEditProfile() {
        driver.get(baseUrl);
        WebElement btnAction = driver.findElement(By.name("btnAction"));
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.name("btnAction")));
        btnAction.click();
        WebElement lnkEditProfile = driver.findElement(By.name("lnkEditProfile"));
        lnkEditProfile.click();
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
    }
}
