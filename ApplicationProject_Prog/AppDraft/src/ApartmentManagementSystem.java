import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ApartmentManagementSystem extends JFrame {

    private static final Color DARK_GRAY = new Color(74, 74, 74);
    private static final Color PURPLE_NAV = new Color(102, 35, 234);
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final Color BOX_BG = Color.WHITE;
    
    private static final String FILE_PATH = "tenants_data.txt";

    private List<Tenant> tenants;
    private List<String> globalPaymentHistory;

    private JPanel unpaidContainer;
    private JPanel historyContainer;
    private DefaultListModel<String> tenantListModel;
    private JList<String> tenantJList;

    //additional
    private JPanel viewContainer;
    private CardLayout cardLayout;

    public ApartmentManagementSystem() {
        loadDataFromFile();

        setTitle("Dorm Management System");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(DARK_GRAY);
        setContentPane(mainPanel);

        JPanel leftNavBar = createLeftNavBar();
        mainPanel.add(leftNavBar, BorderLayout.WEST);

        JPanel centerDashboard = new JPanel(new BorderLayout());
        centerDashboard.setBackground(Color.WHITE);
        mainPanel.add(centerDashboard, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel("Payments", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(PURPLE_NAV);
        titleLabel.setBorder(new EmptyBorder(15, 0, 15, 0));
        centerDashboard.add(titleLabel, BorderLayout.NORTH);

        // JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 2, 0));
        // columnsPanel.setBackground(Color.WHITE);

        // JPanel unpaidColumn = createColumnPanel("Unpaid");
        // unpaidContainer = new JPanel();
        // unpaidContainer.setLayout(new BoxLayout(unpaidContainer, BoxLayout.Y_AXIS));
        // unpaidContainer.setBackground(DARK_GRAY);
        // JScrollPane unpaidScrollPane = createStyledScrollPane(unpaidContainer);
        // unpaidColumn.add(unpaidScrollPane, BorderLayout.CENTER);

        // JPanel historyColumn = createColumnPanel("History");
        // historyContainer = new JPanel();
        // historyContainer.setLayout(new BoxLayout(historyContainer, BoxLayout.Y_AXIS));
        // historyContainer.setBackground(DARK_GRAY);
        // JScrollPane historyScrollPane = createStyledScrollPane(historyContainer);
        // historyColumn.add(historyScrollPane, BorderLayout.CENTER);

        // columnsPanel.add(unpaidColumn);
        // columnsPanel.add(historyColumn);
        // centerDashboard.add(columnsPanel, BorderLayout.CENTER);

        cardLayout = new CardLayout();
        viewContainer = new JPanel(cardLayout);
        viewContainer.setBackground(DARK_GRAY);
        // create views for unpaid and history columns and add to card layout
        JPanel unpaidView = createSingleColumnView("Unpaid", unpaidContainer);
        JPanel historyView = createSingleColumnView("History", historyContainer);

        viewContainer.add(unpaidView, "UNPAID");
        viewContainer.add(historyView, "HISTORY");
        centerDashboard.add(viewContainer, BorderLayout.CENTER);
        refreshUIData();
    }

    // Creates a single column view panel with a scrollable container
    private JPanel createSingleColumnView(String title, JPanel clientContainer) {
        JPanel column = createColumnPanel(title);

        if (clientContainer == null) {
            clientContainer = new JPanel();
        }
        clientContainer.setLayout(new BoxLayout(clientContainer, BoxLayout.Y_AXIS));
        clientContainer.setBackground(DARK_GRAY);

        // assign back to the corresponding field so callers can reference it
        if ("Unpaid".equals(title)) {
            unpaidContainer = clientContainer;
        } else if ("History".equals(title)) {
            historyContainer = clientContainer;
        }

        JScrollPane scroll = createStyledScrollPane(clientContainer);
        column.add(scroll, BorderLayout.CENTER);
        return column;
    }

    private JPanel createLeftNavBar() {

        JPanel nav = new JPanel();
        nav.setBackground(PURPLE_NAV);
        nav.setPreferredSize(new Dimension(200, 0));
        nav.setLayout(new BorderLayout());

        // JLabel menuIcon = new JLabel("≡", SwingConstants.LEFT);
        // menuIcon.setFont(new Font("Arial", Font.BOLD, 36));
        // menuIcon.setForeground(TEXT_WHITE);
        // menuIcon.setBorder(new EmptyBorder(10, 20, 10, 0));
        // nav.add(menuIcon, BorderLayout.NORTH);

        JButton btnBackToDashboard = new JButton("☰");
        btnBackToDashboard.setFont(new Font("Arial", Font.BOLD, 22));
        btnBackToDashboard.setForeground(Color.WHITE);
        btnBackToDashboard.setContentAreaFilled(false);
        btnBackToDashboard.setBorderPainted(false);
        btnBackToDashboard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBackToDashboard.setFont(new Font("Arial", Font.BOLD, 36));
        btnBackToDashboard.setForeground(TEXT_WHITE);
        btnBackToDashboard.setBorder(new EmptyBorder(0, -160, 0, 0));
        nav.add(btnBackToDashboard, BorderLayout.NORTH);

        btnBackToDashboard.addActionListener(e -> {
            JFrame current = (JFrame) SwingUtilities.getWindowAncestor(nav);
            current.dispose();
            new AppDraft7(true).setVisible(true);
        });

        JPanel tenantCtrlPanel = new JPanel(new BorderLayout());
        tenantCtrlPanel.setOpaque(false);
        tenantCtrlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel tenantLabel = new JLabel("Active Units:", SwingConstants.LEFT);
        tenantLabel.setForeground(TEXT_WHITE);
        tenantLabel.setFont(new Font("Arial", Font.BOLD, 14));
        tenantCtrlPanel.add(tenantLabel, BorderLayout.NORTH);

        tenantListModel = new DefaultListModel<>();
        tenantJList = new JList<>(tenantListModel);
        tenantJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tenantListScroll = new JScrollPane(tenantJList);
        tenantListScroll.setPreferredSize(new Dimension(200, 300));
        tenantCtrlPanel.add(tenantListScroll, BorderLayout.CENTER);

        // JPanel btnActionGrid = new JPanel(new GridLayout(2, 1, 0, 8));
        // btnActionGrid.setOpaque(false);
        // btnActionGrid.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel btnActionGrid = new JPanel();
        btnActionGrid.setLayout(new BoxLayout(btnActionGrid, BoxLayout.Y_AXIS));
        btnActionGrid.setOpaque(false);
        btnActionGrid.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton addTenantBtn = new JButton("Add New Unit");
        addTenantBtn.addActionListener(this::handleClearanceToAddTenant);
        JButton removeTenantBtn = new JButton("Remove Selected");
        removeTenantBtn.addActionListener(this::handleTenantRemoval);
        JButton unpaidBtn = new JButton("Unpaid");
        unpaidBtn.addActionListener(e -> cardLayout.show(viewContainer, "UNPAID"));
        JButton historyBtn = new JButton("History");
        historyBtn.addActionListener(e -> cardLayout.show(viewContainer, "HISTORY"));

        Dimension btnSize = new Dimension(180, 40);
        addTenantBtn.setMaximumSize(btnSize);
        removeTenantBtn.setMaximumSize(btnSize);
        unpaidBtn.setMaximumSize(btnSize);
        historyBtn.setMaximumSize(btnSize);

        btnActionGrid.add(addTenantBtn);
        btnActionGrid.add(removeTenantBtn);
        btnActionGrid.add(unpaidBtn);
        btnActionGrid.add(historyBtn);
        tenantCtrlPanel.add(btnActionGrid, BorderLayout.SOUTH);

        nav.add(tenantCtrlPanel, BorderLayout.CENTER);
        return nav;
    }

    private JPanel createColumnPanel(String headerTitle) {
        JPanel column = new JPanel(new BorderLayout());
        column.setBackground(Color.WHITE);

        JLabel heading = new JLabel(headerTitle, SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.PLAIN, 20));
        heading.setForeground(DARK_GRAY);
        heading.setBorder(new EmptyBorder(10, 0, 15, 0));

        column.add(heading, BorderLayout.NORTH);
        return column;
    }

    private JScrollPane createStyledScrollPane(JPanel clientContainer) {
        JScrollPane pane = new JScrollPane(clientContainer);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        pane.getViewport().setBackground(DARK_GRAY);

        JScrollBar verticalBar = pane.getVerticalScrollBar();
        verticalBar.setBackground(DARK_GRAY);
        verticalBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(PURPLE_NAV);
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y, thumbBounds.width - 4, thumbBounds.height, 10, 10);
                g2.dispose();
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton jbtn = new JButton();
                jbtn.setPreferredSize(new Dimension(0, 0));
                return jbtn;
            }
        });
        return pane;
    }

    private void refreshUIData() {
        unpaidContainer.removeAll();
        historyContainer.removeAll();
        tenantListModel.clear();

        for (Tenant tenant : tenants) {
            tenantListModel.addElement(tenant.getUnitNumber());

            if (tenant.hasUnpaidBalance()) {
                JPanel card = createWhiteDataCard(tenant.getUnitNumber(), "Balance Due: ₱" + String.format("%.2f", tenant.getBalance()), true, tenant);
                unpaidContainer.add(card);
                unpaidContainer.add(Box.createVerticalStrut(12));
            }
        }

        for (String historyLog : globalPaymentHistory) {
            JPanel card = createWhiteDataCard(historyLog, "Transaction Recorded", false, null);
            historyContainer.add(card);
            historyContainer.add(Box.createVerticalStrut(12));
        }

        unpaidContainer.revalidate();
        unpaidContainer.repaint();
        historyContainer.revalidate();
        historyContainer.repaint();

        saveDataToFile();
    }

    private JPanel createWhiteDataCard(String mainText, String subText, boolean withPayButton, Tenant tenantRef) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BOX_BG);
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
        card.setPreferredSize(new Dimension(300, 70));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DARK_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JPanel textWrapper = new JPanel(new GridLayout(2, 1));
        textWrapper.setOpaque(false);

        JLabel mainLabel = new JLabel(mainText);
        mainLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainLabel.setForeground(Color.BLACK);

        JLabel subLabel = new JLabel(subText);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subLabel.setForeground(Color.DARK_GRAY);

        textWrapper.add(mainLabel);
        textWrapper.add(subLabel);
        card.add(textWrapper, BorderLayout.CENTER);

        if (withPayButton && tenantRef != null) {
            JButton payBtn = new JButton("UPDATE");
            payBtn.setBackground(PURPLE_NAV);
            payBtn.setForeground(Color.BLACK);
            payBtn.addActionListener(e -> handleProcessPayment(tenantRef));
            card.add(payBtn, BorderLayout.EAST);
        }

        return card;
    }

    private void handleClearanceToAddTenant(ActionEvent e) {
        JTextField unitField = new JTextField();
        JTextField balanceField = new JTextField("0.00");
        Object[] fields = {
                "Apartment / Unit Number:", unitField,
                "Outstanding Balance (PHP ₱):", balanceField
                };

        int option = JOptionPane.showConfirmDialog(this, fields, "Register New Unit Data", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION && !unitField.getText().trim().isEmpty()) {
            try {
                double startingBalance = Double.parseDouble(balanceField.getText().trim());
                tenants.add(new Tenant(unitField.getText().trim(), startingBalance, startingBalance > 0));
                refreshUIData();
            } catch (NumberFormatException error) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric value for the balance.", "Invalid Format", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleTenantRemoval(ActionEvent e) {
        int selectedIdx = tenantJList.getSelectedIndex();
        if (selectedIdx != -1) {
            tenants.remove(selectedIdx);
            refreshUIData();
        } else {
            JOptionPane.showMessageDialog(this, "Select a unit ID from the left panel listing to remove.", "Selection Needed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleProcessPayment(Tenant tenant) {
        String inputAmount = JOptionPane.showInputDialog(this, "Process collection payment for " + tenant.getUnitNumber() + ":", tenant.getBalance());
        if (inputAmount != null && !inputAmount.trim().isEmpty()) {
            try {
                double payment = Double.parseDouble(inputAmount.trim());
                if (payment <= 0 || payment > tenant.getBalance()) {
                    JOptionPane.showMessageDialog(this, "Invalid payment transaction amount context configuration requirements.", "Processing Refused", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                tenant.clearBalance(payment);
                globalPaymentHistory.add(0, tenant.getUnitNumber() + " paid ₱" + String.format("%.2f", payment));
                refreshUIData();
            } catch (NumberFormatException err) {
                JOptionPane.showMessageDialog(this, "Numeric balance figures input constraints only.", "Error Processing Payment", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveDataToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            writer.println("---TENANTS_START---");
            for (Tenant t : tenants) {
                writer.println(t.getUnitNumber() + "," + t.getBalance() + "," + t.hasUnpaidBalance());
            }
            writer.println("---HISTORY_START---");
            for (String log : globalPaymentHistory) {
                writer.println(log);
            }
        } catch (IOException e) {
            System.err.println("Could not save tenant metadata configurations: " + e.getMessage());
        }
    }

    private void loadDataFromFile() {
        tenants = new ArrayList<>();
        globalPaymentHistory = new ArrayList<>();
        File dataFile = new File(FILE_PATH);

        if (!dataFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            String mode = "";
            while ((line = reader.readLine()) != null) {
                if (line.equals("---TENANTS_START---")) {
                    mode = "TENANTS";
                    continue;
                } else if (line.equals("---HISTORY_START---")) {
                    mode = "HISTORY";
                    continue;
                }

                if (mode.equals("TENANTS")) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        String unit = parts[0];
                        double balance = Double.parseDouble(parts[1]);
                        boolean isUnpaid = Boolean.parseBoolean(parts[2]);
                        tenants.add(new Tenant(unit, balance, isUnpaid));
                    }
                } else if (mode.equals("HISTORY")) {
                    globalPaymentHistory.add(line);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading structural file records profile details; creating fresh defaults.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ApartmentManagementSystem().setVisible(true));
    }

    private static class Tenant {
        private final String unitNumber;
        private double unpaidBalance;
        private boolean hasUnpaid;

        public Tenant(String unitNumber, double balance, boolean outstandingIndicator) {
            this.unitNumber = unitNumber;
            this.unpaidBalance = balance;
            this.hasUnpaid = outstandingIndicator;
        }

        public String getUnitNumber() { return unitNumber; }
        public double getBalance() { return unpaidBalance; }
        public boolean hasUnpaidBalance() { return hasUnpaid && unpaidBalance > 0; }

        public void clearBalance(double paymentAmount) {
            this.unpaidBalance -= paymentAmount;
            if (this.unpaidBalance <= 0) {
                this.unpaidBalance = 0;
                this.hasUnpaid = false;
            }
        }
    }
}