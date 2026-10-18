package org.example.simu.model;

/**
 * Enum representing the operational customer order categories within the pizzeria simulation.
 * <p>
 * Defines customer behavioral profiles, ordering preferences, and workstation routing paths:
 * <ul>
 *   <li>{@link #TAKEAWAY} - Customers ordering for takeaway pick-up.</li>
 *   <li>{@link #DINEIN} - Customers dining in-house (eligible for drink preferences and table service).</li>
 *   <li>{@link #ONLINE} - Customers ordering online for courier delivery dispatch.</li>
 * </ul>
 * </p>
 *
 * @author Illia
 * @version 1.0
 */
public enum CustomerType {
    TAKEAWAY, DINEIN, ONLINE
}
