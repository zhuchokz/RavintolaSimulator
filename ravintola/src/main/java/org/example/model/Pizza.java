package org.example.model;

public class Pizza {
    private static int pizzaCounter = 1;
    private final int id;
    private String nimi;
    private boolean valmis;

    public Pizza(String nimi) {
        this.id = pizzaCounter++;
        this.nimi = nimi;
        this.valmis = false;
    }

    public int getId() {
        return id;
    }

    public String getNimi() {
        return nimi;
    }

    public boolean isValmis() {
        return valmis;
    }

    public void setValmis(boolean valmis) {
        this.valmis = valmis;
    }
}
