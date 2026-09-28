package org.example.simu.model;

import org.example.eduni.distributions.ContinuousGenerator;
import org.example.simu.framework.Clock;
import org.example.simu.framework.Event;
import org.example.simu.framework.EventList;

import java.util.LinkedList;

public class ServicePoint {
	private LinkedList<Customer> queue = new LinkedList<Customer>(); // Изменено с jono на queue
	private ContinuousGenerator generator;
	private EventList eventList;
	private EventType eventTypeScheduled;
	private boolean reserved = false;

	// Параметры конструктора переведены на английский
	public ServicePoint(ContinuousGenerator generator, EventList eventList, EventType eventType){
		this.eventList = eventList;
		this.generator = generator;
		this.eventTypeScheduled = eventType;
	}

	public void addQueue(Customer customer){   // Имя переменной 'a' изменено на 'customer'
		queue.add(customer);
	}

	public Customer removeQueue(){
		reserved = false;
		return queue.poll();
	}

	public void beginService() {
		reserved = true;
		double serviceTime = generator.sample();
		eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getTime() + serviceTime));
	}

	public boolean isReserved(){
		return reserved;
	}

	public boolean isOnQueue(){
		return queue.size() != 0;
	}
}
