package net.hakugyokurou.aeb;

import java.lang.reflect.Method;

//Prioritized
public class PriorEventBus extends EventBus{

	public PriorEventBus(IPriorityStrategy judges) {
		this(getName(),judges);
	}

	public PriorEventBus(String name, IPriorityStrategy judges) {
		this(name, getDefaultSubscriberFinder(), judges);
	}
	
	public PriorEventBus(String name, ISubscriberFinder finder, IPriorityStrategy judges) {
		super(name, finder);
	}

	@Override
	protected void addReceiver(Class<?> event, Object handler,
			Method subscriber, EventInvoker invoker) {
		// TODO Auto-generated method stub
		super.addReceiver(event, handler, subscriber, invoker);
	}
}
