package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>
 *     a GUI for the app
 * </p>
 * <p>
 *     uses JFrame and has a native looking user interface for the
 *     currency conversion operations.
 *     it has a swap button, convert button, and refresh button.
 *     the app only loads makes a call once at the start or
 *     when the refresh button is pressed.
 * </p>
 */
public class GUI extends JFrame {
    private final Config config;
    private CurrencyConverter converter;
    private JComboBox<String> fromCurrency;
    private JComboBox<String> toCurrency;
    private JTextField amountField;
    private JLabel resultLabel;
    private JButton convertButton;
    private JButton swapButton;
    private JButton refreshButton;
    private JButton setDefaultButton;
    private JButton addToFavoritesButton;
    private JLabel lastUpdateLabel;
    private JLabel nextUpdateLabel;

    /**
     * <p>constructs a new GUI instance</p>
     * initialises the GUI components, sets up the layout, and adds event listeners
     */
    public GUI() {
        setLookAndFeel();
        config = Config.load();
        checkAndSetApiKey();
        initializeConverter();
        initComponents();
        setupLayout();
        addListeners();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Currency Converter");
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * sets the look and feel of the application to the system's default
     */
    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeConverter() {
        try {
            converter = new CurrencyConverter(config);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to initialize the currency converter: " + e.getMessage(),
                    "Initialization Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void checkAndSetApiKey() {
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            String apiKey = JOptionPane.showInputDialog(this,
                    "Please enter your ExchangeRate-API key:",
                    "API Key Required",
                    JOptionPane.QUESTION_MESSAGE);

            if (apiKey != null && !apiKey.trim().isEmpty()) {
                config.setApiKey(apiKey.trim());
                config.save();
            } else {
                JOptionPane.showMessageDialog(this,
                        "A valid API key is required to use this application.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
    }

    /**
     * <p>initialises all the GUI components used in the application</p>
     * this includes creating combo boxes, text fields, labels, and buttons
     */
    private void initComponents() {
        List<String> currencies = new ArrayList<>(config.getFavoriteCurrencies());
        currencies.addAll(converter.getAvailableCurrencies());
        fromCurrency = new JComboBox<>(currencies.toArray(new String[0]));
        toCurrency = new JComboBox<>(currencies.toArray(new String[0]));
        amountField = new JTextField(10);
        resultLabel = new JLabel("Converted amount will appear here");
        resultLabel.setFont(resultLabel.getFont().deriveFont(Font.BOLD));
        convertButton = new JButton("Convert");
        swapButton = new JButton("Swap");
        refreshButton = new JButton("Refresh Rates");
        setDefaultButton = new JButton("Set Default");
        addToFavoritesButton = new JButton("Add to Favorites");
        lastUpdateLabel = new JLabel("Last update: " + converter.getLastUpdateTime());
        nextUpdateLabel = new JLabel("Next update: " + converter.getNextUpdateTime());

        String defaultCurrency = config.getDefaultFromCurrency();
        if (defaultCurrency != null && !defaultCurrency.isEmpty() && currencies.contains(defaultCurrency)) {
            fromCurrency.setSelectedItem(defaultCurrency);
        }
    }

    /**
     * <p>sets up the layout of the GUI components</p>
     * organises the components in a grid bag layout for a clean and responsive design
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // reference currency
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        mainPanel.add(new JLabel("From:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        mainPanel.add(fromCurrency, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        mainPanel.add(setDefaultButton, gbc);

        // target currency
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        mainPanel.add(new JLabel("To:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        mainPanel.add(toCurrency, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        mainPanel.add(addToFavoritesButton, gbc);

        // amount
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Amount:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        mainPanel.add(amountField, gbc);

        // result
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3; gbc.weightx = 1.0;
        mainPanel.add(resultLabel, gbc);

        // buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.add(convertButton);
        buttonPanel.add(swapButton);
        buttonPanel.add(refreshButton);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        mainPanel.add(buttonPanel, gbc);

        // update labels
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        mainPanel.add(lastUpdateLabel, gbc);

        gbc.gridy = 6;
        mainPanel.add(nextUpdateLabel, gbc);

        setContentPane(mainPanel);
    }

    /**
     * <p>adds action listeners to the buttons in the GUI</p>
     * this method sets up the event handling for user interactions
     */
    private void addListeners() {
        convertButton.addActionListener(e -> convertCurrency());
        swapButton.addActionListener(e -> swapCurrencies());
        refreshButton.addActionListener(e -> refreshRates());
        setDefaultButton.addActionListener(e -> setDefaultCurrency());
        addToFavoritesButton.addActionListener(e -> addToFavorites());
    }

    /**
     * <p>sets the currently selected 'from' currency as the default</p>
     * updates the configuration and saves it.
     * displays a confirmation message to the user
     */
    private void setDefaultCurrency() {
        String selectedCurrency = (String) fromCurrency.getSelectedItem();
        config.setDefaultFromCurrency(selectedCurrency);
        config.save();
        JOptionPane.showMessageDialog(this, selectedCurrency + " set as default 'from' currency", "Default Set", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * <p>adds the currently selected 'to' currency to the list of favourites.</p>
     * updates the configuration, saves it, and refreshes the currency lists
     * displays a confirmation message or informs if the currency is already a favourite
     */
    private void addToFavorites() {
        String selectedCurrency = (String) toCurrency.getSelectedItem();
        List<String> favorites = config.getFavoriteCurrencies();
        if (!favorites.contains(selectedCurrency)) {
            favorites.addFirst(selectedCurrency);
            config.setFavoriteCurrencies(favorites);
            config.save();
            updateCurrencyLists();
            JOptionPane.showMessageDialog(this, selectedCurrency + " added to favorites", "Favorite Added", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, selectedCurrency + " is already in favorites", "Already Favorite", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * <p>updates the currency lists in the combo boxes.</p>
     * <p>combines favourite currencies with all available currencies</p>
     * preserves the current selections in both combo boxes
     */
    private void updateCurrencyLists() {
        List<String> currencies = new ArrayList<>(config.getFavoriteCurrencies());
        currencies.addAll(converter.getAvailableCurrencies());

        String selectedFromCurrency = (String) fromCurrency.getSelectedItem();
        String selectedToCurrency = (String) toCurrency.getSelectedItem();

        fromCurrency.setModel(new DefaultComboBoxModel<>(currencies.toArray(new String[0])));
        toCurrency.setModel(new DefaultComboBoxModel<>(currencies.toArray(new String[0])));

        fromCurrency.setSelectedItem(selectedFromCurrency);
        toCurrency.setSelectedItem(selectedToCurrency);
    }

    /**
     * <p>performs the currency conversion based on user input.</p>
     * <p>retrieves values from input fields, calls the converter, and displays the result.</p>
     * handles potential errors and displays appropriate error messages
     */
    private void convertCurrency() {
        try {
            String from = (String) fromCurrency.getSelectedItem();
            String to = (String) toCurrency.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText());

            double result = converter.convert(amount, from, to);

            DecimalFormat df = new DecimalFormat("#.##");
            resultLabel.setText(amount + " " + from + " = " + df.format(result) + " " + to);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Conversion error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * swaps the selected currencies in the 'from' and 'to' combo boxes.
     */
    private void swapCurrencies() {
        int fromIndex = fromCurrency.getSelectedIndex();
        int toIndex = toCurrency.getSelectedIndex();
        fromCurrency.setSelectedIndex(toIndex);
        toCurrency.setSelectedIndex(fromIndex);
    }

    /**
     * refreshes the exchange rates by calling the CurrencyConverter refresh method
     * updates the GUI with new timestamps and notifies the user of the result
     */
    private void refreshRates() {
        if (converter.refreshRates()) {
            lastUpdateLabel.setText("Last update: " + converter.getLastUpdateTime());
            nextUpdateLabel.setText("Next update: " + converter.getNextUpdateTime());
            JOptionPane.showMessageDialog(this, "Rates updated successfully", "Refresh Rates", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update rates", "Refresh Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}