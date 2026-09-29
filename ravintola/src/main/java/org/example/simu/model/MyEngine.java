package org.example.simu.model;

import org.example.controller.IControllerMtoV;
import org.example.eduni.distributions.Negexp;
import org.example.eduni.distributions.Normal;
import org.example.simu.framework.ArrivalProcess;
import org.example.simu.framework.Clock;
import org.example.simu.framework.Engine;
import org.example.simu.framework.Event;


public class MyEngine extends Engine {
	private ArrivalProcess arrivalProcess;

	private ServicePoint orderPoint;
	private ServicePoint payPoint;
	private ServicePoint tablePoint;
	private ServicePoint drinkPoint;
	private ServicePoint oven1;
	private ServicePoint oven2;
	private ServicePoint pickupPoint;
	private ServicePoint courierPoint;

	public MyEngine(IControllerMtoV controller){ // NEW
		super(controller); // NEW

		// Named ServicePoints with specific distributions & capacities
		orderPoint = new ServicePoint("Order Register", new Normal(2, 0.5), eventList, EventType.DEP_ORDER);
		payPoint = new ServicePoint("Payment Counter", new Normal(1, 0.5), eventList, EventType.DEP_PAY);
		tablePoint = new ServicePoint("Dining Tables", new Normal(15, 5), eventList, EventType.DEP_TABLE, 10);
		drinkPoint = new ServicePoint("Drink Kiosk", new Normal(1, 0.2), eventList, EventType.DEP_DRINK);

		oven1 = new ServicePoint("Oven 1", new Normal(10, 2), eventList, EventType.DEP_OVEN1, 4);
		oven2 = new ServicePoint("Oven 2", new Normal(10, 2), eventList, EventType.DEP_OVEN2, 4);

		pickupPoint = new ServicePoint("Pickup Counter", new Normal(1, 0.3), eventList, EventType.DEP_PICKUP);
		courierPoint = new ServicePoint("Couriers", new Normal(15, 5), eventList, EventType.DEP_COURIER, 3);

		servicePoints = new ServicePoint[] {
				orderPoint, payPoint, tablePoint, drinkPoint, oven1, oven2, pickupPoint, courierPoint
		};
		
		arrivalProcess = new ArrivalProcess(new Negexp(5,2), eventList, EventType.ARR);
	}

	@Override
	protected void initialization() {
		Customer.reset(); // resets after each restart of simulation
		Clock.getInstance().setTime(0); // resets clock after restart
		arrivalProcess.generateNext();	 // First arrival in the system
	}

	private void routeToOven(Customer c) {
		if (!oven1.isReserved()) {
			oven1.addQueue(c); // Put in Oven 1 if it has space
		} else if (!oven2.isReserved()) {
			oven2.addQueue(c); // Put in Oven 2 if Oven 1 is full
		} else {
			// If BOTH are full, put the customer in the queue that is shortest
			if (oven1.getQueueSize() <= oven2.getQueueSize()) {
				oven1.addQueue(c);
			} else {
				oven2.addQueue(c);
			}
		}
	}

	@Override
	protected void runEvent(Event t) {  // B phase events
		Customer customer;

		switch ((EventType)t.getType()){
		case ARR:
			customer = new Customer();
			if (customer.getType() == CustomerType.ONLINE) {
				routeToOven(customer);
			} else {
				orderPoint.addQueue(customer);
			}
			arrivalProcess.generateNext();
			controller.visualiseCustomer();
			break;

		case DEP_ORDER:
			customer = orderPoint.removeQueue();
			payPoint.addQueue(customer);
			break;

		case DEP_PAY:
			customer = payPoint.removeQueue();
			routeToOven(customer);
			break;

		case DEP_OVEN1:
		case DEP_OVEN2:
			if (t.getType() == EventType.DEP_OVEN1) {
				customer = oven1.removeQueue();
			} else {
				customer = oven2.removeQueue();
			}

			if (customer.getType() == CustomerType.ONLINE) {
				courierPoint.addQueue(customer);
			} else {
				pickupPoint.addQueue(customer);
			}
			break;

		case DEP_PICKUP:
			customer = pickupPoint.removeQueue();
			if  (customer.getType() == CustomerType.TAKEAWAY) {
				finishCustomer(customer);
			} else if (customer.getType() == CustomerType.DINEIN) {
				if (customer.wantsDrink()) {
					drinkPoint.addQueue(customer);
				} else {
					tablePoint.addQueue(customer);
				}
			}
			break;

		case DEP_DRINK:
			customer = drinkPoint.removeQueue();
			tablePoint.addQueue(customer);
			break;

		case DEP_TABLE:
			customer = tablePoint.removeQueue();
			finishCustomer(customer);
			break;

		case DEP_COURIER:
			customer = courierPoint.removeQueue();
			finishCustomer(customer);
			break;
		}	
	}

	private void finishCustomer(Customer c) {
		c.setRemovalTime(Clock.getInstance().getTime());
		c.reportResults();
	}

	@Override
	protected void results() {
		System.out.println("=========================================");
		System.out.println("       SIMULATION RESULTS REPORT        ");
		System.out.println("=========================================");
		System.out.printf("Total Simulation Time: %.2f minutes\n\n", Clock.getInstance().getTime());

		for (ServicePoint sp : servicePoints) {
			sp.reportResults();
		}

		// new gui
		controller.showEndTime(Clock.getInstance().getTime());
	}
}
