package org.example.simu.model;

import java.util.Random;


public class Order {
    private int orderId;
    private String pizzaType;
    private double price;

    private static final String[] MENU = {"Margherita", "Pepperoni", "Quattro Formaggi", "Hawaiian", "Vegetarian"};
    private static final double[] PRICES = {8.50, 10.90, 11.50, 9.90, 9.50};
    private static final Random random = new Random();

    public Order(int customerId) {
        this.orderId = customerId;

        int index = random.nextInt(MENU.length);
        this.pizzaType = MENU[index];
        this.price = PRICES[index];
    }

    public int getOrderId() {
        return orderId;
    }

    public String getPizzaType() {
        return pizzaType;
    }

    public double getPrice() {
        return price;
    }
}
