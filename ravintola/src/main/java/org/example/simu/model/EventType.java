package org.example.simu.model;

import org.example.simu.framework.IEventType;

/**
 * Enum defining all discrete event types processed by the discrete-event simulation engine.
 * <p>
 * Implements {@link IEventType} to classify state transitions and customer lifecycle events
 * across workstations (arrivals, order placement, payment, baking, dine-in seating, drink fetching, and delivery dispatch).
 * </p>
 *
 * @author Illia
 * @version 2.0
 */
public enum EventType implements IEventType {
	/** General customer arrival at the pizzeria. */
	ARR,

	/** Order processing completed; customer moves to payment. */
	DEP_ORDER,

	/** Payment completed; order routed to an available oven queue. */
	DEP_PAY,

	/** Pizza baking completed in Oven 1; order routed to pickup or courier dispatch. */
	DEP_OVEN1,

	/** Pizza baking completed in Oven 2; order routed to pickup or courier dispatch. */
	DEP_OVEN2,

	/** Pickup completed; takeaway customers depart, while dine-in customers route to drinks or tables. */
	DEP_PICKUP,

	/** Drink fetching completed; customer proceeds to dining table seating. */
	DEP_DRINK,

	/** In-house dining meal finished; dine-in customer departs the pizzeria. */
	DEP_TABLE,

	/** Courier delivery dispatch completed; online customer order leaves system. */
	DEP_COURIER
}
