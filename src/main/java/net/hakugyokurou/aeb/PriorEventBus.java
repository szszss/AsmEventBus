package net.hakugyokurou.aeb;

import java.lang.reflect.Method;

//Prioritized
public class PriorEventBus extends EventBus{

	public PriorEventBus(IPriorityJudges judges) {
		this(getName(),judges);
	}

	public PriorEventBus(String name, IPriorityJudges judges) {
		this(name, getDefaultSubscriberFinder(), judges);
	}
	
	public PriorEventBus(String name, ISubscriberFinder finder, IPriorityJudges judges) {
		super(name, finder);
	}

	@Override
	protected void addReceiver(Class<?> event, Object handler,
			Method subscriber, EventInvoker invoker) {
		// TODO Auto-generated method stub
		super.addReceiver(event, handler, subscriber, invoker);
	}
}
