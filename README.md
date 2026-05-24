# 📞 Premium Contact Book Manager

A clean, robust, and elegant Java Console (CLI) application for managing contact records (Name and Phone Number) persistently. This application complies with strict programming constraints, using **only standard Java Standard Library packages (`java.io`)** and requiring zero database setups or external dependencies.

---

## ✨ Features Included

### Core Requirements
1. **➕ Add Contact**: Prompt the user for name and phone number and append the new entry to `phone.txt` (format: `Name - Phone`).
2. **📖 View All Contacts**: Load and print all saved contacts from `phone.txt`.
3. **🔍 Search Contacts**: Search case-insensitively across both contact names and phone numbers.
4. **❌ Delete Contact**: Locate a contact by name or phone number, ask for confirmation, and update `phone.txt` accordingly.

### 🌟 Bonus Improvements
- **🔒 Duplicate Prevention**: Automatically screens database records to block adding contacts with identical phone numbers.
- **🎨 ASCII Table Layout**: Renders output inside a beautifully formatted, auto-padded CLI grid table with exact headers, borders, and truncations.
- **🛡️ Inputs Validation**: Filters names from illegal delimiter formats (`-`) and validates phone number sequences (permits only digits, spaces, dashes, or a leading `+`).
- **♻️ Failure Tolerance**: Skips any manually corrupted database text lines gracefully, reporting warnings without interrupting execution.
- **🔋 Try-With-Resources**: Keeps file input/output streams closed automatically to prevent memory leaks.

---

## 📂 File Architecture

The project workspace consists of:
```
stores phone/
├── PhoneBookApp.java   # Core application class & command line interface
├── phone.txt           # Plain text file database (Name - Phone)
└── README.md           # Documentation guide
```

---

## 🚀 How to Run the Application

Follow these steps to run the application on your computer:

### Prerequisites
- Make sure you have **Java Development Kit (JDK) 8 or higher** installed. Check this by executing:
  ```bash
  java -version
  ```

### Steps to Compile and Run
1. Open your terminal or Command Prompt (CMD/PowerShell) in the workspace directory.
2. Compile the Java class:
   ```bash
   javac PhoneBookApp.java
   ```
3. Run the compiled application:
   ```bash
   java PhoneBookApp
   ```

*Note: If you have Java 11 or higher, you can run the program directly without manual compilation:*
```bash
java PhoneBookApp.java
```

---

## 💾 Data Persistence Format
All contacts are saved inside `phone.txt` utilizing the following format:
```text
John Doe - 08123456789
Jane Smith - 08987654321
Alice Johnson - 08555123456
```
This is fully loaded and rewritten dynamically inside memory when additions or deletions take place.
