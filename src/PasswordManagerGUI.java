import javax.swing.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

public class PasswordManagerGUI extends JFrame {

    private static final String ALGORITHM = "AES";
    private static final byte[] keyValue =
            new byte[]{'T','h','e','B','e','s','t','S','e','c','r','e','t','K','e','y'};
    private static final String FILE_NAME = "passwords.txt";

    private Map<String, String> passwordStore = new LinkedHashMap<>();

    private JTextField addSiteField, retrieveSiteField;
    private JPasswordField addPasswordField;
    private JTextArea outputArea;
    private JTable table;
    private DefaultTableModel tableModel;

    public PasswordManagerGUI() {
        setTitle("🔐 Password Manager (AES Encrypted)");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(40,40,45));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        // Add Password Section
        JPanel addPanel = createSectionPanel("➕ Add New Password", createAddPanel());
        // Retrieve Section
        JPanel retrievePanel = createSectionPanel("🔍 Retrieve Password", createRetrievePanel());

        topPanel.add(addPanel);
        topPanel.add(Box.createVerticalStrut(15));
        topPanel.add(retrievePanel);

        // Table
        tableModel = new DefaultTableModel(new Object[]{"Site", "Encrypted Password"}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setBackground(new Color(25,25,25));
        table.setForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(60,60,65));
        table.getTableHeader().setForeground(Color.WHITE);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "🗂 Stored Passwords",
                0, 0, null, Color.WHITE
        ));
        tableScroll.getViewport().setBackground(new Color(30,30,30));

        // Output Area
        outputArea = new JTextArea(4, 20);
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(20,20,20));
        outputArea.setForeground(Color.GREEN);
        outputArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "📋 Messages / Output",
                0, 0, null, Color.WHITE
        ));

        add(topPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(outputScroll, BorderLayout.SOUTH);

        loadPasswords();
        cleanDuplicateFile();
        refreshTable();
    }

    private JPanel createSectionPanel(String title, JPanel content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(50,50,55));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                title, 0, 0, null, Color.WHITE
        ));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(50,50,55));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel siteLabel = new JLabel("Website / App:");
        siteLabel.setForeground(Color.WHITE);
        addSiteField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        addPasswordField = new JPasswordField(20);
        addPasswordField.setEchoChar('•');

        JButton eyeBtn = new JButton("👁");
        eyeBtn.addActionListener(e -> togglePassword(addPasswordField));

        JButton addBtn = new JButton("✅ Add Password");
        styleButton(addBtn, new Color(46, 204, 113));
        addBtn.addActionListener(e -> addPassword());

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(siteLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(addSiteField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(passLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1;
        panel.add(addPasswordField, gbc);
        gbc.gridx = 2;
        panel.add(eyeBtn, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(addBtn, gbc);

        return panel;
    }

    private JPanel createRetrievePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(55,55,60));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel siteLabel = new JLabel("Website / App:");
        siteLabel.setForeground(Color.WHITE);
        retrieveSiteField = new JTextField(20);

        JButton getBtn = new JButton("🔍 Retrieve Password");
        styleButton(getBtn, new Color(52, 152, 219));
        getBtn.addActionListener(e -> retrievePassword());

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(siteLabel, gbc);
        gbc.gridx = 1;
        panel.add(retrieveSiteField, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(getBtn, gbc);

        return panel;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(180, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void togglePassword(JPasswordField field) {
        if (field.getEchoChar() == '•') field.setEchoChar((char) 0);
        else field.setEchoChar('•');
    }

    private void addPassword() {
        try {
            String site = addSiteField.getText().trim();
            String password = new String(addPasswordField.getPassword()).trim();

            if (site.isEmpty() || password.isEmpty()) {
                outputArea.setText("⚠️ Please enter both site and password.");
                return;
            }

            if (passwordStore.containsKey(site)) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "A password for this site already exists. Overwrite?",
                        "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) {
                    outputArea.setText("❌ Operation cancelled.");
                    return;
                }
            }

            String encrypted = encrypt(password);
            passwordStore.put(site, encrypted);
            savePasswords();
            refreshTable();
            outputArea.setText("✅ Password saved for " + site);
            addSiteField.setText("");
            addPasswordField.setText("");
        } catch (Exception ex) {
            outputArea.setText("❌ Error: " + ex.getMessage());
        }
    }

    private void retrievePassword() {
        try {
            String site = retrieveSiteField.getText().trim();
            if (site.isEmpty()) {
                outputArea.setText("⚠️ Enter site name to retrieve password.");
                return;
            }

            String encrypted = passwordStore.get(site);
            if (encrypted == null) {
                outputArea.setText("❌ No password found for " + site);
                return;
            }

            String decrypted = decrypt(encrypted);
            outputArea.setText("🔍 Site: " + site + "\nPassword: " + decrypted);
        } catch (Exception ex) {
            outputArea.setText("❌ Error retrieving password: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Map.Entry<String, String> entry : passwordStore.entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    private void savePasswords() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (var entry : passwordStore.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (Exception ignored) {}
    }

    private void loadPasswords() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            passwordStore.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(":", 2);
                if (p.length == 2) passwordStore.put(p[0], p[1]);
            }
        } catch (Exception ignored) {}
    }

    private void cleanDuplicateFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        Map<String, String> cleaned = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) cleaned.put(parts[0], parts[1]);
            }
        } catch (IOException ignored) {}

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, String> entry : cleaned.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException ignored) {}

        passwordStore.clear();
        passwordStore.putAll(cleaned);
    }

    private String encrypt(String data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(keyValue, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    private String decrypt(String encryptedData) throws Exception {
        SecretKeySpec key = new SecretKeySpec(keyValue, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
    }

   public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        // Show login dialog first
        LoginDialog login = new LoginDialog(null);
        login.setVisible(true);

        if (login.isSucceeded()) {
            // Only show the main GUI if login succeeds
            new PasswordManagerGUI().setVisible(true);
        } else {
            // Exit if login fails or cancelled
            System.exit(0);
        }
    });
}

}
