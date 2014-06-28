package net.hakugyokurou.aeb;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

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
		executor.execute(new Runnable() {
			private Object event;
			public Runnable setEvent(Object event) {
				this.event = event;
				return this;
			}
			public void run() {
				AsyncEventBus.this.bus.post(event);
			}
		}.setEvent(event));
	}

	@Override
	protected void addReceiver(Class<?> event, Object handler,
			Method subscriber, EventInvoker invoker) {
		bus.addReceiver(event, handler, subscriber, invoker);
	}

}