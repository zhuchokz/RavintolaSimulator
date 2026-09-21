package org.example.model;

import org.example.framework.Kello;
import org.example.framework.Moottori;
import org.example.framework.Saapumisprosessi;
import org.example.framework.Tapahtuma;
import org.example.eduni.distributions.Negexp;
import org.example.eduni.distributions.Normal;

public class OmaMoottori extends Moottori {

	private Saapumisprosessi saapumisprosessi;
	private Palvelupiste[] palvelupisteet;

	public OmaMoottori() {
		palvelupisteet = new Palvelupiste[3];

		// 3 palvelupistettä: Kassa, Keittiö ja Luovutustiski
		palvelupisteet[0] = new Palvelupiste(new Normal(4, 2), tapahtumalista, TapahtumanTyyppi.DEP1); // Kassa
		palvelupisteet[1] = new Palvelupiste(new Normal(12, 4), tapahtumalista, TapahtumanTyyppi.DEP2); // Keittiö
		palvelupisteet[2] = new Palvelupiste(new Normal(2, 1), tapahtumalista, TapahtumanTyyppi.DEP3); // Luovutus

		// asiakkaiden saapumisväli (keskiarvo 10 minuuttia)
		saapumisprosessi = new Saapumisprosessi(new Negexp(10, 5), tapahtumalista, TapahtumanTyyppi.ARR1);
	}

	@Override
	protected void alustukset() {
		saapumisprosessi.generoiSeuraava(); // Ensimmäinen asiakas jonoon
	}

	@Override
	protected void suoritaTapahtuma(Tapahtuma t) {
		Asiakas a;

		switch ((TapahtumanTyyppi) t.getTyyppi()) {
			case ARR1:
				// asiakas saapuu ja menee kassajonoon (palvelupiste 0)
				a = new Asiakas();
				palvelupisteet[0].lisaaJonoon(a);
				saapumisprosessi.generoiSeuraava();
				break;

			case DEP1:
				// asiakkaalle luodaan Pizza ja Tilaus
				a = (Asiakas) palvelupisteet[0].otaJonosta();
				System.out.println("-> Asiakas " + a.getId() + " teki tilauksen Kassalla. Siirtyy keittiöjonoon.");

				// tässä hyödynnetään olemassa olevia luokkianne:
				Pizza uusiPizza = new Pizza("Pepperoni");
				Tilaus tilaus = new Tilaus(a, uusiPizza);
				a.setTilaus(tilaus); // tallennetaan tilaus asiakkaalle

				palvelupisteet[1].lisaaJonoon(a); // siirretään keittiöjonoon
				break;

			case DEP2:
				// pizza on paistettu
				a = (Asiakas) palvelupisteet[1].otaJonosta();
				if (a.getTilaus() != null && a.getTilaus().getPizza() != null) {
					a.getTilaus().getPizza().setValmis(true); // merkataan pizza valmiiksi
				}
				System.out.println("-> Kokki sai valmiiksi asiakkaan " + a.getId() + " pizzan. Siirtyy luovutukseen.");
				palvelupisteet[2].lisaaJonoon(a); // siirretään luovutusjonoon
				break;

			case DEP3:
				// asiakas saa pizzan ja poistuu järjestelmästä
				a = (Asiakas) palvelupisteet[2].otaJonosta();
				a.setPoistumisaika(Kello.getInstance().getAika());
				a.raportti();
				break;
		}
	}

	@Override
	protected void yritaCTapahtumat() {
		for (Palvelupiste p : palvelupisteet) {
			if (!p.onVarattu() && p.onJonossa()) {
				p.aloitaPalvelu();
			}
		}
	}

	@Override
	protected void tulokset() {
		System.out.println("\n--- SIMULOINTI PÄÄTTYI ---");
		System.out.println("Kello on nyt: " + Kello.getInstance().getAika());
		System.out.println("Kiitos pizzansyönnistä!");
	}
}
