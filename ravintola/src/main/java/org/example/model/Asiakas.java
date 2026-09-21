package org.example.model;

import org.example.framework.Kello;
import org.example.framework.Trace;

public class Asiakas {
	private double saapumisaika;
	private double poistumisaika;
	private int id;
	private Tilaus tilaus;

	private static int i = 1;
	private static long sum = 0;

	public Asiakas() {
		id = i++;
		saapumisaika = Kello.getInstance().getAika();
		Trace.out(Trace.Level.INFO, "Uusi asiakas nro " + id + " saapui ravintolaan klo " + saapumisaika);
	}

	public Tilaus getTilaus() { return tilaus; }
	public void setTilaus(Tilaus tilaus) { this.tilaus = tilaus; }
	public double getPoistumisaika() { return poistumisaika; }
	public void setPoistumisaika(double poistumisaika) { this.poistumisaika = poistumisaika; }
	public double getSaapumisaika() { return saapumisaika; }
	public int getId() { return id; }

	public void raportti() {
		Trace.out(Trace.Level.INFO, "\n>> Asiakas " + id + " sai pizzansa ja poistui!");
		Trace.out(Trace.Level.INFO, ">> Viipymäaika ravintolassa: " + (poistumisaika - saapumisaika));
		sum += (poistumisaika - saapumisaika);
		double keskiarvo = (double) sum / id;
		System.out.println(">> Asiakkaiden läpimenon keskiarvo tähän asti: " + keskiarvo);
	}
}
