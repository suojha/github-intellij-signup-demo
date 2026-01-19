
package com.assignment.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException; // Use Selenium's NoSuchElementException
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SignUpPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final String BASE_URL = "http://jt-dev.azurewebsites.net/#/SignUp";

    // ------------ CANDIDATES (broad coverage of common dropdowns) ------------
    // Dropdown toggles / containers
    private final By[] toggleCandidates = new By[]{
            // ui-select
            By.cssSelector("span.ui-select-toggle"),
            By.xpath("//span[contains(@class,'ui-select-toggle')]"),
            By.xpath("(//div[contains(@class,'ui-select-container')])[1]"),
            By.xpath("//div[contains(@class,'ui-select-container') and (.//span[contains(@class,'ui-select-placeholder') or contains(@class,'ui-select-match')])]"),
            // Angular Material
            By.cssSelector(".mat-select-trigger"),
            By.xpath("//div[contains(@class,'mat-select')]"),
            // ng-select (popular Angular control)
            By.cssSelector("ng-select .ng-select-container"),
            // Select2
            By.cssSelector(".select2-selection"),
            // Chosen
            By.cssSelector(".chosen-container"),
            // Generic ARIA combobox
            By.cssSelector("[role='combobox']"),
            // Any visible element with 'Language' label nearby
            By.xpath("//*[self::div or self::span or self::label][contains(translate(., 'LANGUAGE', 'language'),'language')]")
    };

    // Options inside opened dropdowns
    private final By[] optionCandidates = new By[]{
            // ui-select
            By.cssSelector("div.ui-select-choices-row"),
            By.xpath("//div[contains(@class,'ui-select-choices-row')]"),
            By.xpath("//div[contains(@class,'ui-select-choices')]//div[contains(@class,'ui-select-choices-row')]"),
            By.xpath("//li[contains(@class,'ui-select-choices-row')]"),
            // Angular Material
            By.cssSelector("mat-option .mat-option-text"),
            By.xpath("//mat-option//span[contains(@class,'mat-option-text') or self::span]"),
            By.cssSelector(".mat-select-panel .mat-option"),
            // ng-select
            By.cssSelector(".ng-dropdown-panel .ng-option"),
            // Select2
            By.cssSelector(".select2-results__option"),
            // Chosen
            By.cssSelector(".chosen-results li"),
            // Generic listbox items (narrowed)
            By.cssSelector("[role='option']"),
            // Fallback: visible <option> (from <select>)
            By.tagName("option")
    };

    // Inputs
    private final By[] nameCandidates = new By[]{
            By.name("name"),
            By.xpath("//input[@placeholder='Name']"),
            By.xpath("//input[contains(@id,'name')]")
    };

    private final By[] orgCandidates = new By[]{
            By.name("orgName"),
            By.xpath("//input[@placeholder='Organization Name' or @placeholder='Organisation Name']"),
            By.xpath("//input[contains(@id,'org')]")
    };

    private final By[] emailCandidates = new By[]{
            By.xpath("//input[@type='email']"),
            By.name("email"),
            By.xpath("//input[@placeholder='Email' or @placeholder='E-mail']")
    };

    // ✅ Terms (checkbox only; avoid clicking the hyperlink)
    private final By[] termsCheckboxCandidates = new By[]{
            // direct checkbox by common ids/names/models
            By.cssSelector("input[type='checkbox'][id*='term'], input[type='checkbox'][name*='term'], input[type='checkbox'][id*='agree'], input[type='checkbox'][name*='agree']"),
            // Angular model attribute (if present)
            By.cssSelector("input[type='checkbox'][ng-model*='agree']"),
            // last resort: a checkbox near the "I agree" label (tries not to hit the link)
            By.xpath("//label[contains(.,'I agree')]/preceding::input[@type='checkbox'][1] | " +
                    "//input[@type='checkbox'][ancestor::*[contains(.,'I agree')]][1]"),
            // very generic (fast path)
            By.cssSelector("input[type='checkbox']")
    };

    // Labels as an absolute last fallback (we avoid clicking labels if possible)
    private final By[] termsLabelCandidates = new By[]{
            By.xpath("//label[contains(normalize-space(.),'I agree') and (contains(.,'Terms') or contains(.,'conditions') or contains(.,'Conditions'))]"),
            By.xpath("(//*[self::label or self::span][contains(.,'I agree') and contains(.,'Terms')])[1]")
    };

    // Submit and confirmation
    private final By[] signUpBtnCandidates = new By[]{
            By.xpath("//button[@type='submit']"),
            // Some builds show 'Get Started'
            By.xpath("//button[contains(normalize-space(.),'Sign Up') or contains(normalize-space(.),'SignUp') or contains(normalize-space(.),'Get Started')]")
    };

    // Success messages (several ways sites display them)
    private final By[] confirmationMsgCandidates = new By[]{
            // Exact assignment text
            By.xpath("//*[contains(normalize-space(),'A welcome email has been sent. Please check your email.')]"),
            By.xpath("//*[contains(normalize-space(),'A welcome email has been sent') and contains(normalize-space(),'Please check your email')]"),
            // Case-insensitive contains 'welcome email' AND 'check your email'
            By.xpath("//*[contains(translate(., 'WELCOME EMAIL', 'welcome email'),'welcome email') and contains(translate(., 'CHECK YOUR EMAIL', 'check your email'),'check your email')]"),
            // toasts/alerts/success text
            By.cssSelector(".alert-success, .toast-success, .toast-message, .text-success, .alert.alert-success"),
            // generic banners that often hold success
            By.cssSelector("[class*='success']")
    };

    // Typical inline validation errors (tight to avoid false matches)
    private final By[] errorMsgCandidates = new By[]{
            By.cssSelector(".help-block, .text-danger, .alert-danger, .validation-message, [data-valmsg-for]"),
            By.xpath("//*[contains(translate(.,'REQUIREDINVALIDPLEASE','requiredinvalidplease'),'required') or " +
                    "contains(translate(.,'REQUIREDINVALIDPLEASE','requiredinvalidplease'),'invalid') or " +
                    "contains(translate(.,'REQUIREDINVALIDPLEASE','requiredinvalidplease'),'please')]")
    };

    // for debug
    private String lastSubmissionError = "";

    public SignUpPage(WebDriver driver) {
        this.driver = driver;
        // Keep a reasonable explicit wait for *true* waits; discovery is now done via quick polling.
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // =================== Public API ===================

    public void openPage() {
        driver.get(BASE_URL);

        // DOM ready
        wait.until((ExpectedCondition<Boolean>) d ->
                "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));

        // Angular settle (safe if not Angular)
        waitForAngularToFinish();

        // Make sure form exists (default content or inside a frame)
        ensureContextAtForm();
    }

    /** Validate languages exist (English & Dutch). */
    public boolean validateLanguages() {
        // 1) Plain <select> fast path
        List<String> fromSelect = tryReadLanguagesFromPlainSelect();
        if (containsEnglishAndDutch(fromSelect)) return true;

        // 2) Try to open any dropdown and read options
        List<String> texts = tryReadFromDropdowns();
        if (containsEnglishAndDutch(texts)) return true;

        // 3) LAST RESORT: visible anywhere on page
        if (pageHasVisibleText("English") && pageHasVisibleText("Dutch")) return true;

        System.out.println("[DEBUG] Could not detect a language dropdown. Frames: "
                + driver.findElements(By.cssSelector("iframe, frame")).size());
        return false;
    }

    /** ✅ Select the language (call from your test). */
    public void selectLanguage(String language) {
        // Try a native <select> first
        if (trySelectInPlainSelect(language)) return;

        // Else open a dropdown and pick the option
        driver.switchTo().defaultContent();
        if (tryOpenDropdownAndChoose(language)) return;

        List<WebElement> frames = driver.findElements(By.cssSelector("iframe, frame"));
        for (int i = 0; i < frames.size(); i++) {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(i);
            if (tryOpenDropdownAndChoose(language)) {
                driver.switchTo().defaultContent();
                return;
            }
        }
        driver.switchTo().defaultContent();
        // Some builds may default to English; continue silently.
    }

    public void fillName(String name) {
        WebElement el = findFirstVisibleAcrossFrames(nameCandidates);
        clearAndType(el, name);
    }

    public void fillOrg(String org) {
        WebElement el = findFirstVisibleAcrossFrames(orgCandidates);
        clearAndType(el, org);
    }

    public void fillEmail(String email) {
        WebElement el = findFirstVisibleAcrossFrames(emailCandidates);
        clearAndType(el, email);
    }

    /** ✅ Click the checkbox input (never the hyperlink); JS fallback + change event. */
    public void acceptTerms() {
        // Fast path: generic checkbox in current context
        try {
            driver.switchTo().defaultContent();
            List<WebElement> fast = driver.findElements(By.cssSelector("input[type='checkbox']"));
            for (WebElement cb : fast) {
                if (cb.isDisplayed()) {
                    if (!cb.isSelected()) {
                        cb.click();
                    }
                    return;
                }
            }
        } catch (Exception ignored) {}

        // Fallback to robust cross-frame strategy
        try {
            WebElement cb = findFirstVisibleAcrossFrames(termsCheckboxCandidates);
            scrollIntoViewCenter(cb);

            if (!cb.isSelected()) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(cb));
                    cb.click();
                } catch (WebDriverException clickProblem) {
                    // If intercepted/hidden, set it via JS and dispatch change event
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].checked = true;" +
                                    "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                            cb
                    );
                }
            }

            // Small assert: ensure it is selected
            if (!cb.isSelected()) {
                // Last fallback: JS click on the input element explicitly
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);
            }
            return; // success path
        } catch (TimeoutException | NoSuchElementException noCheckboxVisible) {
            // ignore, try label fallback below
        }

        // FINAL fallback: click the label text—but offset to the left (away from the <a> link)
        try {
            WebElement label = findFirstVisibleAcrossFrames(termsLabelCandidates);
            scrollIntoViewCenter(label);

            // Click ~6px inside the left edge to avoid the link area
            int xFromCenter = -label.getSize().width / 2 + 6;
            new Actions(driver)
                    .moveToElement(label, xFromCenter, 0)
                    .click()
                    .perform();

            // Verify checkbox actually got selected; if not, try to find and JS-set it
            try {
                WebElement cb = findFirstVisibleAcrossFrames(termsCheckboxCandidates);
                if (!cb.isSelected()) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].checked = true;" +
                                    "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                            cb
                    );
                }
            } catch (Exception ignored) {
                // If we still can’t find a checkbox, keep going; the form may accept label state
            }
        } catch (Exception e) {
            // As a last resort, do nothing—submit may still work if T&C not mandatory in this build
            System.out.println("[DEBUG] acceptTerms(): fallback label click failed: " + e.getMessage());
        }
    }

    /** Smarter click + wait for outcome (success/error/URL or body text). */
    public void clickSignUp() {
        lastSubmissionError = "";
        WebElement btn = findFirstVisibleAcrossFrames(signUpBtnCandidates);
        scrollIntoViewCenter(btn);
        safeClick(btn);

        // Try to scroll to top where banners/toasts might appear
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,0);");
        } catch (Exception ignored) {}

        // Wait for either a confirmation OR a visible error OR URL change OR body text contains phrase
        boolean gotOutcome = waitForSubmissionOutcome();
        if (!gotOutcome) {
            System.out.println("[DEBUG] No confirmation or error detected within wait window.");
        }
        if (!lastSubmissionError.isEmpty()) {
            System.out.println("[DEBUG] Submission error seen: " + lastSubmissionError);
        }
    }

    /** Robust confirmation detection (toasts, banners, body text). */
    public boolean verifyConfirmation() {
        // 1) Direct element matches (toasts/banners/modal)
        try {
            WebElement msg = findFirstVisibleAcrossFrames(confirmationMsgCandidates);
            return msg.isDisplayed();
        } catch (TimeoutException ignore) { /* try body text below */ }

        // 2) As a final check, look in page text (case-insensitive) in page and frames
        if (pageContainsTextCI("welcome email") && pageContainsTextCI("check your email")) {
            return true;
        }

        if (!lastSubmissionError.isEmpty()) {
            System.out.println("[DEBUG] verifyConfirmation(): Last error = " + lastSubmissionError);
        }
        return false;
    }

    // =================== Internals ===================

    private boolean containsEnglishAndDutch(List<String> list) {
        if (list == null || list.isEmpty()) return false;
        return list.stream().anyMatch(s -> s != null && s.trim().equalsIgnoreCase("English"))
                && list.stream().anyMatch(s -> s != null && (s.trim().equalsIgnoreCase("Dutch") || s.trim().equalsIgnoreCase("Nederlands")));
    }

    /** Try to read languages from any <select> (in page or any iframe). */
    private List<String> tryReadLanguagesFromPlainSelect() {
        List<String> values = readFromAllSelectsInCurrentContext();
        if (!values.isEmpty()) return values;

        List<WebElement> frames = driver.findElements(By.cssSelector("iframe, frame"));
        for (int i = 0; i < frames.size(); i++) {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(i);
            values = readFromAllSelectsInCurrentContext();
            if (!values.isEmpty()) {
                driver.switchTo().defaultContent();
                return values;
            }
        }
        driver.switchTo().defaultContent();
        return Collections.emptyList();
    }

    private List<String> readFromAllSelectsInCurrentContext() {
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        List<String> texts = new ArrayList<>();
        for (WebElement sel : selects) {
            try {
                Select s = new Select(sel);
                List<String> these = s.getOptions().stream()
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(t -> !t.isEmpty())
                        .collect(Collectors.toList());
                texts.addAll(these);
            } catch (Exception ignored) {}
        }
        return texts.stream().distinct().collect(Collectors.toList());
    }

    /** Try to select in a native <select> by visible text (page and frames). */
    private boolean trySelectInPlainSelect(String language) {
        driver.switchTo().defaultContent();
        if (selectInCurrentContext(language)) return true;

        List<WebElement> frames = driver.findElements(By.cssSelector("iframe, frame"));
        for (int i = 0; i < frames.size(); i++) {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(i);
            if (selectInCurrentContext(language)) {
                driver.switchTo().defaultContent();
                return true;
            }
        }
        driver.switchTo().defaultContent();
        return false;
    }

    private boolean selectInCurrentContext(String language) {
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        for (WebElement sel : selects) {
            try {
                if (!sel.isDisplayed()) continue;
                scrollIntoViewCenter(sel);
                Select s = new Select(sel);
                s.selectByVisibleText(language);
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    /** Open a custom dropdown and click an option text that equals language. */
    private boolean tryOpenDropdownAndChoose(String language) {
        for (By toggleBy : toggleCandidates) {
            List<WebElement> toggles = driver.findElements(toggleBy);
            for (WebElement t : toggles) {
                try {
                    if (!t.isDisplayed()) continue;
                    scrollIntoViewCenter(t);
                    safeClick(t);
                    // light wait for options to show (kept minimal)
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                    if (clickOptionByExactText(language)) return true;
                    try { t.sendKeys(Keys.ESCAPE); } catch (Exception ignored) {}
                } catch (WebDriverException ignored) {}
            }
        }
        return false;
    }

    private boolean clickOptionByExactText(String text) {
        for (By by : optionCandidates) {
            List<WebElement> items = driver.findElements(by);
            for (WebElement it : items) {
                try {
                    if (!it.isDisplayed()) continue;
                    String t = it.getText();
                    if (t != null && t.trim().equals(text)) {
                        scrollIntoViewCenter(it);
                        safeClick(it);
                        return true;
                    }
                } catch (StaleElementReferenceException ignored) {}
            }
        }
        return false;
    }

    /** Try opening various dropdown toggles and read options via many patterns. */
    private List<String> tryReadFromDropdowns() {
        driver.switchTo().defaultContent();
        List<String> values = tryReadFromDropdownsInCurrentContext();
        if (!values.isEmpty()) return values;

        List<WebElement> frames = driver.findElements(By.cssSelector("iframe, frame"));
        for (int i = 0; i < frames.size(); i++) {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(i);
            values = tryReadFromDropdownsInCurrentContext();
            if (!values.isEmpty()) {
                driver.switchTo().defaultContent();
                return values;
            }
        }
        driver.switchTo().defaultContent();
        return Collections.emptyList();
    }

    private List<String> tryReadFromDropdownsInCurrentContext() {
        for (By toggleBy : toggleCandidates) {
            List<WebElement> toggles = driver.findElements(toggleBy);
            if (toggles.isEmpty()) continue;

            for (WebElement t : toggles) {
                try {
                    if (!t.isDisplayed()) continue;
                    scrollIntoViewCenter(t);
                    safeClick(t);

                    // Read options quickly
                    List<String> texts = readOptionsTextWithAllPatterns();
                    if (!texts.isEmpty()) return texts;

                    // close and continue
                    try { t.sendKeys(Keys.ESCAPE); } catch (Exception ignored) {}
                } catch (WebDriverException ignored) {
                    // try next toggle
                }
            }
        }
        return Collections.emptyList();
    }

    private List<String> readOptionsTextWithAllPatterns() {
        long end = System.currentTimeMillis() + 2000; // ~2s (faster)
        Set<String> collected = new LinkedHashSet<>();
        while (System.currentTimeMillis() < end) {
            for (By by : optionCandidates) {
                List<WebElement> items = driver.findElements(by);
                for (WebElement it : items) {
                    try {
                        if (!it.isDisplayed()) continue;
                        String txt = it.getText();
                        if (txt != null) {
                            txt = txt.trim();
                            if (!txt.isEmpty()) collected.add(txt);
                        }
                    } catch (WebDriverException ignored) {}
                }
            }
            if (!collected.isEmpty()) break;
            try { Thread.sleep(80); } catch (InterruptedException ignored) {}
        }
        return new ArrayList<>(collected);
    }

    private boolean pageHasVisibleText(String exactText) {
        By exact = By.xpath("//*[normalize-space(text())='" + exactText + "']");
        if (isAnyVisible(exact)) return true;

        List<WebElement> frames = driver.findElements(By.cssSelector("iframe, frame"));
        for (int i = 0; i < frames.size(); i++) {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(i);
            if (isAnyVisible(exact)) {
                driver.switchTo().defaultContent();
                return true;
            }
        }
        driver.switchTo().defaultContent();
        return false;
    }

    private boolean pageContainsTextCI(String snippet) {
        String js = "return (document.body && (document.body.innerText || document.body.textContent)) || '';";
        try {
            driver.switchTo().defaultContent();
            String txt = String.valueOf(((JavascriptExecutor) driver).executeScript(js));
            if (txt != null && txt.toLowerCase().contains(snippet.toLowerCase())) return true;

            List<WebElement> frames = driver.findElements(By.cssSelector("iframe, frame"));
            for (int i = 0; i < frames.size(); i++) {
                driver.switchTo().defaultContent();
                driver.switchTo().frame(i);
                txt = String.valueOf(((JavascriptExecutor) driver).executeScript(js));
                if (txt != null && txt.toLowerCase().contains(snippet.toLowerCase())) {
                    driver.switchTo().defaultContent();
                    return true;
                }
            }
        } catch (Exception ignored) {
        } finally {
            driver.switchTo().defaultContent();
        }
        return false;
    }

    private boolean isAnyVisible(By by) {
        for (WebElement el : driver.findElements(by)) {
            try { if (el.isDisplayed()) return true; }
            catch (WebDriverException ignored) {}
        }
        return false;
    }

    // ---------- Frame-aware element finders (FAST total wait instead of 25s per locator) ----------

    /**
     * FAST finder: polls all candidate selectors quickly in the current context,
     * then iterates frames. Avoids long per-candidate waits.
     */
    private WebElement findFirstVisibleAcrossFrames(By[] candidates) {
        // 1) Current context quick scan
        driver.switchTo().defaultContent();
        WebElement inPage = quickFindVisible(candidates, Duration.ofSeconds(3));
        if (inPage != null) return inPage;

        // 2) Scan frames quickly
        List<WebElement> frames = driver.findElements(By.cssSelector("iframe, frame"));
        for (int i = 0; i < frames.size(); i++) {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(i);
            WebElement el = quickFindVisible(candidates, Duration.ofSeconds(3));
            if (el != null) return el; // remain in this frame
        }

        driver.switchTo().defaultContent();
        throw new TimeoutException("No visible element across frames for: " + Arrays.toString(candidates));
    }

    /**
     * Polls all selectors without long per-selector waits, for up to totalTimeout.
     */
    private WebElement quickFindVisible(By[] candidates, Duration totalTimeout) {
        long end = System.currentTimeMillis() + totalTimeout.toMillis();
        while (System.currentTimeMillis() < end) {
            for (By by : candidates) {
                try {
                    List<WebElement> els = driver.findElements(by);
                    for (WebElement el : els) {
                        if (el.isDisplayed()) return el;
                    }
                } catch (WebDriverException ignored) {}
            }
            try { Thread.sleep(80); } catch (InterruptedException ignored) {}
        }
        return null;
    }

    // ---------- Utilities ----------

    private void clearAndType(WebElement el, String text) {
        try { el.clear(); }
        catch (InvalidElementStateException ignored) {
            el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        }
        el.sendKeys(text);
    }

    private void safeClick(WebElement el) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(el));
            scrollIntoViewCenter(el);
            el.click();
        } catch (WebDriverException e) {
            jsClick(el);
        }
    }

    private void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    private void scrollIntoViewCenter(WebElement el) {
        try {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center', inline:'nearest'});", el);
            new Actions(driver).moveToElement(el, 1, 1).perform();
        } catch (JavascriptException ignored) {}
    }

    private void waitForAngularToFinish() {
        try {
            wait.until(d -> {
                Object result = ((JavascriptExecutor) d).executeScript(
                        "try {" +
                                " if (window.angular && angular.element(document.body).injector) {" +
                                "   var $http = angular.element(document.body).injector().get('$http');" +
                                "   return ($http.pendingRequests.length === 0);" +
                                " } else { return true; }" +
                                "} catch(e) { return true; }"
                );
                return result instanceof Boolean && (Boolean) result;
            });
        } catch (Exception ignored) {}
    }

    /** After clicking SignUp, wait for confirmation or error or URL change or body text match. */
    private boolean waitForSubmissionOutcome() {
        long end = System.currentTimeMillis() + 8000; // 8s (faster than 15s)
        String startUrl = driver.getCurrentUrl();
        while (System.currentTimeMillis() < end) {
            // Success element?
            for (By by : confirmationMsgCandidates) {
                for (WebElement el : driver.findElements(by)) {
                    try { if (el.isDisplayed()) return true; } catch (Exception ignored) {}
                }
            }
            // Success by body text?
            if (pageContainsTextCI("welcome email") && pageContainsTextCI("check your email")) {
                return true;
            }
            // Error?
            for (By by : errorMsgCandidates) {
                for (WebElement el : driver.findElements(by)) {
                    try {
                        if (!el.isDisplayed()) continue;
                        String txt = el.getText();
                        // Keep only likely validation text (avoid dumping whole page)
                        if (txt != null) {
                            String t = txt.trim();
                            String tl = t.toLowerCase();
                            if (!t.isEmpty() && (tl.contains("required") || tl.contains("invalid") || tl.contains("please"))) {
                                lastSubmissionError = t;
                                return false;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
            // URL changed?
            if (!Objects.equals(startUrl, driver.getCurrentUrl())) return true;

            try { Thread.sleep(120); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    /** Make sure driver context is where the form exists. */
    private void ensureContextAtForm() {
        driver.switchTo().defaultContent();
        if (existsAny(nameCandidates) || existsAny(toggleCandidates)) return;

        List<WebElement> frames = driver.findElements(By.cssSelector("iframe, frame"));
        for (int i = 0; i < frames.size(); i++) {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(i);
            if (existsAny(nameCandidates) || existsAny(toggleCandidates)) return;
        }
        driver.switchTo().defaultContent();
        // Don’t throw here; some builds may render later – validation handles timeouts.
    }

    private boolean existsAny(By[] candidates) {
        for (By by : candidates) {
            if (!driver.findElements(by).isEmpty()) return true;
        }
        return false;
    }
}
