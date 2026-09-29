package org.example.simu.model;

import org.example.simu.framework.IEventType;

// TODO:
// Event types are defined by the requirements of the simulation model
public enum EventType implements IEventType {
	ARR,        // Customer arrival
	DEP_ORDER,  // Order completed -> Payment
	DEP_PAY,    // Payment completed -> Oven queue
	DEP_OVEN1,  // Pizza baked in Oven 1 -> Pickup or Courier
	DEP_OVEN2,  // Pizza baked in Oven 2 -> Pickup or Courier
	DEP_PICKUP, // Pickup completed -> Exit (Takeaway) or Drink/Table (Dine-in)
	DEP_DRINK,  // Drink fetched -> Table
	DEP_TABLE,  // Meal finished at table -> Exit
	DEP_COURIER // Delivery completed -> Exit
}
