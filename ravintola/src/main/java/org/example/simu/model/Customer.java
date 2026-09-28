package org.example.simu.model;

import org.example.simu.framework.Clock;
import org.example.simu.framework.Trace;

/**
 * Represents a Pizzeria Customer.
 */
public class Customer {
	private double arrivalTime;
	private double removalTime;
	private int id;
	private Order order; // Связываем клиента с его заказом (tilaus)

	private static int i = 1;
	private static long sum = 0; // Внимание: при делении long на int среднее время будет округляться до целого! (см. примечание ниже)

	public Customer() {
		id = i++;
		arrivalTime = Clock.getInstance().getTime();

		// Автоматически создаем случайный заказ для этого клиента при его создании
		this.order = new Order(this.id);

		Trace.out(Trace.Level.INFO, "New pizzeria customer #" + id + " arrived at " + arrivalTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " wants to order: " + order.getPizzaType());
	}

	// Геттеры и сеттеры
	public double getRemovalTime() {
		return removalTime;
	}

	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public int getId() {
		return id;
	}

	public Order getOrder() {
		return order;
	}

	public void reportResults() {
		double timeSpent = removalTime - arrivalTime;

		Trace.out(Trace.Level.INFO, "\nPizzeria Customer " + id + " is ready and leaves with their order!");
		Trace.out(Trace.Level.INFO, "Pizza ordered: " + order.getPizzaType() + " (Price: " + order.getPrice() + "€)");
		Trace.out(Trace.Level.INFO, "Arrived at: " + arrivalTime);
		Trace.out(Trace.Level.INFO, "Left at: " + removalTime);
		Trace.out(Trace.Level.INFO, "Total time spent in pizzeria: " + timeSpent);

		// Исправлен расчет среднего времени (sum сделан double, чтобы не терять точность)
		sum += timeSpent;
		double mean = (double) sum / id;
		System.out.println("Current mean of pizzeria customer service times: " + mean);
	}
}
