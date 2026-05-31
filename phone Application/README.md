# Sleek Mobile Contact Manager GUI (Java Swing)

A highly polished desktop contact management application designed to look and feel like a modern, premium smartphone app. Unlike default Java Swing interfaces which can look outdated, this application leverages hardware-accelerated anti-aliased 2D graphics (`Graphics2D`), smooth color-coded initial badges, custom placeholder components, real-time list filtering, and elegant modular overlay drawers.

---

## ✨ Features

- **📱 High-Fidelity Smartphone Shell Bezel:** Built with a custom matte graphite phone body, front speaker notch, camera lens reflections, and a bottom swipe indicator bar.
- **🕒 Simulated System Status Bar:** Includes a dynamically updating digital local clock (updating every second), reception lines, animated Wi-Fi indicator, and an active battery health indicator.
- **🏷️ Dynamic Initial-Based Avatars:** Contacts receive circular avatar badges containing their initials. The avatar's vibrant background is automatically computed using a hash of the contact's name, rendering unique, consistent colors for every contact.
- **🔍 Real-Time Live Filtering:** An interactive search bar filters your contacts list instantly as you type names, phone numbers, or email addresses.
- **⚡ In-App Slide-in Overlay Drawer Sheets:** Bypasses crude OS `JOptionPane` dialog boxes. Adding and editing sheets slide cleanly over the active screen inside the phone workspace with custom glowing active borders.
- **💾 Automatic Text-File Persistence:** Saves changes instantly on additions, modifications, or deletions to a lightweight `contacts.txt` local database.
- **🎁 Demo Contacts Preloaded:** Comes preloaded with beautiful demo contact profiles (Taylor Swift, Elon Musk, Bill Gates, Zendaya, and Ada Lovelace) on first launch so the system never looks empty!

---

## 🛠️ Tech Stack & Requirements

- **Runtime Environment:** Java JDK 8 or higher (Fully verified and optimized on OpenJDK 25)
- **Framework:** Java Swing & AWT (no external library dependencies required, runs out-of-the-box!)
- **Storage:** Local flat-file database (`contacts.txt`)

---

## 🚀 How to Compile & Run

Open your terminal or command prompt inside the project folder:

### 1. Compile the Source Code
```bash
# Create bin directory for compile artifacts
mkdir bin

# Compile the App.java source file into the bin folder
javac -d bin src/App.java
```

### 2. Launch the Application
```bash
# Run the application using the compiled bytecode in bin
java -cp bin App
```

---

## 🎨 Creative Design Systems Inside Java Swing
- **Obsidian Dark Color Theme:** Styled with deep slate slate shades (`0x0F172A`, `0x1E293B`, `0x334155`) with high contrast royal indigo (`0x6366F1`) and vibrant green/rose red indicators to ensure premium aesthetics.
- **Dynamic Graphics2D Rendering:** Utilizes advanced anti-aliasing features to guarantee clean curved bezels and circular icons without pixelated rough edges:
  ```java
  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  ```
