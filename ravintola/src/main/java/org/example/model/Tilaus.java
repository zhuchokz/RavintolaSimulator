package org.example.model;

public class Tilaus {
    private Asiakas asiakas;
    private Pizza pizza;

    public Tilaus(Asiakas asiakas, Pizza pizza) {
        this.asiakas = asiakas;
        this.pizza = pizza;
    }

    public Asiakas getAsiakas() {
        return asiakas;
    }

    public Pizza getPizza() {
        return pizza;
    }
}
