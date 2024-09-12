package main;

import javax.swing.SwingUtilities;

/**
 * <p>main class for the currency converter application</p>
 * <p>launches the GUI using SwingUtilities.invokeLater for thread safety</p>
 *
 * notable features include:
 * <ul>
 *     <li>ability to set a default 'from' currency for convenience</li>
 *     <li>favourite currency list for quick access to frequently used currencies</li>
 *     <li>persistence of defaults and favourites between sessions using a config file</li>
 *     <li>option to manually refresh exchange rates</li>
 *     <li>simple native looking GUI</li>
 * </ul>
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}