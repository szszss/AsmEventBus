package net.hakugyokurou.aeb;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;

public abstract class EventInvoker {
	
	private final Method subscriber;
	
	public EventInvoker(Method subscriber) {
		this.subscriber = subscriber;
	}
	
	public Method getSubscriber() {
		return subscriber;
	}
	
	public abstract void invoke(Object receiver, Object event);
	
	public static class WWW extends EventInvoker {

		public WWW(Method subscriber) {
			super(subscriber);
		}

		@Override
		public void invoke(Object receiver, Object event) {
			((EventBus)receiver).register(event);
		}
		
	}
}
