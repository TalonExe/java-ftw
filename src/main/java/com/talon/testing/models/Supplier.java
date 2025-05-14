package com.talon.testing.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
        try (BufferedReader br = new BufferedReader(new FileReader("D:\\APU One Drive\\OneDrive - Asia Pacific University\\Documents\\GitHub\\java-ftw\\target\\classes\\data\\suppliers.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 7) {
                    suppliers.add(new Supplier(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim(), data[4].trim(), data[5].trim(), data[6].trim()));
                }
            }
        }
        return suppliers;
    }

    // You can add the addSupplier method here if needed
    public static boolean addSupplier(Supplier supplier) throws IOException {
        // Implementation for adding a supplier to the file would go here
        System.out.println("Adding supplier functionality needs to be implemented.");
        return false;
    }
}