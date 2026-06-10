import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class AppDraft7 extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private JList<String> tenantList;
    private JList<String> utilityList;
    private DefaultListModel<String> listModel;
    private DefaultListModel<String> utilityListModel;

    // room monitoring colours
    private final Color BG_DARK_GRAY = new Color(67, 67, 67);
    private final Color CANVA_GREEN = new Color(102, 187, 106);
    private final Color CANVA_RED = new Color(239, 83, 80);

    private static final String ADMIN_CREATION_KEY = "adminadminkey6767";
    private static final String FILE_PATH = "system_data.txt";
    private static final String UTILITIES_FILE = "utilities_data.txt";

    public AppDraft7(boolean openDashboardDirectly) {
        
        setTitle("Dorm Management System");
        // setIconImage(new ImageIcon("placeholderphoto.png").getImage());
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        JPanel loginScreen = createLoginScreen();
        JPanel dashboardScreen = createDashboardScreen();
        JPanel tenantsScreen = createTenantsScreen();
        JPanel roomsScreen = createRoomsScreen();
        JPanel utilitiesScreen = createUtilitiesScreen();

        mainContainer.add(loginScreen, "LOGIN");
        mainContainer.add(dashboardScreen, "DASHBOARD");
        mainContainer.add(tenantsScreen, "TENANTS");
        mainContainer.add(roomsScreen, "ROOMS");
        mainContainer.add(utilitiesScreen, "UTILITIES");

        add(mainContainer);
        if (openDashboardDirectly) {
            cardLayout.show(mainContainer, "DASHBOARD");
        } else {
            cardLayout.show(mainContainer, "LOGIN");
        }
        loadTenantDataFromFile();
        loadUtilityDataFromFile();

        setVisible(true);
    }

    private void loadTenantDataFromFile() {
        listModel.clear();
        try (FileReader fr = new FileReader(FILE_PATH);
             BufferedReader br = new BufferedReader(fr)) {
            
            String line;
            boolean insideTenantBlock = false;
            String name = "", room = "", contact = "", leaseStart = "";

            while ((line = br.readLine()) != null) {
                if (line.contains("[TENANT]")) {
                    insideTenantBlock = true;
                    continue;
                }
                if (line.startsWith("------------------------------------------------")) {
                    if (insideTenantBlock && !name.isEmpty()) {
                        listModel.addElement("Tenant Name: " + name + "  |  Room: " + room + "  |  Contact: " + contact + "  |  Lease Start: " + leaseStart);
                        listModel.addElement("");

                        name = ""; room = ""; contact = ""; leaseStart = "";
                        insideTenantBlock = false;
                    }
                    continue;
                }
                if (insideTenantBlock) {
                    if (line.startsWith("Name: ")) name = line.replace("Name: ", "").trim();
                    if (line.startsWith("Room: ")) room = line.replace("Room: ", "").trim();
                    if (line.startsWith("Contact: ")) contact = line.replace("Contact: ", "").trim();
                    if (line.startsWith("Lease Start: ")) leaseStart = line.replace("Lease Start: ", "").trim();
                }
            }
        } catch (IOException e) {
        }
    }

    // updates the txt file when deleting
    private void rewriteFileFromList() {
        ArrayList<String> retainedLines = new ArrayList<>();
        
        try (FileReader fr = new FileReader(FILE_PATH);
             BufferedReader br = new BufferedReader(fr)) {
            
            String line;
            ArrayList<String> currentBlock = new ArrayList<>();
            boolean insideTenantBlock = false;
            String currentName = "";

            while ((line = br.readLine()) != null) {
                currentBlock.add(line);
                
                if (line.contains("[TENANT]")) {
                    insideTenantBlock = true;
                }
                if (insideTenantBlock && line.startsWith("Name: ")) {
                    currentName = line.replace("Name: ", "").trim();
                }
                
                if (line.startsWith("------------------------------------------------")) {
                    boolean preserveBlock = true;
                    
                    if (insideTenantBlock) {
                        preserveBlock = false;
                        for (int i = 0; i < listModel.size(); i++) { //checks tenant info
                            String listEntry = listModel.get(i);
                            if (listEntry.startsWith(currentName + "    |    ")) {
                                preserveBlock = true;
                                break;
                            }
                        }
                    }
                    
                    if (preserveBlock) {
                        retainedLines.addAll(currentBlock);
                    }
                    currentBlock.clear();
                    insideTenantBlock = false;
                }
            }
            retainedLines.addAll(currentBlock);
            
        } catch (IOException e) {
            System.err.println("Error parsing file for removal: " + e.getMessage());
        }

        
        try (FileWriter fw = new FileWriter(FILE_PATH, false);
        // overwrites txt file with updated list
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (String outputLine : retainedLines) {
                bw.write(outputLine);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error during file structural update sync: " + e.getMessage());
        }
    }

    private void loadUtilityDataFromFile() {
        utilityListModel.clear();
        try (FileReader fr = new FileReader(UTILITIES_FILE);
             BufferedReader br = new BufferedReader(fr)) {
            
            String line;
            boolean insideUtilityBlock = false;
            String status = "",tenant = "", electricity = "", water = "", other = "";

            while ((line = br.readLine()) != null) {
                if (line.contains("[UTILITIES]")) {
                    insideUtilityBlock = true;
                    continue;
                }
                if (line.startsWith("------------------------------------------------")) {
                    if (insideUtilityBlock && !status.isEmpty()) {
                        utilityListModel.addElement("Status: " + status + "  | Tenant: " + tenant + "  | Electricity: " + electricity + "  |  Water: " + water + "  |  Other: " + other + "");
                        utilityListModel.addElement("");

                        status = ""; electricity = ""; water = ""; other = "";
                        insideUtilityBlock = false;
                    }
                    continue;
                }
                if (insideUtilityBlock) {
                    if (line.startsWith("Status: ")) status = line.replace("Status: ", "").trim();
                    if (line.startsWith("Tenant: ")) tenant = line.replace("Tenant: ", "").trim();
                    if (line.startsWith("Electricity: ")) electricity = line.replace("Electricity: ", "").trim();
                    if (line.startsWith("Water: ")) water = line.replace("Water: ", "").trim();
                    if (line.startsWith("Other: ")) other = line.replace("Other: ", "").trim();
                }
            }
        } catch (IOException e) {
        }
    }

    // updates the txt file when deleting
    private void rewriteUtilityFileFromList() {
        ArrayList<String> retainedLines = new ArrayList<>();
        
        try (FileReader fr = new FileReader(UTILITIES_FILE);
             BufferedReader br = new BufferedReader(fr)) {
            
            String line;
            ArrayList<String> currentBlock = new ArrayList<>();
            boolean insideUtilityBlock = false;
            String currentName = "";

            while ((line = br.readLine()) != null) {
                currentBlock.add(line);
                
                if (line.contains("[UTILITIES]")) {
                    insideUtilityBlock = true;
                }
                if (insideUtilityBlock && line.startsWith("Name: ")) {
                    currentName = line.replace("Name: ", "").trim();
                }
                
                if (line.startsWith("------------------------------------------------")) {
                    boolean preserveBlock = true;
                    
                    if (insideUtilityBlock) {
                        preserveBlock = false;
                        for (int i = 0; i < utilityListModel.size(); i++) { //checks utility info
                            String listEntry = utilityListModel.get(i);
                            if (listEntry.startsWith(currentName + "    |    ")) {
                                preserveBlock = true;
                                break;
                            }
                        }
                    }
                    
                    if (preserveBlock) {
                        retainedLines.addAll(currentBlock);
                    }
                    currentBlock.clear();
                    insideUtilityBlock = false;
                }
            }
            retainedLines.addAll(currentBlock);
            
        } catch (IOException e) {
            System.err.println("Error parsing file for removal: " + e.getMessage());
        }

        
        try (FileWriter fw = new FileWriter(UTILITIES_FILE, false);
        // overwrites txt file with updated list
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (String outputLine : retainedLines) {
                bw.write(outputLine);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error during file structural update sync: " + e.getMessage());
        }
    }

    // First Login screen
    private JPanel createLoginScreen() {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(new Color(74, 74, 74));

        JPanel loginCard = new JPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBackground(new Color(28, 28, 28));
        loginCard.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        loginCard.setPreferredSize(new Dimension(550, 400));

        JLabel lblLogin = new JLabel("LOGIN");
        lblLogin.setFont(new Font("Arial", Font.BOLD, 50));
        lblLogin.setForeground(Color.WHITE);
        lblLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel inputGridPanel = new JPanel(new GridLayout(2, 2, 40, 10));
        inputGridPanel.setBackground(new Color(28, 28, 28));
        inputGridPanel.setMaximumSize(new Dimension(460, 100));

        JLabel lblUser = new JLabel("Username", JLabel.CENTER);
        lblUser.setFont(new Font("Arial", Font.BOLD, 18));
        lblUser.setForeground(Color.WHITE);
        
        JTextField txtUser = new JTextField();
        txtUser.setBackground(new Color(85, 85, 85));
        txtUser.setForeground(Color.WHITE);

        JLabel lblPass = new JLabel("Password", JLabel.CENTER);
        lblPass.setFont(new Font("Arial", Font.BOLD, 18));
        lblPass.setForeground(Color.WHITE);
        
        JPasswordField txtPass = new JPasswordField();
        txtPass.setBackground(new Color(85, 85, 85));
        txtPass.setForeground(Color.WHITE);

        inputGridPanel.add(lblUser);
        inputGridPanel.add(lblPass);
        inputGridPanel.add(txtUser);
        inputGridPanel.add(txtPass);

        JButton btnEnter = new JButton("Enter");
        btnEnter.setFont(new Font("Arial", Font.BOLD, 22));
        btnEnter.setBackground(new Color(85, 85, 85));
        btnEnter.setForeground(Color.BLACK);
        btnEnter.setFocusPainted(false);
        btnEnter.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnEnter.addActionListener(e -> {

            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());

            if (AccountManager.validateLogin(username, password)) {

                 String logEntry =
                    "User Activity: '" + username + "' authenticated successfully.\n";

                FileManager.saveRecordToFile("LOGIN", logEntry);

                cardLayout.show(mainContainer, "DASHBOARD");

            } else {

                JOptionPane.showMessageDialog(
                    this,
                    "Invalid Username or Password",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
                    );
            }
        });

        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(28, 28, 28));

        JLabel lblNoAccount = new JLabel("No account?");
        lblNoAccount.setForeground(Color.WHITE);

        JButton btnCreateAccount = new JButton("Create Here");
        btnCreateAccount.setForeground(new Color(135, 206, 235));
        btnCreateAccount.setBorderPainted(false);
        btnCreateAccount.setContentAreaFilled(false);
        btnCreateAccount.setFocusPainted(false);
        btnCreateAccount.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnCreateAccount.addActionListener(e -> showCreateAccountDialog());

        footerPanel.add(lblNoAccount);
        footerPanel.add(btnCreateAccount);

        loginCard.add(lblLogin);
        loginCard.add(Box.createVerticalStrut(40));
        loginCard.add(inputGridPanel);
        loginCard.add(Box.createVerticalStrut(40));
        loginCard.add(btnEnter);
        loginCard.add(Box.createVerticalStrut(30));
        loginCard.add(footerPanel);

        outerPanel.add(loginCard);
        return outerPanel;

    }

    // MAIN DASHBOARD
    private JPanel createDashboardScreen() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(90, 15, 230));
        sidebar.setPreferredSize(new Dimension(250, 600));
        sidebar.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JLabel lblAdmin = new JLabel("Welcome Admin!");
        lblAdmin.setFont(new Font("Arial", Font.BOLD, 24));
        lblAdmin.setForeground(Color.WHITE);
        lblAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblAdmin);
        sidebar.add(Box.createVerticalStrut(40));

        JButton btnTenants = new JButton("Tenants");
        JButton btnPayments = new JButton("Payments");
        JButton btnRooms = new JButton("Rooms");
        JButton btnUtilities = new JButton("Utilities");

        JButton[] menuItems = {btnTenants, btnPayments, btnRooms, btnUtilities};
        for (JButton btnMenu : menuItems) {
            btnMenu.setFont(new Font("Arial", Font.PLAIN, 22));
            btnMenu.setBackground(new Color(74, 74, 74));
            btnMenu.setForeground(Color.BLACK);
            btnMenu.setFocusPainted(false);
            btnMenu.setMaximumSize(new Dimension(180, 40));
            btnMenu.setPreferredSize(new Dimension(180, 40));
            btnMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebar.add(btnMenu);
            sidebar.add(Box.createVerticalStrut(15));
        }

        btnTenants.addActionListener(e -> cardLayout.show(mainContainer, "TENANTS"));
        btnRooms.addActionListener(e -> cardLayout.show(mainContainer, "ROOMS"));
        btnPayments.addActionListener(e -> {
            new ApartmentManagementSystem().setVisible(true);

            JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(btnPayments);
            currentFrame.dispose();
        });
        btnUtilities.addActionListener(e -> cardLayout.show(mainContainer, "UTILITIES"));
        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Arial", Font.PLAIN, 16));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));
        sidebar.add(btnLogout);

        JPanel contentArea = new JPanel(new GridBagLayout());
        contentArea.setBackground(new Color(85, 85, 85));

        JPanel logoContainer = new JPanel();
        logoContainer.setLayout(new BoxLayout(logoContainer, BoxLayout.Y_AXIS));
        logoContainer.setOpaque(false);

        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(300, 280));
        imageLabel.setBackground(new Color(210, 235, 250));
        imageLabel.setOpaque(true);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // image section for the dashboard
        String imgPath = "placeholder3.png"; 
        java.io.File imgFile = new java.io.File(imgPath);

        if (imgFile.exists()) {
            ImageIcon originalIcon = new ImageIcon(imgPath);
            // scale image down to the same size as our window, pero still small dimensions ang png
            java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(300, 280, java.awt.Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        }

        JLabel lblDormName = new JLabel("Dorm away from Home", SwingConstants.CENTER);
        lblDormName.setFont(new Font("Arial", Font.PLAIN, 18));
        lblDormName.setForeground(Color.WHITE);
        lblDormName.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoContainer.add(imageLabel);
        logoContainer.add(Box.createVerticalStrut(15));
        logoContainer.add(lblDormName);

        contentArea.add(logoContainer);

        dashboardPanel.add(sidebar, BorderLayout.WEST);
        dashboardPanel.add(contentArea, BorderLayout.CENTER);

        return dashboardPanel;
    }

    // Tenants Page info
    private JPanel createTenantsScreen() {
        JPanel tenantsPanel = new JPanel(new BorderLayout());
        tenantsPanel.setBackground(new Color(85, 85, 85));

        JPanel topNavbar = new JPanel(new BorderLayout());
        topNavbar.setBackground(new Color(90, 15, 230));
        topNavbar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton btnBackToDashboard = new JButton("☰");
        btnBackToDashboard.setFont(new Font("Arial", Font.BOLD, 22));
        btnBackToDashboard.setForeground(Color.WHITE);
        btnBackToDashboard.setContentAreaFilled(false);
        btnBackToDashboard.setBorderPainted(false);
        btnBackToDashboard.setBorder(new EmptyBorder(0, 10, 0, 0));
        btnBackToDashboard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBackToDashboard.addActionListener(e -> cardLayout.show(mainContainer, "DASHBOARD"));

        JLabel lblTitle = new JLabel("Tenant Information Management", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setBorder(new EmptyBorder(0, -20, 0, 0));
        lblTitle.setForeground(Color.WHITE);

        // JLabel lblGuide = new JLabel("Tenant Name  |  Contact Number  |  Room Number", JLabel.CENTER);
        // lblGuide.setFont(new Font("Arial", Font.PLAIN, 14));
        // lblGuide.setForeground(Color.WHITE);
        // lblGuide.setBorder(new EmptyBorder(0, -20, 0, 0));

        topNavbar.add(btnBackToDashboard, BorderLayout.WEST);
        topNavbar.add(lblTitle, BorderLayout.CENTER);

        JPanel internalContent = new JPanel();
        internalContent.setLayout(new BoxLayout(internalContent, BoxLayout.Y_AXIS));
        internalContent.setBackground(new Color(85, 85, 85));
        internalContent.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionButtonPanel.setOpaque(false);

        JButton btnOpenModal = new JButton("Add Tenant");
        btnOpenModal.setFont(new Font("Arial", Font.BOLD, 16));
        btnOpenModal.setBackground(new Color(90, 15, 230));
        btnOpenModal.setForeground(Color.BLACK);
        btnOpenModal.setFocusPainted(false);
        btnOpenModal.addActionListener(e -> showTenantInput());

        JButton btnDeleteSelection = new JButton("Delete Selection");
        btnDeleteSelection.setFont(new Font("Arial", Font.BOLD, 16));
        btnDeleteSelection.setBackground(new Color(180, 20, 20));
        btnDeleteSelection.setForeground(Color.BLACK);
        btnDeleteSelection.setFocusPainted(false);
        

        btnDeleteSelection.addActionListener(e -> {
            int selectedIndex = tenantList.getSelectedIndex();
            if (selectedIndex != -1) {
                int confirmation = JOptionPane.showConfirmDialog(
                    this, "Are you sure you want to delete this Tenant Info?", 
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION
                );
                if (confirmation == JOptionPane.YES_OPTION) {
                    listModel.remove(selectedIndex);
                    rewriteFileFromList();         
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an active tenant from the list to delete", "Notice", JOptionPane.WARNING_MESSAGE);
            }
        });

        actionButtonPanel.add(btnOpenModal);
        actionButtonPanel.add(btnDeleteSelection);

        // tenant info list
        listModel = new DefaultListModel<>();
        tenantList = new JList<>(listModel);
        tenantList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        tenantList.setBackground(new Color(28, 28, 28));
        tenantList.setForeground(Color.WHITE);
        tenantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tenantList);
        scrollPane.setPreferredSize(new Dimension(650, 300));
        scrollPane.setMaximumSize(new Dimension(650, 300));
        internalContent.add(actionButtonPanel);
        internalContent.add(Box.createVerticalStrut(20));
        internalContent.add(scrollPane);
        tenantsPanel.add(topNavbar, BorderLayout.NORTH);
        tenantsPanel.add(internalContent, BorderLayout.CENTER);
        return tenantsPanel;
    }

    // jdialog sections tenant
    // pop up for tenant input
    private void showTenantInput() {
        JDialog modalDialog = new JDialog(this, "Enter Tenant Information", true);
        modalDialog.setSize(380, 250);
        modalDialog.setLocationRelativeTo(this);
        modalDialog.setLayout(new BorderLayout(10, 10));
        
        JPanel formGrid = new JPanel(new GridLayout(4, 2, 10, 10));
        formGrid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblName = new JLabel("Full Name:");
        JTextField txtName = new JTextField();
        JLabel lblRoom = new JLabel("Room Assigned:");
        JTextField txtRoom = new JTextField();
        JLabel lblContact = new JLabel("Contact Number:");
        JTextField txtContact = new JTextField();
        JLabel lblLeaseStart = new JLabel("Lease Start:");
        JTextField txtLeaseStart = new JTextField();

        formGrid.add(lblName); formGrid.add(txtName);
        formGrid.add(lblRoom); formGrid.add(txtRoom);
        formGrid.add(lblContact); formGrid.add(txtContact);
        formGrid.add(lblLeaseStart); formGrid.add(txtLeaseStart);
        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Cancel");
        JButton btnSave = new JButton("Save Record");
        btnSave.setBackground(new Color(90, 15, 230));
        btnSave.setForeground(Color.BLACK);

        btnCancel.addActionListener(e -> modalDialog.dispose());
        
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String room = txtRoom.getText().trim();
            String contact = txtContact.getText().trim();
            String leaseStart = txtLeaseStart.getText().trim();

            if (!name.isEmpty() && !room.isEmpty()) {
                listModel.addElement("Tenant Name: " + name + "  |  Room: " + room + "  |  Contact: " + contact + "  |  Lease Start: " + leaseStart);
                listModel.addElement("");

                String fileFormatData = String.format("Name: %s\nRoom: %s\nContact: %s\nLease Start: %s\n", name, room, contact, leaseStart);
                FileManager.saveRecordToFile("TENANT", fileFormatData);
                
                modalDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(modalDialog, "Name and Room entries cannot be blank", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionControls.add(btnCancel);
        actionControls.add(btnSave);

        modalDialog.add(formGrid, BorderLayout.CENTER);
        modalDialog.add(actionControls, BorderLayout.SOUTH);
        
        modalDialog.setVisible(true);
    }

    // room monitoring
    private JPanel createRoomsScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel topNavbar = new JPanel(new BorderLayout());
        topNavbar.setBackground(new Color(90, 15, 230));
        topNavbar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton btnBackToDashboard = new JButton("☰");
        btnBackToDashboard.setFont(new Font("Arial", Font.BOLD, 22));
        btnBackToDashboard.setForeground(Color.WHITE);
        btnBackToDashboard.setContentAreaFilled(false);
        btnBackToDashboard.setBorderPainted(false);
        btnBackToDashboard.setBorder(new EmptyBorder(0, 10, 0, 0));
        btnBackToDashboard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBackToDashboard.addActionListener(e -> cardLayout.show(mainContainer, "DASHBOARD"));

        JLabel lblTitle = new JLabel("Room Monitoring", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setBorder(new EmptyBorder(0, -20, 0, 0));
        lblTitle.setForeground(Color.WHITE);

        topNavbar.add(btnBackToDashboard, BorderLayout.WEST);
        topNavbar.add(lblTitle, BorderLayout.CENTER);
        panel.add(topNavbar, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG_DARK_GRAY);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        JPanel gridPanel = new JPanel(new GridLayout(5, 3, 15, 15));
        gridPanel.setBackground(BG_DARK_GRAY);

        // Load existing saved states from file
        ArrayList<String> savedStates = FileManager.loadRoomStates();

        for (int i = 1; i <= 15; i++) {
            String roomName = "Room " + i;
            JButton roomBtn = new JButton(roomName);
            roomBtn.setFont(new Font("Arial", Font.BOLD, 14));
            roomBtn.setForeground(Color.WHITE);
            roomBtn.setFocusPainted(false);
            roomBtn.setOpaque(true);
            roomBtn.setBorderPainted(false);
            roomBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // determine historical state or fallback to default pattern if file is fresh
            String assignedState = "";
            for (String stateEntry : savedStates) {
                if (stateEntry.startsWith(roomName + ":")) {
                    assignedState = stateEntry.split(":")[1];
                    break;
                }
            }

            // apply state and color configurations
            if (!assignedState.isEmpty()) {
                if ("OCCUPIED".equals(assignedState)) {
                    roomBtn.setBackground(CANVA_RED);
                    roomBtn.setActionCommand("OCCUPIED");
                } else {
                    roomBtn.setBackground(CANVA_GREEN);
                    roomBtn.setActionCommand("VACANT");
                }
            } else {
                if (i % 2 == 0) {
                    roomBtn.setBackground(CANVA_RED);
                    roomBtn.setActionCommand("OCCUPIED");
                } else {
                    roomBtn.setBackground(CANVA_GREEN);
                    roomBtn.setActionCommand("VACANT");
                }
            }
            roomBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton target = (JButton) e.getSource();
                    if ("VACANT".equals(target.getActionCommand())) {
                        target.setBackground(CANVA_RED);
                        target.setActionCommand("OCCUPIED");
                    } else {
                        target.setBackground(CANVA_GREEN);
                        target.setActionCommand("VACANT");
                    }
                    
                    // automatically save the updated configuration layout right away
                    saveRoomStates(gridPanel);
                }
            });

            gridPanel.add(roomBtn);
        }

        mainContent.add(gridPanel, BorderLayout.CENTER);
        panel.add(mainContent, BorderLayout.CENTER);

        return panel;
    }

    private void saveRoomStates(JPanel gridPanel) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("room_states.txt", false))) {
            for (Component comp : gridPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    bw.write(btn.getText() + ":" + btn.getActionCommand());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving room states: " + e.getMessage());
        }
    }

    // Utilities Page info
    private JPanel createUtilitiesScreen() {
        JPanel utilitiesPanel = new JPanel(new BorderLayout());
        utilitiesPanel.setBackground(new Color(85, 85, 85));

        JPanel topNavbar = new JPanel(new BorderLayout());
        topNavbar.setBackground(new Color(90, 15, 230));
        topNavbar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton btnBackToDashboard = new JButton("☰");
        btnBackToDashboard.setFont(new Font("Arial", Font.BOLD, 22));
        btnBackToDashboard.setForeground(Color.WHITE);
        btnBackToDashboard.setContentAreaFilled(false);
        btnBackToDashboard.setBorderPainted(false);
        btnBackToDashboard.setBorder(new EmptyBorder(0, 10, 0, 0));
        btnBackToDashboard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBackToDashboard.addActionListener(e -> cardLayout.show(mainContainer, "DASHBOARD"));

        JLabel lblTitle = new JLabel("Tenant Utilities Management", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setBorder(new EmptyBorder(0, -20, 0, 0));
        lblTitle.setForeground(Color.WHITE);

        topNavbar.add(btnBackToDashboard, BorderLayout.WEST);
        topNavbar.add(lblTitle, BorderLayout.CENTER);

        JPanel internalContent = new JPanel();
        internalContent.setLayout(new BoxLayout(internalContent, BoxLayout.Y_AXIS));
        internalContent.setBackground(new Color(85, 85, 85));
        internalContent.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionButtonPanel.setOpaque(false);

        JButton btnOpenModal = new JButton("Add Utility Info");
        btnOpenModal.setFont(new Font("Arial", Font.BOLD, 16));
        btnOpenModal.setBackground(new Color(90, 15, 230));
        btnOpenModal.setForeground(Color.BLACK);
        btnOpenModal.setFocusPainted(false);
        btnOpenModal.addActionListener(e -> showUtilitiesInput());

        JButton btnDeleteSelection = new JButton("Delete Selection");
        btnDeleteSelection.setFont(new Font("Arial", Font.BOLD, 16));
        btnDeleteSelection.setBackground(new Color(180, 20, 20));
        btnDeleteSelection.setForeground(Color.BLACK);
        btnDeleteSelection.setFocusPainted(false);
        

        btnDeleteSelection.addActionListener(e -> {
            int selectedIndex = utilityList.getSelectedIndex();
            if (selectedIndex != -1) {
                int confirmation = JOptionPane.showConfirmDialog(
                    this, "Are you sure you want to delete this Utility Info?", 
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION
                );
                if (confirmation == JOptionPane.YES_OPTION) {
                    utilityListModel.remove(selectedIndex);
                    rewriteUtilityFileFromList();         
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an active utility from the list to delete", "Notice", JOptionPane.WARNING_MESSAGE);
            }
        });

        actionButtonPanel.add(btnOpenModal);
        actionButtonPanel.add(btnDeleteSelection);

        // utility info list
        utilityListModel = new DefaultListModel<>();
        utilityList = new JList<>(utilityListModel);
        utilityList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        utilityList.setBackground(new Color(28, 28, 28));
        utilityList.setForeground(Color.WHITE);
        utilityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(utilityList);
        scrollPane.setPreferredSize(new Dimension(650, 300));
        scrollPane.setMaximumSize(new Dimension(650, 300));
        internalContent.add(actionButtonPanel);
        internalContent.add(Box.createVerticalStrut(20));
        internalContent.add(scrollPane);
        utilitiesPanel.add(topNavbar, BorderLayout.NORTH);
        utilitiesPanel.add(internalContent, BorderLayout.CENTER);
        return utilitiesPanel;
    }

    // jdialog sections Utility
    // pop up for tenant input
    private void showUtilitiesInput() {
        JDialog modalDialog = new JDialog(this, "Enter Utility Information", true);
        modalDialog.setSize(400, 300);
        modalDialog.setLocationRelativeTo(this);
        modalDialog.setLayout(new BorderLayout(10, 10));
        
        JPanel formGrid = new JPanel(new GridLayout(6, 2, 10, 10));
        formGrid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblStatus = new JLabel("Status:");
        String[] statusOptions = {"PAID", "UNPAID", "PARTIAL"};
        JComboBox<String> cmbStatus = new JComboBox<>(statusOptions);
        JLabel lblTenant = new JLabel("Tenant:");
        JTextField txtTenant = new JTextField();
        JLabel lblElectricity = new JLabel("Electricity:");
        JTextField txtElectricity = new JTextField();
        JLabel lblWater = new JLabel("Water:");
        JTextField txtWater = new JTextField();
        JLabel lblOther = new JLabel("Other:");
        JTextField txtOther = new JTextField();
        JLabel lblTotalText = new JLabel("Total Amount:");
        JLabel lblTotalValue = new JLabel("₱0.00");
        lblTotalValue.setFont(new Font("Arial", Font.BOLD, 14));

        formGrid.add(lblStatus); formGrid.add(cmbStatus);
        formGrid.add(lblTenant); formGrid.add(txtTenant);
        formGrid.add(lblElectricity); formGrid.add(txtElectricity);
        formGrid.add(lblWater); formGrid.add(txtWater);
        formGrid.add(lblOther); formGrid.add(txtOther);
        formGrid.add(lblTotalText); formGrid.add(lblTotalValue);

        DocumentListener totalCalculator = new DocumentListener() {
            private void calculateTotal() {
                try {
                    double electricity = txtElectricity.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtElectricity.getText().trim());
                    double water = txtWater.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtWater.getText().trim());
                    double other = txtOther.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtOther.getText().trim());
                    
                    double total = electricity + water + other;
                    lblTotalValue.setText(String.format("%.2f", total));
                } catch (NumberFormatException ex) {
                    lblTotalValue.setText("Invalid Input"); // Displays if user types letters
                }
            }

            @Override public void insertUpdate(DocumentEvent e) { calculateTotal(); }
            @Override public void removeUpdate(DocumentEvent e) { calculateTotal(); }
            @Override public void changedUpdate(DocumentEvent e) { calculateTotal(); }
        };

        txtElectricity.getDocument().addDocumentListener(totalCalculator);
        txtWater.getDocument().addDocumentListener(totalCalculator);
        txtOther.getDocument().addDocumentListener(totalCalculator);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Cancel");
        JButton btnSave = new JButton("Save Record");
        btnSave.setBackground(new Color(90, 15, 230));
        btnSave.setForeground(Color.BLACK);

        btnCancel.addActionListener(e -> modalDialog.dispose());
        
        btnSave.addActionListener(e -> {
            String status = (String) cmbStatus.getSelectedItem();
            String tenant = txtTenant.getText().trim();
            String electricity = txtElectricity.getText().trim();
            String water = txtWater.getText().trim();
            String other = txtOther.getText().trim();

            if (!status.isEmpty() && !electricity.isEmpty() && !tenant.isEmpty() && !water.isEmpty() && !other.isEmpty()) {
                utilityListModel.addElement("Status: " + status + "  |  Tenant: " + tenant + "  |  Electricity: " + electricity + "  |  Water: " + water + "  |  Other: " + other);
                utilityListModel.addElement("");

                String fileFormatData = String.format("Status: %s\nTenant: %s\nElectricity: %s\nWater: %s\nOther: %s\n", status, tenant, electricity, water, other);
                FileManager.saveUtilityToFile("UTILITIES", fileFormatData);
                
                modalDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(modalDialog, "All fields must be filled out", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionControls.add(btnCancel);
        actionControls.add(btnSave);

        modalDialog.add(formGrid, BorderLayout.CENTER);
        modalDialog.add(actionControls, BorderLayout.SOUTH);
        
        modalDialog.setVisible(true);
    }

    //for creating new account but need admin key, change sa top
    private void showCreateAccountDialog() {

        JDialog dialog = new JDialog(this, "Create Account", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JPasswordField txtConfirmPassword = new JPasswordField();
        JPasswordField txtAdminKey = new JPasswordField();

        formPanel.add(new JLabel("Username:"));
        formPanel.add(txtUsername);

        formPanel.add(new JLabel("Password:"));
        formPanel.add(txtPassword);

        formPanel.add(new JLabel("Confirm Password:"));
        formPanel.add(txtConfirmPassword);

        formPanel.add(new JLabel("Admin Key:"));
        formPanel.add(txtAdminKey);

        JPanel buttonPanel = new JPanel();

        JButton btnSave = new JButton("Create");
        JButton btnCancel = new JButton("Cancel");

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {

            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String confirm = new String(txtConfirmPassword.getPassword());
            String adminKey = new String(txtAdminKey.getPassword());

            if (username.isEmpty() || password.isEmpty()) {

                JOptionPane.showMessageDialog(
                        dialog,
                        "Username and Password cannot be empty."
                );
                return;
            }

            if (!password.equals(confirm)) {

                JOptionPane.showMessageDialog(
                        dialog,
                        "Passwords do not match."
                );
                return;
            }

            if (!adminKey.equals(ADMIN_CREATION_KEY)) {

                JOptionPane.showMessageDialog(
                        dialog,
                        "Invalid Admin Key.",
                        "Access Denied",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            AccountManager.saveAccount(username, password);

            JOptionPane.showMessageDialog(
                    dialog,
                    "Account Created Successfully!"
            );

            dialog.dispose();
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
    public static void main(String[] args) {
        new AppDraft7(false);
    }
}
