package main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>converts currencies using real-time exchange rates.</p>
 * fetches exchange rates from the exchange rate API and has
 * methods for currency conversion and rate management.
 */
public class CurrencyConverter {
    private static final String API_BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private static final String API_ENDPOINT_SUFFIX = "/latest/USD";
    private static final String REFERENCE_CURRENCY = "USD";
    private static final Gson gson = new Gson();
    private static final Logger LOGGER = Logger.getLogger(CurrencyConverter.class.getName());

    private final Config config;
    private Map<String, Double> exchangeRates;
    private String lastUpdateTime;
    private String nextUpdateTime;

    /**
     * constructs a new CurrencyConverter and initializes it by fetching
     * the latest exchange rates
     */
    public CurrencyConverter(Config config) {
        LOGGER.info("Initializing CurrencyConverter");
        this.config = config;
        refreshRates();
    }

    /**
     * refreshes the exchange rates by fetching the latest data from the API
     *
     * @return true if the rates were successfully refreshed, false otherwise
     */
    public boolean refreshRates() {
        LOGGER.info("Refreshing rates");
        try {
            ExchangeRateResponse response = getExchangeRates();
            if (response != null && "success".equals(response.result)) {
                LOGGER.info("Successfully retrieved exchange rates");
                exchangeRates = new HashMap<>();
                for (String currency : response.conversion_rates.keySet()) {
                    exchangeRates.put(currency, response.conversion_rates.get(currency).getAsDouble());
                }
                lastUpdateTime = response.time_last_update_utc;
                nextUpdateTime = response.time_next_update_utc;
                LOGGER.info("Rates updated. Last update: " + lastUpdateTime + ", Next update: " + nextUpdateTime);
                return true;
            } else {
                LOGGER.warning("Failed to refresh rates. Response: " + (response != null ? response.result : "null"));
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing rates", e);
            return false;
        }
    }

    /**
     * converts an amount from one currency to another
     *
     * @param amount       amount to convert
     * @param fromCurrency currency code to convert from
     * @param toCurrency   currency code to convert to
     * @return converted amount in the target currency
     * @throws IllegalStateException if exchange rates are not available for the specified currencies
     */
    public double convert(double amount, String fromCurrency, String toCurrency) {
        LOGGER.info("converting " + amount + " from " + fromCurrency + " to " + toCurrency);
        if (exchangeRates == null || !exchangeRates.containsKey(fromCurrency) || !exchangeRates.containsKey(toCurrency)) {
            LOGGER.warning("Exchange rates not available for " + fromCurrency + " or " + toCurrency);
            throw new IllegalStateException("Exchange rates are not available for the specified currencies");
        }
        double fromRate = exchangeRates.get(fromCurrency);
        double toRate = exchangeRates.get(toCurrency);
        double result = amount * (toRate / fromRate);
        LOGGER.info("Conversion result: " + result);
        return result;
    }

    /**
     * retrieves a list of all available currency codes
     *
     * @return a sorted list of currency codes available for conversion
     */
    public List<String> getAvailableCurrencies() {
        if (exchangeRates == null) {
            LOGGER.warning("Exchange rates not available");
            return Collections.emptyList();
        }
        List<String> currencies = new ArrayList<>(exchangeRates.keySet());
        Collections.sort(currencies);
        LOGGER.info("Retrieved " + currencies.size() + " available currencies");
        return currencies;
    }

    /**
     * gets the timestamp of the last exchange rate update
     *
     * @return a string representing the last update time in UTC, or "N/A" if not available
     */
    public String getLastUpdateTime() {
        return lastUpdateTime != null ? lastUpdateTime : "N/A";
    }

    /**
     * gets the timestamp of the next scheduled exchange rate update
     *
     * @return a string representing the next update time in UTC, or "N/A" if it's not available
     */
    public String getNextUpdateTime() {
        return nextUpdateTime != null ? nextUpdateTime : "N/A";
    }

    /**
     * fetches the latest exchange rates from the API
     *
     * @return an ExchangeRateResponse object containing the fetched data
     * @throws Exception if there's an error during the API request or response parsing
     */
    private ExchangeRateResponse getExchangeRates() throws Exception {
        LOGGER.info("Fetching exchange rates for " + REFERENCE_CURRENCY);
        String apiUrl = API_BASE_URL + config.getApiKey() + API_ENDPOINT_SUFFIX;
        URL url = URI.create(apiUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        LOGGER.info("API response code: " + responseCode);

        if (responseCode != 200) {
            LOGGER.warning("Failed API request. HTTP error code: " + responseCode);
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = br
                    .lines()
                    .collect(
                            StringBuilder::new,
                            StringBuilder::append,
                            StringBuilder::append
                    ).toString();
            LOGGER.info("Received API response: " + response);
            ExchangeRateResponse exchangeRateResponse = gson.fromJson(response, ExchangeRateResponse.class);
            LOGGER.info("Parsed API response");
            return exchangeRateResponse;
        } finally {
            conn.disconnect();
        }
    }

    /**
     * a private class representing the structure of the API response.
     */
    @SuppressWarnings("unused") // some fields are unused in this app and i didnt feel like having warnings
    private static class ExchangeRateResponse {
        String result;
        String documentation;
        String terms_of_use;
        long time_last_update_unix;
        String time_last_update_utc;
        long time_next_update_unix;
        String time_next_update_utc;
        String base_code;
        JsonObject conversion_rates;
    }
}