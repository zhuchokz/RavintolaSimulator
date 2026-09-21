package org.example.testi;

import org.example.framework.Moottori;
import org.example.framework.Trace;
import org.example.framework.Trace.Level;
import org.example.model.OmaMoottori;

public class Simulaattori { //Tekstipohjainen

	public static void main(String[] args) {
		
		Trace.setTraceLevel(Level.INFO);
		Moottori m = new OmaMoottori();
		m.setSimulointiaika(10);
		m.aja();
		///
	}
}
