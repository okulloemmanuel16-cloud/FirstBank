package ram.firstbank;

/**
 * Application entry point – First Bank Uganda
 * Starts the application by opening the Login portal.
 */
public class FirstBank {

    public static void main(String[] args) {
        // Initialise DB on startup
        DatabaseManager.initialiseDatabase();

        // Launch Login screen (Nimbus L&F)
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info :
                    javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }
}
