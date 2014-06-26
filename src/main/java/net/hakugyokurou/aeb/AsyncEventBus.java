package net.hakugyokurou.aeb;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

public class AsyncEventBus extends EventBus{

	protected final Executor executor;
	protected final EventBus bus;
	
	public AsyncEventBus(Executor executor, EventBus wrappedEB) {
		this(getName(),executor,wrappedEB);
	}

	public AsyncEventBus(String name, Executor executor, EventBus wrappedEB) {
		super(name);
		this.executor = executor;
		this.bus = wrappedEB;
		if(bus instanceof AsyncEventBus)
			throw new IllegalArgumentException("An AsyncEventBus can't wrap another one.");
	}

	@Override
	public void register(Object handler) {
		bus.register(handler);
	}

	@Override
	public void unregister(Object handler) {
		bus.unregister(handler);
	}

	@Override
	public void post(Object event) {
		
		super.post(event);
	}

	@Override
	protected void addReceiver(Class<?> event, Object handler,
			Method subscriber, EventInvoker invoker) {
		bus.addReceiver(event, handler, subscriber, invoker);
	}

}
