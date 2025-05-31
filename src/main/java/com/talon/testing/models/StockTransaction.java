package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime; // Using LocalDateTime for more precision
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // For unique transaction IDs

public class StockTransaction {
    private String transactionId;
    private String itemId;
    private String itemNameDisplay; // For easier display
    private String poId; // Optional: Link to Purchase Order if applicable
    private String transactionType; // e.g., "PO_RECEIPT", "MANUAL_ADJUSTMENT", "SALE"
    private int quantityChange;    // Positive for stock in, negative for stock out
    private int stockLevelAfterTransaction;
    private String transactionTimestamp; // Store as ISO_LOCAL_DATE_TIME string
    private String userId; // User who performed/triggered the transaction

    private static final String TRANSACTIONS_FILE_PATH = "/data/stock_transactions.txt";
    // Store as a List, not a Map, as order might matter and ID is in object.
    private static final Type TRANSACTION_LIST_TYPE = new TypeToken<List<StockTransaction>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public StockTransaction(String itemId, String itemNameDisplay, String poId, String transactionType,
                            int quantityChange, int stockLevelAfterTransaction, String userId) {
        this.transactionId = UUID.randomUUID().toString(); // Generate unique ID
        this.itemId = itemId;
        this.itemNameDisplay = itemNameDisplay;
        this.poId = poId;
        this.transactionType = transactionType;
        this.quantityChange = quantityChange;
        this.stockLevelAfterTransaction = stockLevelAfterTransaction;
        this.transactionTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        this.userId = userId; // Could be system or a logged-in user ID
    }

    public StockTransaction() { // For Gson
        this.transactionTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getItemId() { return itemId; }
    public String getItemNameDisplay() { return itemNameDisplay; }
    public String getPoId() { return poId; }
    public String getTransactionType() { return transactionType; }
    public int getQuantityChange() { return quantityChange; }
    public int getStockLevelAfterTransaction() { return stockLevelAfterTransaction; }
    public String getTransactionTimestamp() { return transactionTimestamp; }
    public String getUserId() { return userId; }

    // Setters (mainly for Gson, or if you need to modify after creation)
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setItemNameDisplay(String itemNameDisplay) { this.itemNameDisplay = itemNameDisplay; }
    public void setPoId(String poId) { this.poId = poId; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }
    public void setStockLevelAfterTransaction(int stockLevelAfterTransaction) { this.stockLevelAfterTransaction = stockLevelAfterTransaction; }
    public void setTransactionTimestamp(String transactionTimestamp) { this.transactionTimestamp = transactionTimestamp; }
    public void setUserId(String userId) { this.userId = userId; }


    public static List<StockTransaction> loadTransactions() throws IOException {
        List<StockTransaction> transactions = new ArrayList<>();
        File file = getFileFromResource(TRANSACTIONS_FILE_PATH, true); // Ensure file exists

        if (file == null || !file.exists() || file.length() == 0) {
            // If file doesn't exist or is empty, write an empty list to initialize it
            if (file != null && (!file.exists() || file.length() == 0) ) {
                 try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                    gson.toJson(new ArrayList<>(), writer); // Write empty JSON array
                }
                System.out.println("Initialized empty stock_transactions.txt");
            }
            return transactions; // Return empty list
        }

        try (InputStream inputStream = new FileInputStream(file); // Read from the actual file
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            transactions = gson.fromJson(reader, TRANSACTION_LIST_TYPE);
            if (transactions == null) {
                transactions = new ArrayList<>();
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from Stock Transaction data file: " + TRANSACTIONS_FILE_PATH);
            throw new IOException("Error parsing Stock Transaction JSON data. File might be corrupted or not a valid JSON array.", e);
        }
        return transactions;
    }

    public static void saveTransactions(List<StockTransaction> transactions) throws IOException {
        File file = getFileFromResource(TRANSACTIONS_FILE_PATH, false); // Don't create if not found for save, should exist
        if (file == null) {
            throw new IOException("Could not get file for saving Stock Transactions: " + TRANSACTIONS_FILE_PATH);
        }
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(transactions, writer);
            System.out.println("Stock Transactions saved to: " + file.getAbsolutePath());
        }
    }
    
    // Add a single transaction and save the whole list
    public static void addTransaction(StockTransaction transaction) throws IOException {
        if (transaction == null) return;
        List<StockTransaction> transactions = loadTransactions(); // Load existing
        transactions.add(transaction); // Add new one
        saveTransactions(transactions); // Save all
    }


    // getFileFromResource helper (copy from other models or move to a utility)
    private static File getFileFromResource(String resourcePath, boolean createIfNotFound) throws IOException {
        URL resourceUrl = StockTransaction.class.getResource(resourcePath);
        File file;
        if (resourceUrl != null) {
            try { file = new File(resourceUrl.toURI()); }
            catch (URISyntaxException e) { throw new IOException("Invalid resource URI: " + resourcePath, e); }
        } else {
            URL codeSourceUrl = StockTransaction.class.getProtectionDomain().getCodeSource().getLocation();
            File baseDir;
            try {
                baseDir = new File(codeSourceUrl.toURI()).getParentFile();
                file = new File(baseDir, resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath);
            } catch (Exception e) {
                file = new File(System.getProperty("user.dir") + resourcePath);
            }
            if (createIfNotFound && !file.exists()) {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                    System.err.println("Could not create dir: " + parentDir.getAbsolutePath()); return null;
                }
                if (!file.createNewFile()) {
                    System.err.println("Could not create file: " + file.getAbsolutePath()); return null;
                }
                 if (file.length() == 0) { // Initialize with [] if newly created
                    try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                        writer.write("[]"); // Empty JSON array
                    }
                }
            }
        }
        return file;
    }
}