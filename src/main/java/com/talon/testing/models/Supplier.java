package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class Supplier {
    private final SimpleStringProperty supplierId;
    private final SimpleStringProperty supplierName;
    private final SimpleStringProperty contactPerson;
    private final SimpleStringProperty email;
    private final SimpleStringProperty phoneNumber;
    private final SimpleStringProperty address;
    private final SimpleStringProperty registrationDate;

    public Supplier(String supplierId, String supplierName, String contactPerson, String email, String phoneNumber, String address, String registrationDate) {
        this.supplierId = new SimpleStringProperty(supplierId);
        this.supplierName = new SimpleStringProperty(supplierName);
        this.contactPerson = new SimpleStringProperty(contactPerson);
        this.email = new SimpleStringProperty(email);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
        this.address = new SimpleStringProperty(address);
        this.registrationDate = new SimpleStringProperty(registrationDate);
    }

    public String getSupplierId() {
        return supplierId.get();
    }

    public SimpleStringProperty supplierIdProperty() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName.get();
    }

    public SimpleStringProperty supplierNameProperty() {
        return supplierName;
    }

    public String getContactPerson() {
        return contactPerson.get();
    }

    public SimpleStringProperty contactPersonProperty() {
        return contactPerson;
    }

    public String getEmail() {
        return email.get();
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public SimpleStringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public String getRegistrationDate() {
        return registrationDate.get();
    }

    public SimpleStringProperty registrationDateProperty() {
        return registrationDate;
    }

    public static ObservableList<Supplier> loadSuppliers() throws IOException {
        ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
        Gson gson = new Gson();
        try (BufferedReader br = new BufferedReader(new FileReader("D:\\APU One Drive\\OneDrive - Asia Pacific University\\Documents\\GitHub\\java-ftw\\target\\classes\\data\\suppliers.txt"))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }

            Type type = new TypeToken<Map<String, SupplierData>>(){}.getType();
            Map<String, SupplierData> supplierMap = gson.fromJson(jsonContent.toString(), type);

            if (supplierMap != null) {
                for (SupplierData data : supplierMap.values()) {
                    suppliers.add(new Supplier(data.getSupplierId(), data.getSupplierName(), data.getContactPerson(), data.getEmail(), data.getPhoneNumber(), data.getAddress(), data.getRegistrationDate()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Consider showing an alert to the user about the loading error
        }
        return suppliers;
    }

    // Helper class to match the inner structure of the JSON
    private static class SupplierData {
        private String supplierId;
        private String supplierName;
        private String contactPerson;
        private String email;
        private String phoneNumber;
        private String address;
        private String registrationDate;

        public String getSupplierId() {
            return supplierId;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public String getContactPerson() {
            return contactPerson;
        }

        public String getEmail() {
            return email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getAddress() {
            return address;
        }

        public String getRegistrationDate() {
            return registrationDate;
        }
    }

    public static boolean addSupplier(Supplier supplier) throws IOException {
        System.out.println("Adding supplier functionality needs to be implemented.");
        return false;
    }
}