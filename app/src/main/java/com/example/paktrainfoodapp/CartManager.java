package com.example.paktrainfoodapp;

import com.example.paktrainfoodapp.ui.main.Passenger.home.CartItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CartManager {

    private static HashMap<String, CartItem> cartMap = new HashMap<>();
    private static List<Runnable> listeners = new ArrayList<>();

    public static CartItem getFirstItem() {

        if (cartMap.isEmpty()) return null;

        return new ArrayList<>(cartMap.values()).get(0);
    }

    public static void addListener(Runnable r) {

        if (!listeners.contains(r)) {
            listeners.add(r);
        }
    }

    public static void removeListener(Runnable r) {
        listeners.remove(r);
    }

    private static void notifyChange() {

        for (Runnable r : listeners) {
            r.run();
        }
    }

    public static void addOrUpdate(CartItem newItem) {

        String key = newItem.getKey();

        if (cartMap.containsKey(key)) {

            CartItem existing = cartMap.get(key);

            existing.setQuantity(
                    existing.getQuantity() + newItem.getQuantity()
            );

        } else {
            cartMap.put(key, newItem);
        }

        notifyChange();
    }

    public static List<CartItem> getCartItems() {
        return new ArrayList<>(cartMap.values());
    }

    public static double getTotalPrice() {

        double total = 0;

        for (CartItem item : cartMap.values()) {
            total += item.getPrice() * item.getQuantity();
        }

        return total;
    }

    public static void clear() {
        cartMap.clear();
        notifyChange();
    }

    public static boolean isEmpty() {
        return cartMap.isEmpty();
    }
}



