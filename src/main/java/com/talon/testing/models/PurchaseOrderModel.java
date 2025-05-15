package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class PurchaseOrderModel {

    private static final String FILE_PATH = "purchase_orders.json";
    private static final Gson gson = new Gson();
    private static final Type PO_LIST_TYPE = new TypeToken<List<PurchaseOrder>>() {}.getType();

    public List<PurchaseOrder> loadPurchaseOrders() {
        List<PurchaseOrder> orders = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return orders;

        try (Reader reader = new FileReader(file)) {
            List<PurchaseOrder> loaded = gson.fromJson(reader, PO_LIST_TYPE);
            if (loaded != null) orders = loaded;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public void savePurchaseOrders(List<PurchaseOrder> orders) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(orders, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<PurchaseOrder> getApprovedOrders() {
        List<PurchaseOrder> approved = new ArrayList<>();
        for (PurchaseOrder po : loadPurchaseOrders()) {
            if ("Approved".equalsIgnoreCase(po.getStatus())) {
                approved.add(po);
            }
        }
        return approved;
    }
}
