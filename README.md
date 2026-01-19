
# SignUpAutomation
### Beginner‑Friendly UI Test Suite (Selenium + TestNG)
**Author:** Sarvesh Kumar Ojha

---
## 📌 About This Project
This project automates the **Sign Up** process of a sample website using Selenium WebDriver and TestNG. It opens the SignUp page, selects a language, fills the form, accepts terms, submits it, and checks if a confirmation message appears.

It is designed to be simple, readable, and beginner‑friendly while still being reliable.

---
## 🚀 Quick Start
### 1️⃣ Requirements
- Java **17+**
- Maven **3.9+**
- Google Chrome installed

### 2️⃣ Run Tests
Normal mode:
```
mvn clean test
```
Headless mode:
```
mvn -Dheadless=true clean test
```
---
## 📂 Project Structure
```
src/test/java/com/assignment/
 ├─ tests/SignUpTest.java
 ├─ pages/SignUpPage.java
 └─ utils/TestReportListener.java
src/test/resources/log4j2.xml
pom.xml
```

---
## ⚙️ Configuration (Optional)
You can change settings while running:

| Property | Description | Example |
|----------|-------------|---------|
| `headless` | Run browser without UI | `-Dheadless=true` |
| `signup.url` | Change SignUp page URL | `-Dsignup.url=https://site.com` |

---
## 🧠 How It Works (Simple Overview)
1. Open browser (Chrome)
2. Go to SignUp page
3. Look for languages (English + Dutch)
4. Select **English**
5. Enter Name, Organization, Email
6. Accept Terms checkbox
7. Submit form
8. Verify success message

The Page Object Model makes tests clean and easy to maintain.

---
## 📝 Logging & Reports
- Logs stored in: `logs/test.log`
- Log file auto‑rotates
- Custom TestNG Listener (`TestReportListener`) can generate HTML reports and screenshots

---
## ❗ Troubleshooting
- **Driver not downloading?** Check internet or allow WebDriverManager.
- **Element not found?** The page sometimes loads inside an iframe; the project handles this automatically.
- **Checkbox not clickable?** The framework uses fallback methods like JS click.

---
## 👨‍💻 Author
**Sarvesh Kumar Ojha**
Professional II — Navi Mumbai

---
## 📘 License
For educational and demo purposes. Add a license if distributing publicly.
