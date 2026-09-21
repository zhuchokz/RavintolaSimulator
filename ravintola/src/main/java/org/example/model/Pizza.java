package org.example.model;

public class Pizza {
    private String nimi;
    private boolean valmis;

    public Pizza(String nimi) {
        this.nimi = nimi;
        this.valmis = false;
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
