package org.example.model;

import org.example.controller.IKontrolleriForM;
import org.example.framework.*;

import org.example.eduni.distributions.Negexp;
import org.example.eduni.distributions.Normal;

public class OmaMoottori extends Moottori {

	private Saapumisprosessi saapumisprosessi;
	private Palvelupiste[] palvelupisteet;

	public OmaMoottori(IKontrolleriForM kontrolleri){

		super(kontrolleri);
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
	protected void suoritaTapahtuma(Tapahtuma t) {  // B-vaiheen tapahtumat
		Asiakas a;
		switch ((TapahtumanTyyppi) t.getTyyppi()) {

			case ARR1:
				// asiakas saapuu ravintolaan
				palvelupisteet[0].lisaaJonoon(new Asiakas());
				saapumisprosessi.generoiSeuraava();
				break;

			case DEP1:
				// tilaus ja maksu suoritettu
				a = (Asiakas) palvelupisteet[0].otaJonosta();

				Pizza pz = new Pizza("Pepperoni");
				Tilaus tilaus = new Tilaus(a, pz); //tilausnmr
				a.setTilaus(tilaus);

				System.out.println("-> Asiakas " + a.getId() + " maksoi tilauksen nro " + tilaus.getId() + ". Siirtyy odottamaan (Uuni).");

				// siirretään uuniin (palvelupiste 1)
				palvelupisteet[1].lisaaJonoon(a);
				break;

			case DEP2:
				// odotus ja valmistus päättyi
				a = (Asiakas) palvelupisteet[1].otaJonosta();
				a.getTilaus().getPizza().setValmis(true);

				System.out.println("-> Pizza tilaukseen nro " + a.getTilaus().getId() + " on valmis uunista! Siirtyy vastaanottoon.");

				// siirretään luovutustiskille / vastaanottoon (palvelupiste 2)
				palvelupisteet[2].lisaaJonoon(a);
				break;

			case DEP3:
				// vastaanotto valmis -> asiakas ulos
				a = (Asiakas) palvelupisteet[2].otaJonosta();
				a.setPoistumisaika(Kello.getInstance().getAika());

				System.out.println("-> Asiakas " + a.getId() + " otti tilauksen nro " + a.getTilaus().getId() + " vastaan ja käveli ulos ravintolasta.");
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
		kontrolleri.visualisoiAsiakas(); // UUSI

	}
}
