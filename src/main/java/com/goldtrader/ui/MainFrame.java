package com.goldtrader.ui;

import com.goldtrader.service.AuthService;
import com.goldtrader.ui.panels.*;
import com.goldtrader.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private BuyGoldPanel buyGoldPanel;
    private SellGoldPanel sellGoldPanel;
    private InventoryPanel inventoryPanel;
    private CustomerPanel customerPanel;
    private TransactionPanel transactionPanel;
    private ReportsPanel reportsPanel;
    private GoldPricePanel goldPricePanel;

    public MainFrame() {
        setTitle("Gold Trading Management System v1.0");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    authService.logout();
                    System.exit(0);
                }
            }
        });

        initComponents();
    }

    private void initComponents() {
        // Menu bar
        setJMenuBar(createMenuBar());

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        JLabel userLabel = new JLabel("Logged in as: " + SessionManager.getInstance().getCurrentUser().getFullName()
                + " (" + SessionManager.getInstance().getCurrentUser().getRole() + ")");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.add(userLabel, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("Gold Trader v1.0.0");
        versionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusBar.add(versionLabel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        dashboardPanel = new DashboardPanel();
        buyGoldPanel = new BuyGoldPanel();
        sellGoldPanel = new SellGoldPanel();
        inventoryPanel = new InventoryPanel();
        customerPanel = new CustomerPanel();
        transactionPanel = new TransactionPanel();
        reportsPanel = new ReportsPanel();
        goldPricePanel = new GoldPricePanel();

        // Wire up callbacks for refreshing after transactions
        Runnable refreshAll = () -> {
            dashboardPanel.refreshData();
            inventoryPanel.refreshData();
            transactionPanel.refreshData();
            sellGoldPanel.refreshCustomers();
            buyGoldPanel.refreshCustomers();
        };
        buyGoldPanel.setOnTransactionComplete(refreshAll);
        sellGoldPanel.setOnTransactionComplete(refreshAll);

        tabbedPane.addTab("  Dashboard  ", createTabIcon(new Color(255, 215, 0)), dashboardPanel);
        tabbedPane.addTab("  Buy Gold  ", createTabIcon(new Color(46, 125, 50)), buyGoldPanel);
        tabbedPane.addTab("  Sell Gold  ", createTabIcon(new Color(198, 40, 40)), sellGoldPanel);
        tabbedPane.addTab("  Inventory  ", createTabIcon(new Color(21, 101, 192)), inventoryPanel);
        tabbedPane.addTab("  Customers  ", createTabIcon(new Color(142, 36, 170)), customerPanel);
        tabbedPane.addTab("  Transactions  ", createTabIcon(new Color(230, 81, 0)), transactionPanel);
        tabbedPane.addTab("  Reports  ", createTabIcon(new Color(0, 121, 107)), reportsPanel);
        tabbedPane.addTab("  Gold Prices  ", createTabIcon(new Color(255, 179, 0)), goldPricePanel);

        // Refresh panels when switching tabs
        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            switch (idx) {
                case 0 -> dashboardPanel.refreshData();
                case 3 -> inventoryPanel.refreshData();
                case 4 -> customerPanel.refreshData();
                case 5 -> transactionPanel.refreshData();
                case 7 -> goldPricePanel.refreshData();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> {
            authService.logout();
            dispose();
            // Re-show login
            SwingUtilities.invokeLater(() -> {
                LoginDialog login = new LoginDialog(null);
                login.setVisible(true);
                if (login.isAuthenticated()) {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                } else {
                    System.exit(0);
                }
            });
        });
        fileMenu.add(logoutItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            authService.logout();
            System.exit(0);
        });
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Gold Trading Management System v1.0.0\n\n" +
                "A professional gold buying and selling management application.\n" +
                "Features: Buy/Sell Gold, Inventory Management, Customer Management,\n" +
                "Transaction History, Reports & Analytics, Gold Price Management.\n\n" +
                "Built with Java Swing, H2 Database, and FlatLaf.",
                "About Gold Trader", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        return menuBar;
    }

    private Icon createTabIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRoundRect(x, y, 12, 12, 4, 4);
            }

            @Override
            public int getIconWidth() { return 12; }

            @Override
            public int getIconHeight() { return 12; }
        };
    }
}
