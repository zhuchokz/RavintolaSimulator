package org.example.model;

public class Tilaus {
    private static int idCounter = 1;
    private final int id;             // TilausNro
    private Asiakas asiakas;
    private Pizza pizza;

    public Tilaus(Asiakas asiakas, Pizza pizza) {
        this.id = idCounter++;
        this.asiakas = asiakas;
        this.pizza = pizza;
    }

    public int getId() {
        return id;
    }

    public Asiakas getAsiakas() {
        return asiakas;
    }

    public Pizza getPizza() {
        return pizza;
    }
}
