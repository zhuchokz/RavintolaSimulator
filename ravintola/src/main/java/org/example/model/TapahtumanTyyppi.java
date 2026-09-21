package org.example.model;

import org.example.framework.ITapahtumanTyyppi;

// TODO:
// Tapahtumien tyypit määritellään simulointimallin vaatimusten perusteella
public enum TapahtumanTyyppi implements ITapahtumanTyyppi{
	ARR1, // Asiakas saapuu ravintolaan
	DEP1, // Kassa valmis (tilaus tehty)
	DEP2, // Keittiö valmis (pizza paistettu)
	DEP3  // Luovutus valmis (pizza syöty/haettu)

}
