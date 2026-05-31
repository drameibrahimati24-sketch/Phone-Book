import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class App extends JFrame {
    private static final String DATA_FILE = "contacts.txt";
    private final List<Contact> contacts = new ArrayList<>();
    private List<Contact> filteredContacts = new ArrayList<>();
    
    // UI Panels and Layouts
    private JLayeredPane layeredPane;
    private JPanel mainContainer; // Holds screen contents (List and Details)
    private CardLayout cardLayout;
    
    private ContactListPanel listPanel;
    private ContactDetailsPanel detailsPanel;
    private DrawerPanel drawerPanel;
    
    public App() {
        super("Sleek Contact Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Load contacts from file (or write default placeholder list on first launch)
        loadContacts();
        
        // Setup Window size and custom bezel wrapper
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(420, 780));
        
        // Phone Shell wrapping the active screen
        PhoneShellPanel shellPanel = new PhoneShellPanel();
        shellPanel.setBounds(0, 0, 420, 780);
        
        // Screen bounds inside the bezel
        // The empty border in shellPanel leaves space:
        // Top: 45px (Notch/Status Bar area), Bottom: 45px (Home indicator area), Left: 20px, Right: 20px
        int screenWidth = 380;
        int screenHeight = 690;
        
        JPanel screenContainer = new JPanel(new BorderLayout());
        screenContainer.setBounds(20, 45, screenWidth, screenHeight);
        screenContainer.setBackground(new Color(0x0F, 0x17, 0x2A)); // Obsidian screen
        
        // Status Bar
        StatusBarPanel statusBar = new StatusBarPanel();
        screenContainer.add(statusBar, BorderLayout.NORTH);
        
        // Active display area (CardLayout transitions)
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setOpaque(false);
        
        listPanel = new ContactListPanel();
        detailsPanel = new ContactDetailsPanel();
        
        mainContainer.add(listPanel, "LIST");
        mainContainer.add(detailsPanel, "DETAILS");
        
        screenContainer.add(mainContainer, BorderLayout.CENTER);
        
        // Home Navigation Bar at bottom of screen
        HomeBarPanel homeBar = new HomeBarPanel();
        screenContainer.add(homeBar, BorderLayout.SOUTH);
        
        // Drawer sheet overlays (higher layer in JLayeredPane)
        drawerPanel = new DrawerPanel(screenWidth, screenHeight);
        drawerPanel.setBounds(20, 45, screenWidth, screenHeight);
        
        // Add components to JLayeredPane in correct order
        layeredPane.add(shellPanel, Integer.valueOf(1));
        layeredPane.add(screenContainer, Integer.valueOf(2));
        layeredPane.add(drawerPanel, Integer.valueOf(3));
        
        setContentPane(layeredPane);
        pack();
        setLocationRelativeTo(null);
        
        // Set beautiful UI font smoothing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        setVisible(true);
    }
    
    // --- Data Model ---
    public static class Contact implements Serializable {
        private String name;
        private String email;
        private String phone;
        
        public Contact(String name, String email, String phone) {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
    
    // --- CRUD Persistence Operations ---
    private void loadContacts() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            // Write some dynamic sample contacts so the app has high fidelity on first view
            contacts.add(new Contact("Taylor Swift", "taylor@swift.com", "+1 (615) 555-0198"));
            contacts.add(new Contact("Elon Musk", "elon@spacex.com", "+1 (310) 987-1234"));
            contacts.add(new Contact("Bill Gates", "bill@gatesfoundation.org", "+1 (206) 444-5678"));
            contacts.add(new Contact("Zendaya", "zendaya@hbo.com", "+1 (510) 321-4567"));
            contacts.add(new Contact("Ada Lovelace", "ada@computing.org", "+44 20 7946 0958"));
            saveContacts();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    contacts.add(new Contact(parts[0].trim(), parts[1].trim(), parts[2].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading contacts: " + e.getMessage());
        }
    }
    
    private void saveContacts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Contact c : contacts) {
                writer.write(c.getName() + " | " + c.getEmail() + " | " + c.getPhone());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving contacts: " + e.getMessage());
        }
    }
    
    private void filterContacts(String query) {
        filteredContacts.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredContacts.addAll(contacts);
        } else {
            String q = query.toLowerCase().trim();
            for (Contact c : contacts) {
                if (c.getName().toLowerCase().contains(q) || c.getPhone().contains(q) || c.getEmail().toLowerCase().contains(q)) {
                    filteredContacts.add(c);
                }
            }
        }
        listPanel.refreshContactList();
    }
    
    // --- Bespoke UI Classes ---
    
    // Custom phone outer shell bezel rendering
    private class PhoneShellPanel extends JPanel {
        public PhoneShellPanel() {
            setOpaque(false);
            setLayout(null);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Bezel Shadow
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 48, 48);
            
            // Outer bezel metal ring
            g2.setColor(new Color(0x33, 0x41, 0x55)); // Slate metallic border
            g2.setStroke(new BasicStroke(5));
            g2.drawRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 46, 46);
            
            // Bezel dark body
            g2.setColor(new Color(0x09, 0x0D, 0x16)); // Matte black frame
            g2.fillRoundRect(12, 12, getWidth() - 24, getHeight() - 24, 44, 44);
            
            // Speaker Mesh Notch
            g2.setColor(new Color(0x1F, 0x29, 0x37)); // Darker grey mesh
            g2.fillRoundRect(getWidth() / 2 - 40, 20, 80, 6, 3, 3);
            
            // Front camera lens glass reflection
            g2.setColor(new Color(0x0F, 0x17, 0x2A));
            g2.fillOval(getWidth() / 2 + 55, 18, 10, 10);
            g2.setColor(new Color(0x1E, 0x1B, 0x4B)); // Lens iris blue
            g2.fillOval(getWidth() / 2 + 57, 20, 6, 6);
            
            g2.dispose();
        }
    }
    
    // Status Bar showing dynamic time and system statuses
    private class StatusBarPanel extends JPanel {
        private final JLabel timeLabel;
        
        public StatusBarPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(8, 20, 4, 20));
            
            timeLabel = new JLabel();
            timeLabel.setForeground(new Color(0x94, 0xA3, 0xB8)); // Secondary text grey
            timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            updateTime();
            
            // Update time label every second
            javax.swing.Timer timer = new javax.swing.Timer(1000, e -> updateTime());
            timer.start();
            
            JPanel rightIcons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            rightIcons.setOpaque(false);
            
            JLabel signalLabel = new JLabel("📶");
            signalLabel.setForeground(new Color(0x94, 0xA3, 0xB8));
            signalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            JLabel wifiLabel = new JLabel("⚡");
            wifiLabel.setForeground(new Color(0x63, 0x66, 0xF1)); // Accent purple wifi
            wifiLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            
            JLabel batteryLabel = new JLabel("🔋 96%");
            batteryLabel.setForeground(new Color(0x10, 0xB9, 0x81)); // Safe green battery
            batteryLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            
            rightIcons.add(signalLabel);
            rightIcons.add(wifiLabel);
            rightIcons.add(batteryLabel);
            
            add(timeLabel, BorderLayout.WEST);
            add(rightIcons, BorderLayout.EAST);
        }
        
        private void updateTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            timeLabel.setText(sdf.format(new Date()));
        }
    }
    
    // Simulated iPhone-like swipe home indicator at the very bottom
    private class HomeBarPanel extends JPanel {
        public HomeBarPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(380, 24));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw horizontal rounded capsule home swipe line
            g2.setColor(new Color(0x47, 0x55, 0x69)); // Charcoal grey capsule
            g2.fillRoundRect(getWidth() / 2 - 50, 10, 100, 5, 3, 3);
            
            g2.dispose();
        }
    }
    
    // --- LIST SCREEN PANEL ---
    private class ContactListPanel extends JPanel {
        private final JScrollPane scrollPane;
        private final JPanel listContainer;
        private final ModernSearchField searchField;
        
        public ContactListPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            // Header: Title and Search
            JPanel headerPanel = new JPanel();
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
            headerPanel.setOpaque(false);
            headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
            
            JLabel appTitle = new JLabel("Contacts");
            appTitle.setForeground(new Color(0xF8, 0xFA, 0xFC)); // Bold white
            appTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
            appTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            headerPanel.add(appTitle);
            
            headerPanel.add(Box.createVerticalStrut(10));
            
            // Bespoke Search field
            searchField = new ModernSearchField("Search name, phone, or email...");
            searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { filter(); }
                public void removeUpdate(DocumentEvent e) { filter(); }
                public void changedUpdate(DocumentEvent e) { filter(); }
                private void filter() {
                    filterContacts(searchField.getText());
                }
            });
            headerPanel.add(searchField);
            
            add(headerPanel, BorderLayout.NORTH);
            
            // Main List body
            listContainer = new JPanel();
            listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
            listContainer.setOpaque(false);
            
            scrollPane = new JScrollPane(listContainer);
            scrollPane.setBorder(null);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.getVerticalScrollBar().setUnitIncrement(14);
            scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0)); // Super thin custom scrollbar
            
            add(scrollPane, BorderLayout.CENTER);
            
            // Floating Action Button (FAB) Panel at the bottom corner overlay
            JPanel footerPanel = new JPanel(new BorderLayout()) {
                @Override
                public boolean isOptimizedDrawingEnabled() { return false; }
            };
            footerPanel.setOpaque(false);
            footerPanel.setBorder(new EmptyBorder(0, 16, 12, 16));
            
            // Floating custom Add Contact button (Glowing gradient-styled)
            RoundedButton fabAdd = new RoundedButton("＋", 46, new Color(0x63, 0x66, 0xF1), Color.WHITE);
            fabAdd.setFont(new Font("Segoe UI", Font.BOLD, 20));
            fabAdd.setToolTipText("Add new contact");
            fabAdd.addActionListener(e -> {
                drawerPanel.showAddDrawer();
            });
            
            footerPanel.add(fabAdd, BorderLayout.EAST);
            add(footerPanel, BorderLayout.SOUTH);
            
            // Initial filter loads everything
            filterContacts("");
        }
        
        public void refreshContactList() {
            listContainer.removeAll();
            
            List<Contact> source = filteredContacts.isEmpty() && searchField.getText().trim().isEmpty() ? contacts : filteredContacts;
            
            if (source.isEmpty()) {
                // Show beautiful "No contacts found" placeholder card
                JPanel emptyCard = new JPanel(new GridBagLayout());
                emptyCard.setOpaque(false);
                emptyCard.setBorder(new EmptyBorder(40, 20, 40, 20));
                
                JLabel icon = new JLabel("🔍");
                icon.setFont(new Font("Segoe UI", Font.PLAIN, 44));
                JLabel msg = new JLabel("No matching contacts");
                msg.setFont(new Font("Segoe UI", Font.BOLD, 14));
                msg.setForeground(new Color(0x64, 0x74, 0x8B));
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0; gbc.gridy = 0; gbc.weighty = 0.2; gbc.fill = GridBagConstraints.NONE;
                emptyCard.add(icon, gbc);
                gbc.gridy = 1;
                emptyCard.add(msg, gbc);
                
                listContainer.add(emptyCard);
            } else {
                for (Contact c : source) {
                    ContactListItemCard itemCard = new ContactListItemCard(c);
                    listContainer.add(itemCard);
                    listContainer.add(Box.createVerticalStrut(8));
                }
            }
            
            listContainer.revalidate();
            listContainer.repaint();
        }
    }
    
    // --- CONTACT LIST ITEM CARD (HIGH-FIDELITY PANEL) ---
    private class ContactListItemCard extends JPanel {
        private final Contact contact;
        private boolean isHovered = false;
        
        public ContactListItemCard(Contact c) {
            this.contact = c;
            setLayout(new BorderLayout(14, 0));
            setOpaque(false);
            setPreferredSize(new Dimension(348, 64));
            setMaximumSize(new Dimension(348, 64));
            setMinimumSize(new Dimension(348, 64));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            
            // Round colored dynamic avatar on the left
            AvatarBadge avatar = new AvatarBadge(c.getName(), 44);
            add(avatar, BorderLayout.WEST);
            
            // Name and Phone vertical group
            JPanel textGroup = new JPanel();
            textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
            textGroup.setOpaque(false);
            
            JLabel nameLabel = new JLabel(c.getName());
            nameLabel.setForeground(new Color(0xF8, 0xFA, 0xFC)); // White
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            
            JLabel phoneLabel = new JLabel(c.getPhone());
            phoneLabel.setForeground(new Color(0x94, 0xA3, 0xB8)); // Cool grey
            phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            textGroup.add(nameLabel);
            textGroup.add(Box.createVerticalStrut(2));
            textGroup.add(phoneLabel);
            
            add(textGroup, BorderLayout.CENTER);
            
            // Hover highlights and selection triggers
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    detailsPanel.setContact(contact);
                    cardLayout.show(mainContainer, "DETAILS");
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw rounded solid background panel (Dark slate when idle, lighter card grey on hover)
            Color bgColor = isHovered ? new Color(0x33, 0x41, 0x55, 200) : new Color(0x1E, 0x29, 0x3B, 150);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            
            g2.dispose();
        }
    }
    
    // --- PROFILE / DETAILS VIEW SCREEN ---
    private class ContactDetailsPanel extends JPanel {
        private Contact currentContact;
        
        private final AvatarBadge profileAvatar;
        private final JLabel nameHeader;
        private final JLabel phoneDetail;
        private final JLabel emailDetail;
        
        // Simulation elements
        private final JLabel actionStatus;
        
        public ContactDetailsPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            // Initialize final variable first to avoid lambda compile reference error
            actionStatus = new JLabel(" ");
            actionStatus.setForeground(new Color(0x10, 0xB9, 0x81)); // Green feedback text
            actionStatus.setFont(new Font("Segoe UI", Font.ITALIC | Font.BOLD, 13));
            actionStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Top action bar (Back, Edit, Delete)
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setOpaque(false);
            topBar.setBorder(new EmptyBorder(12, 16, 12, 16));
            
            RoundedButton backBtn = new RoundedButton("← Back", 36, new Color(0x1E, 0x29, 0x3B), new Color(0xE2, 0xE8, 0xF0));
            backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            backBtn.addActionListener(e -> {
                actionStatus.setText(" ");
                cardLayout.show(mainContainer, "LIST");
            });
            topBar.add(backBtn, BorderLayout.WEST);
            
            JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightGroup.setOpaque(false);
            
            RoundedButton editBtn = new RoundedButton("Edit", 36, new Color(0x10, 0xB9, 0x81), Color.WHITE);
            editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            editBtn.addActionListener(e -> {
                if (currentContact != null) {
                    drawerPanel.showEditDrawer(currentContact);
                }
            });
            
            RoundedButton deleteBtn = new RoundedButton("Delete", 36, new Color(0xF4, 0x3F, 0x5E), Color.WHITE);
            deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteBtn.addActionListener(e -> {
                if (currentContact != null) {
                    // Custom theme delete verification modal
                    drawerPanel.showDeleteConfirmDialog(currentContact);
                }
            });
            
            rightGroup.add(editBtn);
            rightGroup.add(deleteBtn);
            topBar.add(rightGroup, BorderLayout.EAST);
            
            add(topBar, BorderLayout.NORTH);
            
            // Scrollable Content
            JPanel centerContent = new JPanel();
            centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
            centerContent.setOpaque(false);
            centerContent.setBorder(new EmptyBorder(20, 24, 20, 24));
            
            profileAvatar = new AvatarBadge("", 90);
            profileAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerContent.add(profileAvatar);
            
            centerContent.add(Box.createVerticalStrut(14));
            
            nameHeader = new JLabel("Name");
            nameHeader.setForeground(new Color(0xF8, 0xFA, 0xFC));
            nameHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
            nameHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerContent.add(nameHeader);
            
            centerContent.add(Box.createVerticalStrut(30));
            
            // Details Info Box Card
            JPanel infoCard = new JPanel();
            infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
            infoCard.setOpaque(false);
            infoCard.setBorder(new EmptyBorder(16, 20, 16, 20));
            infoCard.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoCard.setPreferredSize(new Dimension(332, 140));
            infoCard.setMaximumSize(new Dimension(332, 140));
            
            // Details phone row
            JPanel phoneRow = new JPanel(new BorderLayout(10, 0));
            phoneRow.setOpaque(false);
            JLabel phTitle = new JLabel("MOBILE");
            phTitle.setForeground(new Color(0x63, 0x66, 0xF1)); // Indigo label
            phTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
            phoneDetail = new JLabel("Phone Number");
            phoneDetail.setForeground(new Color(0xF8, 0xFA, 0xFC));
            phoneDetail.setFont(new Font("Segoe UI", Font.BOLD, 15));
            phoneRow.add(phTitle, BorderLayout.NORTH);
            phoneRow.add(phoneDetail, BorderLayout.CENTER);
            
            // Details email row
            JPanel emailRow = new JPanel(new BorderLayout(10, 0));
            emailRow.setOpaque(false);
            JLabel emTitle = new JLabel("EMAIL");
            emTitle.setForeground(new Color(0x63, 0x66, 0xF1));
            emTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
            emailDetail = new JLabel("Email Address");
            emailDetail.setForeground(new Color(0xF8, 0xFA, 0xFC));
            emailDetail.setFont(new Font("Segoe UI", Font.BOLD, 15));
            emailRow.add(emTitle, BorderLayout.NORTH);
            emailRow.add(emailDetail, BorderLayout.CENTER);
            
            infoCard.add(phoneRow);
            infoCard.add(Box.createVerticalStrut(16));
            infoCard.add(emailRow);
            
            // Make details card rounded slate custom background
            JPanel wrappedCard = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0x1E, 0x29, 0x3B, 200)); // Dark card slate
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.dispose();
                }
            };
            wrappedCard.setOpaque(false);
            wrappedCard.setAlignmentX(Component.CENTER_ALIGNMENT);
            wrappedCard.add(infoCard);
            
            centerContent.add(wrappedCard);
            centerContent.add(Box.createVerticalStrut(24));
            
            // Simulation Call / Message buttons
            JPanel actionButtonsGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            actionButtonsGroup.setOpaque(false);
            
            RoundedButton callBtn = new RoundedButton("📞 Call", 42, new Color(0x10, 0xB9, 0x81), Color.WHITE);
            callBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            callBtn.setPreferredSize(new Dimension(100, 42));
            callBtn.addActionListener(e -> simulateAction("Calling " + currentContact.getName() + "..."));
            
            RoundedButton mailBtn = new RoundedButton("✉ Email", 42, new Color(0x63, 0x66, 0xF1), Color.WHITE);
            mailBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            mailBtn.setPreferredSize(new Dimension(100, 42));
            mailBtn.addActionListener(e -> simulateAction("Opening Mail compose to " + currentContact.getEmail() + "..."));
            
            actionButtonsGroup.add(callBtn);
            actionButtonsGroup.add(mailBtn);
            
            centerContent.add(actionButtonsGroup);
            
            centerContent.add(Box.createVerticalStrut(20));
            centerContent.add(actionStatus);
            
            add(centerContent, BorderLayout.CENTER);
        }
        
        public void setContact(Contact c) {
            this.currentContact = c;
            profileAvatar.setName(c.getName());
            nameHeader.setText(c.getName());
            phoneDetail.setText(c.getPhone());
            emailDetail.setText(c.getEmail());
            actionStatus.setText(" ");
        }
        
        private void simulateAction(String message) {
            actionStatus.setText(message);
            Timer timer = new Timer(3000, e -> actionStatus.setText(" "));
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    // --- ROUND INITIALS DYNAMIC AVATAR COMPONENT ---
    private static class AvatarBadge extends JPanel {
        private String name;
        private int size;
        
        public AvatarBadge(String name, int size) {
            this.name = name;
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setMinimumSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));
            setOpaque(false);
        }
        
        public void setName(String name) {
            this.name = name;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Dynamic colorful background based on name hash
            g2.setColor(getAvatarColor(name));
            g2.fillOval(0, 0, size, size);
            
            // Draw centered white Initials
            String initials = getInitials(name);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2 - 2));
            FontMetrics fm = g2.getFontMetrics();
            int x = (size - fm.stringWidth(initials)) / 2;
            int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(initials, x, y);
            
            g2.dispose();
        }
        
        private String getInitials(String s) {
            if (s == null || s.trim().isEmpty()) return "?";
            String[] parts = s.trim().split("\\s+");
            if (parts.length == 1) {
                return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            } else {
                return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
            }
        }
        
        private Color getAvatarColor(String s) {
            if (s == null || s.isEmpty()) return new Color(0x63, 0x66, 0xF1); // Fallback Purple
            int hash = s.hashCode();
            float hue = Math.abs(hash % 360) / 360f;
            // Vibrant pastel brightness
            return Color.getHSBColor(hue, 0.60f, 0.68f);
        }
    }
    
    // --- BESPOKE MODAL SLIDE-IN DRAWER SHEET OVERLAY ---
    private class DrawerPanel extends JPanel {
        private final int width;
        private final int height;
        
        private boolean isVisible = false;
        private Contact editingContact = null;
        
        // Input Controls
        private final JPanel overlayCard;
        private final JLabel drawerTitle;
        private final ModernInputField nameField;
        private final ModernInputField emailField;
        private final ModernInputField phoneField;
        
        // Custom Delete dialog elements
        private boolean isDeleteConfirmMode = false;
        private Contact deletingContact = null;
        private final JLabel deleteConfirmText;
        private final RoundedButton deleteConfirmBtn;
        private final RoundedButton saveBtn;
        
        public DrawerPanel(int width, int height) {
            this.width = width;
            this.height = height;
            
            setLayout(null);
            setOpaque(false);
            setVisible(false);
            
            // Card bounds dimensions
            int cardW = width - 32;
            int cardH = 340;
            int cardX = 16;
            int cardY = height - cardH - 30; // Float near bottom
            
            overlayCard = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Card background with rich shadow
                    g2.setColor(new Color(0, 0, 0, 180));
                    g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 24, 24);
                    
                    g2.setColor(new Color(0x1E, 0x29, 0x3B)); // Rich dark slate
                    g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 24, 24);
                    
                    g2.setColor(new Color(0x33, 0x41, 0x55)); // Thin elegant outline border
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 24, 24);
                    
                    g2.dispose();
                }
            };
            overlayCard.setOpaque(false);
            overlayCard.setBounds(cardX, cardY, cardW, cardH);
            overlayCard.setLayout(null);
            
            drawerTitle = new JLabel("Add Contact");
            drawerTitle.setForeground(Color.WHITE);
            drawerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            drawerTitle.setBounds(20, 16, 200, 24);
            overlayCard.add(drawerTitle);
            
            // Text inputs
            nameField = new ModernInputField("Full Name");
            nameField.setBounds(20, 56, cardW - 40, 42);
            overlayCard.add(nameField);
            
            phoneField = new ModernInputField("Phone Number");
            phoneField.setBounds(20, 110, cardW - 40, 42);
            overlayCard.add(phoneField);
            
            emailField = new ModernInputField("Email Address");
            emailField.setBounds(20, 164, cardW - 40, 42);
            overlayCard.add(emailField);
            
            // Actions
            RoundedButton cancelBtn = new RoundedButton("Cancel", 38, new Color(0x33, 0x41, 0x55), new Color(0xE2, 0xE8, 0xF0));
            cancelBtn.setBounds(20, 226, 110, 38);
            cancelBtn.addActionListener(e -> hideDrawer());
            overlayCard.add(cancelBtn);
            
            saveBtn = new RoundedButton("Save", 38, new Color(0x63, 0x66, 0xF1), Color.WHITE);
            saveBtn.setBounds(cardW - 130, 226, 110, 38);
            saveBtn.addActionListener(e -> saveAction());
            overlayCard.add(saveBtn);
            
            // --- Custom Delete dialog details inside drawer ---
            deleteConfirmText = new JLabel("Are you sure you want to delete this contact?");
            deleteConfirmText.setForeground(new Color(0xE2, 0xE8, 0xF0));
            deleteConfirmText.setFont(new Font("Segoe UI", Font.BOLD, 14));
            deleteConfirmText.setBounds(20, 80, cardW - 40, 30);
            deleteConfirmText.setVisible(false);
            overlayCard.add(deleteConfirmText);
            
            deleteConfirmBtn = new RoundedButton("Yes, Delete", 38, new Color(0xF4, 0x3F, 0x5E), Color.WHITE);
            deleteConfirmBtn.setBounds(cardW - 140, 226, 120, 38);
            deleteConfirmBtn.addActionListener(e -> deleteAction());
            deleteConfirmBtn.setVisible(false);
            overlayCard.add(deleteConfirmBtn);
            
            add(overlayCard);
            
            // Click outside overlayCard to dismiss drawer
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!overlayCard.getBounds().contains(e.getPoint())) {
                        hideDrawer();
                    }
                }
            });
        }
        
        public void showAddDrawer() {
            editingContact = null;
            isDeleteConfirmMode = false;
            
            drawerTitle.setText("New Contact");
            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            
            toggleFormVisibility(true);
            toggleDeleteVisibility(false);
            
            overlayCard.setSize(width - 32, 290);
            overlayCard.setLocation(16, height - 290 - 30);
            
            setVisible(true);
            nameField.requestFocusInWindow();
        }
        
        public void showEditDrawer(Contact c) {
            editingContact = c;
            isDeleteConfirmMode = false;
            
            drawerTitle.setText("Edit Contact");
            nameField.setText(c.getName());
            emailField.setText(c.getEmail());
            phoneField.setText(c.getPhone());
            
            toggleFormVisibility(true);
            toggleDeleteVisibility(false);
            
            overlayCard.setSize(width - 32, 290);
            overlayCard.setLocation(16, height - 290 - 30);
            
            setVisible(true);
            nameField.requestFocusInWindow();
        }
        
        public void showDeleteConfirmDialog(Contact c) {
            deletingContact = c;
            isDeleteConfirmMode = true;
            
            drawerTitle.setText("Delete Contact?");
            deleteConfirmText.setText("Delete \"" + c.getName() + "\" permanently?");
            
            toggleFormVisibility(false);
            toggleDeleteVisibility(true);
            
            overlayCard.setSize(width - 32, 190);
            overlayCard.setLocation(16, height - 190 - 30);
            
            setVisible(true);
        }
        
        private void toggleFormVisibility(boolean visible) {
            nameField.setVisible(visible);
            emailField.setVisible(visible);
            phoneField.setVisible(visible);
            saveBtn.setVisible(visible);
        }
        
        private void toggleDeleteVisibility(boolean visible) {
            deleteConfirmText.setVisible(visible);
            deleteConfirmBtn.setVisible(visible);
        }
        
        public void hideDrawer() {
            setVisible(false);
            editingContact = null;
            deletingContact = null;
        }
        
        private void saveAction() {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            
            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Phone cannot be empty!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (editingContact == null) {
                // Add contact
                Contact fresh = new Contact(name, email, phone);
                contacts.add(fresh);
            } else {
                // Edit existing
                editingContact.setName(name);
                editingContact.setEmail(email);
                editingContact.setPhone(phone);
                detailsPanel.setContact(editingContact);
            }
            
            saveContacts();
            filterContacts(""); // Refresh
            hideDrawer();
        }
        
        private void deleteAction() {
            if (deletingContact != null) {
                contacts.remove(deletingContact);
                saveContacts();
                filterContacts(""); // Refresh
                hideDrawer();
                cardLayout.show(mainContainer, "LIST"); // Return to list view
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Draw dimmed visual scrim panel blocking backdrop clicks
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 130)); // 50% opacity mask
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
    
    // --- CUSTOM DECORATED INPUT FIELDS ---
    private static class ModernInputField extends JTextField implements FocusListener {
        private final String placeholder;
        private boolean isPlaceholderActive = true;
        private boolean isFocused = false;
        
        public ModernInputField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setForeground(new Color(0x64, 0x74, 0x8B)); // Slate gray placeholder
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setText(placeholder);
            setBorder(new EmptyBorder(10, 16, 10, 16));
            addFocusListener(this);
        }
        
        @Override
        public String getText() {
            return isPlaceholderActive ? "" : super.getText();
        }
        
        @Override
        public void setText(String t) {
            if (t == null || t.isEmpty()) {
                isPlaceholderActive = true;
                setForeground(new Color(0x64, 0x74, 0x8B));
                super.setText(placeholder);
            } else {
                isPlaceholderActive = false;
                setForeground(Color.WHITE);
                super.setText(t);
            }
        }
        
        public void focusGained(FocusEvent e) {
            isFocused = true;
            if (isPlaceholderActive) {
                isPlaceholderActive = false;
                setText("");
            }
            repaint();
        }
        
        public void focusLost(FocusEvent e) {
            isFocused = false;
            if (super.getText().isEmpty()) {
                isPlaceholderActive = true;
                setForeground(new Color(0x64, 0x74, 0x8B));
                super.setText(placeholder);
            }
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw background fill
            g2.setColor(new Color(0x0F, 0x17, 0x2A)); // Screen background Obsidian
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            
            // Draw focused glow/outline
            if (isFocused) {
                g2.setColor(new Color(0x63, 0x66, 0xF1)); // Indigo focus
                g2.setStroke(new BasicStroke(2f));
            } else {
                g2.setColor(new Color(0x33, 0x41, 0x55)); // Slate border outline
                g2.setStroke(new BasicStroke(1f));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            
            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    // --- BEAUTIFULLY STYLED SEARCH INPUT BAR ---
    private static class ModernSearchField extends JTextField implements FocusListener {
        private final String placeholder;
        private boolean isPlaceholderActive = true;
        private boolean isFocused = false;
        
        public ModernSearchField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setForeground(new Color(0x64, 0x74, 0x8B));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setText(placeholder);
            setBorder(new EmptyBorder(8, 38, 8, 14)); // Wide left padding for search lens icon space
            addFocusListener(this);
            setPreferredSize(new Dimension(348, 38));
            setMaximumSize(new Dimension(348, 38));
        }
        
        @Override
        public String getText() {
            return isPlaceholderActive ? "" : super.getText();
        }
        
        public void focusGained(FocusEvent e) {
            isFocused = true;
            if (isPlaceholderActive) {
                isPlaceholderActive = false;
                super.setText("");
                setForeground(Color.WHITE);
            }
            repaint();
        }
        
        public void focusLost(FocusEvent e) {
            isFocused = false;
            if (super.getText().isEmpty()) {
                isPlaceholderActive = true;
                setForeground(new Color(0x64, 0x74, 0x8B));
                super.setText(placeholder);
            }
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw background capsule fill
            g2.setColor(new Color(0x1E, 0x29, 0x3B)); // Dark slate card
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            
            // Draw focus border
            if (isFocused) {
                g2.setColor(new Color(0x63, 0x66, 0xF1)); // Indigo glow outline
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(new Color(0x33, 0x41, 0x55));
                g2.setStroke(new BasicStroke(1f));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            
            // Draw a stylish glass magnifying glass search icon on the left
            g2.setColor(new Color(0x64, 0x74, 0x8B));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(15, 12, 10, 10);
            g2.drawLine(23, 20, 27, 24);
            
            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    // --- CUSTOM SOLID ROUNDED CONTAINER BUTTON ---
    private static class RoundedButton extends JButton {
        private final Color baseColor;
        private final Color textColor;
        private boolean isHovered = false;
        private final int roundedRadius;
        
        public RoundedButton(String text, int roundedRadius, Color baseColor, Color textColor) {
            super(text);
            this.roundedRadius = roundedRadius;
            this.baseColor = baseColor;
            this.textColor = textColor;
            
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(textColor);
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw hover lighting transition
            Color renderColor = isHovered ? baseColor.brighter() : baseColor;
            g2.setColor(renderColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), roundedRadius, roundedRadius);
            
            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    // --- MAIN EXECUTOR ENTRY POINT ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
