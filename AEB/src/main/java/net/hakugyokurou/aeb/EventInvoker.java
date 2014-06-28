package net.hakugyokurou.aeb;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;

public abstract class EventInvoker {
	
	//private List<Object> receivers = new CopyOnWriteArrayList<Object>();
	
	public abstract void invoke(Object receiver, Object event);
	
	/*public void post(Event event) {
		for(Iterator<Object> iterator=receivers.iterator(); iterator.hasNext(); )
		{
			invoke(iterator.next(), event);
		}
	}
	
	void addReceiver(Object receiver) {
		receivers.add(receiver);
	}
	
	void removeReceiver(Object receiver) {
		receivers.remove(receiver);
	}*/
}
