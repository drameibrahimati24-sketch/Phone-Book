import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * PhoneBookApp is a simple Java Console Application to manage phone contacts.
 * It stores contacts in a plain text file (phone.txt) using java.io library.
 * Features: Add Contact, View Contacts, Search Contact, Delete Contact, and Exit.
 * Includes input validation, duplicate prevention, and clean table output.
 */
public class PhoneBookApp {

    // Database text file path
    private static final String FILE_NAME = "phone.txt";

    public static void main(String[] args) {
        // Ensure the data file exists or create it on startup
        initializeFile();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        // Display a premium ASCII Art header at startup
        printHeader();

        // Main application loop
        while (running) {
            printMenu();
            System.out.print("➤ Select an option (1-5): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    addContact(scanner);
                    break;
                case "2":
                    viewContacts();
                    break;
                case "3":
                    searchContacts(scanner);
                    break;
                case "4":
                    deleteContact(scanner);
                    break;
                case "5":
                    System.out.println("\nThank you for using Contact Manager. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid option! Please enter a number between 1 and 5.");
                    break;
            }
            if (running) {
                pressEnterToContinue(scanner);
            }
        }
        scanner.close();
    }

    /**
     * Ensures the database file (phone.txt) exists.
     * If the file is missing, a new one is created.
     */
    private static void initializeFile() {
        File file = new File(FILE_NAME);
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    System.out.println("ℹ️ Database file created successfully: " + FILE_NAME);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error initializing the database file: " + e.getMessage());
        }
    }

    /**
     * Prints a beautiful ASCII application header.
     */
    private static void printHeader() {
        System.out.println("==================================================");
        System.out.println("      📞 PREMIUM CONTACT BOOK MANAGER v1.0         ");
        System.out.println("==================================================");
        System.out.println(" A secure, clean CLI to manage your local contacts");
        System.out.println(" Persistent storage: " + FILE_NAME);
        System.out.println("==================================================\n");
    }

    /**
     * Prints the interactive menu choices.
     */
    private static void printMenu() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║                MAIN DIRECTORY                  ║");
        System.out.println("╠════════════════════════════════════════════════╣");
        System.out.println("║  1. ➕ Add New Contact                         ║");
        System.out.println("║  2. 📖 View All Contacts                       ║");
        System.out.println("║  3. 🔍 Search Contacts                         ║");
        System.out.println("║  4. ❌ Delete Contact                          ║");
        System.out.println("║  5. 🚪 Exit                                    ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }

    /**
     * Prompts the user to press Enter to return to the menu.
     * Keeps the console output clean and easy to read.
     */
    private static void pressEnterToContinue(Scanner scanner) {
        System.out.println("\nPress [ENTER] to return to the menu...");
        scanner.nextLine();
    }

    /**
     * Prompts the user for details to add a new contact.
     * Performs strict input validation and duplicate checks.
     */
    private static void addContact(Scanner scanner) {
        System.out.println("\n--- Add New Contact ---");

        // 1. Get and validate Name
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("❌ Validation Error: Name cannot be empty.");
            return;
        }
        if (name.contains("-")) {
            System.out.println("❌ Validation Error: Name cannot contain the '-' character.");
            return;
        }

        // 2. Get and validate Phone Number
        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine().trim();
        if (phone.isEmpty()) {
            System.out.println("❌ Validation Error: Phone number cannot be empty.");
            return;
        }
        // Basic pattern matching: digits, optional leading +, spaces, dashes
        if (!phone.matches("^\\+?[0-9\\s\\-]+$")) {
            System.out.println("❌ Validation Error: Phone number can only contain digits, spaces, dashes, or a leading '+'.");
            return;
        }

        // Clean up formatting (e.g., convert multiple spaces or format nicely)
        String cleanedPhone = phone.replaceAll("\\s+", " ");

        // 3. Load existing contacts to check for duplicate phone numbers (Bonus requirement)
        List<Contact> contacts = loadAllContacts();
        for (Contact contact : contacts) {
            if (contact.getPhoneNumber().replaceAll("[\\s\\-]", "")
                       .equals(cleanedPhone.replaceAll("[\\s\\-]", ""))) {
                System.out.println("❌ Error: A contact with this phone number (" + contact.getPhoneNumber() + ") already exists (" + contact.getName() + ").");
                return;
            }
        }

        // 4. Save contact to file
        Contact newContact = new Contact(name, cleanedPhone);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(newContact.toFileString());
            writer.newLine();
            System.out.println("✨ Contact added successfully!");
            System.out.printf("👤 Name:  %s%n📱 Phone: %s%n", name, cleanedPhone);
        } catch (IOException e) {
            System.out.println("❌ Error saving contact to file: " + e.getMessage());
        }
    }

    /**
     * Displays all saved contacts from the text file in a neat table.
     */
    private static void viewContacts() {
        System.out.println("\n--- All Saved Contacts ---");
        List<Contact> contacts = loadAllContacts();

        if (contacts.isEmpty()) {
            System.out.println("ℹ️ No contacts found in your book. Choose option 1 to add a new contact.");
            return;
        }

        printContactsTable(contacts);
    }

    /**
     * Prompts for a search term and prints any contacts whose name or phone matches.
     */
    private static void searchContacts(Scanner scanner) {
        System.out.println("\n--- Search Contact ---");
        System.out.print("Enter search keyword (name or phone): ");
        String query = scanner.nextLine().trim().toLowerCase();

        if (query.isEmpty()) {
            System.out.println("❌ Error: Search keyword cannot be empty.");
            return;
        }

        List<Contact> contacts = loadAllContacts();
        List<Contact> matches = new ArrayList<>();

        for (Contact c : contacts) {
            if (c.getName().toLowerCase().contains(query) || c.getPhoneNumber().toLowerCase().contains(query)) {
                matches.add(c);
            }
        }

        if (matches.isEmpty()) {
            System.out.println("ℹ️ No contacts found matching: \"" + query + "\"");
        } else {
            System.out.println("\n🔎 Found " + matches.size() + " matching contact(s):");
            printContactsTable(matches);
        }
    }

    /**
     * Prompts for the name or phone number of a contact to delete,
     * updates the persistent file, and outputs results.
     */
    private static void deleteContact(Scanner scanner) {
        System.out.println("\n--- Delete Contact ---");
        System.out.print("Enter the EXACT name or phone number of the contact to delete: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("❌ Error: Identifier cannot be empty.");
            return;
        }

        List<Contact> contacts = loadAllContacts();
        Contact toRemove = null;

        // Perform search for direct exact match (case-insensitive)
        for (Contact c : contacts) {
            if (c.getName().equalsIgnoreCase(input) || c.getPhoneNumber().equalsIgnoreCase(input)) {
                toRemove = c;
                break;
            }
        }

        if (toRemove == null) {
            System.out.println("❌ Contact not found. Please verify the name or phone number.");
            return;
        }

        // Display contact details and confirm deletion
        System.out.println("\n⚠️ Are you sure you want to delete this contact?");
        System.out.println("   Name  : " + toRemove.getName());
        System.out.println("   Phone : " + toRemove.getPhoneNumber());
        System.out.print("Confirm deletion (y/N): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            contacts.remove(toRemove);
            if (saveAllContacts(contacts)) {
                System.out.println("🗑️ Contact deleted successfully!");
            } else {
                System.out.println("❌ Error: Failed to rewrite contact file.");
            }
        } else {
            System.out.println("❌ Deletion canceled.");
        }
    }

    /**
     * Reads and parses all contacts from the database file.
     * Catches and reports corrupted file lines gracefully without crashing.
     *
     * @return a List of Contact objects
     */
    private static List<Contact> loadAllContacts() {
        List<Contact> list = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines gracefully
                }
                Contact c = Contact.parse(line);
                if (c != null) {
                    list.add(c);
                } else {
                    System.err.println("⚠️ Warning: Skipping corrupted format on line " + lineNumber + ": \"" + line + "\"");
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error reading contacts file: " + e.getMessage());
        }

        return list;
    }

    /**
     * Overwrites the contacts database file with a list of contacts.
     * Used after deletion to keep persistent data in sync.
     *
     * @param contacts the list of contacts to save
     * @return true if save succeeded, false otherwise
     */
    private static boolean saveAllContacts(List<Contact> contacts) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, false))) {
            for (Contact c : contacts) {
                writer.write(c.toFileString());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error writing to contacts file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper to render a list of contacts in a clean, beautifully-aligned CLI table.
     */
    private static void printContactsTable(List<Contact> list) {
        // Standard column widths
        int nameWidth = 26;
        int phoneWidth = 18;

        // Print header border
        System.out.println("┌" + "─".repeat(nameWidth + 2) + "┬" + "─".repeat(phoneWidth + 2) + "┐");
        
        // Print column headers
        System.out.printf("│ %-" + nameWidth + "s │ %-" + phoneWidth + "s │%n", "Name", "Phone Number");
        
        // Print divider border
        System.out.println("├" + "─".repeat(nameWidth + 2) + "┼" + "─".repeat(phoneWidth + 2) + "┤");

        // Print row records
        for (Contact c : list) {
            String formattedName = c.getName();
            // Truncate name if it exceeds column capacity
            if (formattedName.length() > nameWidth) {
                formattedName = formattedName.substring(0, nameWidth - 3) + "...";
            }

            String formattedPhone = c.getPhoneNumber();
            // Truncate phone if it exceeds column capacity
            if (formattedPhone.length() > phoneWidth) {
                formattedPhone = formattedPhone.substring(0, phoneWidth - 3) + "...";
            }

            System.out.printf("│ %-" + nameWidth + "s │ %-" + phoneWidth + "s │%n", formattedName, formattedPhone);
        }

        // Print footer border
        System.out.println("└" + "─".repeat(nameWidth + 2) + "┴" + "─".repeat(phoneWidth + 2) + "┘");
        System.out.printf("  Total Contacts: %d%n", list.size());
    }

    /**
     * Inner class representing a single contact record.
     */
    private static class Contact {
        private final String name;
        private final String phoneNumber;

        public Contact(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        /**
         * Serializes the contact into the file storage format (Name - Phone)
         */
        public String toFileString() {
            return name + " - " + phoneNumber;
        }

        /**
         * Parses a contact line from the text file.
         * Expected format: "Name - Phone"
         *
         * @param line string read from file
         * @return Contact object, or null if line format is invalid
         */
        public static Contact parse(String line) {
            int delimiterIndex = line.indexOf(" - ");
            if (delimiterIndex == -1) {
                return null;
            }

            String name = line.substring(0, delimiterIndex).trim();
            String phone = line.substring(delimiterIndex + 3).trim();

            if (name.isEmpty() || phone.isEmpty()) {
                return null;
            }

            return new Contact(name, phone);
        }
    }
}
