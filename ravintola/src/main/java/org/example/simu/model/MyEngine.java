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

	public MyEngine(IControllerMtoV controller){ // NEW
		super(controller); // NEW

		servicePoints = new ServicePoint[3];

		// Pizzeria service points: 0 = Cashier, 1 = Kitchen, 2 = Delivery
		servicePoints[0] = new ServicePoint(new Normal(10,6), eventList, EventType.DEP1);
		servicePoints[1] = new ServicePoint(new Normal(10,10), eventList, EventType.DEP2);
		servicePoints[2] = new ServicePoint(new Normal(5,3), eventList, EventType.DEP3);

		arrivalProcess = new ArrivalProcess(new Negexp(15,5), eventList, EventType.ARR1);
	}

	@Override
	protected void initialization() {
		arrivalProcess.generateNext();	 // First arrival in the system
	}

	@Override
	protected void runEvent(Event t) {  // B phase events
		Customer customer; // Заменено 'a' на 'customer'

		switch ((EventType)t.getType()){
			case ARR1:
				servicePoints[0].addQueue(new Customer());
				arrivalProcess.generateNext();
				controller.visualiseCustomer(); // NEW
				break;

			case DEP1:
				customer = servicePoints[0].removeQueue();
				servicePoints[1].addQueue(customer);
				break;

			case DEP2:
				customer = servicePoints[1].removeQueue();
				servicePoints[2].addQueue(customer);
				break;

			case DEP3:
				customer = servicePoints[2].removeQueue();
				customer.setRemovalTime(Clock.getInstance().getTime());
				customer.reportResults();
				break;
		}
	}

	@Override
	protected void results() {
		// NEW GUI
		controller.showEndTime(Clock.getInstance().getTime());
	}
}
