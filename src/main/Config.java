package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final String CONFIG_FILE = "currency_converter_config.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private String defaultFromCurrency;
    private List<String> favoriteCurrencies;
    private String apiKey;

    public Config() {
        this.defaultFromCurrency = "";
        this.favoriteCurrencies = new ArrayList<>();
        this.apiKey = "";
    }

    public String getDefaultFromCurrency() {
        return defaultFromCurrency;
    }

    public void setDefaultFromCurrency(String defaultFromCurrency) {
        this.defaultFromCurrency = defaultFromCurrency;
    }

    public List<String> getFavoriteCurrencies() {
        return favoriteCurrencies;
    }

    public void setFavoriteCurrencies(List<String> favoriteCurrencies) {
        this.favoriteCurrencies = favoriteCurrencies;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public static Config load() {
        try (Reader reader = new FileReader(CONFIG_FILE)) {
            return gson.fromJson(reader, Config.class);
        } catch (IOException e) {
            return new Config();
        }
    }

    public void save() {
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}