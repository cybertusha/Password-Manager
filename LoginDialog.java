import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean succeeded;

    // Hardcoded credentials (demo purposes)
    private final String MASTER_USERNAME = "admin";
    private final String MASTER_PASSWORD = "password123"; // In production, use hashed password

    public LoginDialog(Frame parent) {
        super(parent, "🔐 Login", true);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(45,45,50));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        usernameField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passwordField = new JPasswordField(20);
        passwordField.setEchoChar('•');

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(52, 152, 219));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.addActionListener(e -> login());

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(userLabel, gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(passLabel, gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(loginBtn, gbc);

        getContentPane().add(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (MASTER_USERNAME.equals(username) && MASTER_PASSWORD.equals(password)) {
            succeeded = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "❌ Invalid username or password",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            usernameField.setText("");
            passwordField.setText("");
            succeeded = false;
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
