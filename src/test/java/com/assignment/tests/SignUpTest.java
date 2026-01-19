
package com.assignment.tests;

import com.assignment.pages.SignUpPage;
import com.assignment.utils.TestReportListener;            // <-- Added (Step 2)

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.testng.Assert;
import org.testng.annotations.*;

import org.apache.logging.log4j.LogManager;               // <-- Added (Step 2)
import org.apache.logging.log4j.Logger;                  // <-- Added (Step 2)
import org.testng.annotations.Listeners;                 // <-- Added (Step 2)

@Listeners(TestReportListener.class)                     // <-- Added (Step 2)
public class SignUpTest {

    public WebDriver driver;                              // <-- Made public (Step 2)
    private static final Logger log = LogManager.getLogger(SignUpTest.class); // <-- Added (Step 2)
    private SignUpPage signUp;

    @BeforeClass
    public void setup() {
        log.info("Setting up ChromeDriver (WebDriverManager) and options...");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Faster page load – don't wait for subresources
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        // Optional: enable headless by -Dheadless=true
        if ("true".equalsIgnoreCase(System.getProperty("headless", "false"))) {
            log.info("Running in headless mode");
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }

        options.addArguments("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");

        log.info("Launching Chrome...");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        // Initialize the Page Object
        log.info("Initializing SignUpPage");
        signUp = new SignUpPage(driver);
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        log.info("Closing browser...");
        if (driver != null) driver.quit();
    }

    @Test
    public void testSignUp() {
        log.info("Starting SignUp test");

        // Step 1: Open page
        log.info("Opening page");
        signUp.openPage();

        // Step 2: Validate languages
        log.info("Validating languages");
        Assert.assertTrue(signUp.validateLanguages(), "Languages not present.");
        log.info("Selecting language: English");
        signUp.selectLanguage("English");

        // Step 3: Fill details
        String name = "Sarvesh Kumar Ojha";
        log.info("Filling details: Name/Org/Email");
        signUp.fillName(name);
        signUp.fillOrg(name);
        signUp.fillEmail("sarvesh" + System.currentTimeMillis() + "@test.com");

        // Step 4: Accept terms
        log.info("Accepting terms");
        signUp.acceptTerms();

        // Step 5: Submit form
        log.info("Submitting form");
        signUp.clickSignUp();

        // Step 6: Validate confirmation message
        log.info("Verifying confirmation message");
        Assert.assertTrue(signUp.verifyConfirmation(), "Confirmation message not found");

        log.info("Test completed successfully");
    }
}
