package net.hakugyokurou.aeb;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import net.hakugyokurou.aeb.auxiliary.IDeadEventHandler;
import net.hakugyokurou.aeb.auxiliary.ISubscriberExceptionHandler;
import net.hakugyokurou.aeb.strategy.EnumHierarchyStrategy;
import net.hakugyokurou.aeb.strategy.ISubscriberStrategy;

public class AsyncEventBus extends EventBus{

	protected final Executor executor;
	protected final EventBus bus;
	
	public AsyncEventBus(Executor executor, EventBus wrappedEB) {
		this(null,executor,wrappedEB);
	}

	public AsyncEventBus(String name, Executor executor, EventBus wrappedEB) {
		super(name);
		this.executor = executor;
		this.bus = wrappedEB;
		if(executor==null)
			throw new NullPointerException("Executor can't be null.");
		if(wrappedEB==null)
			throw new NullPointerException("Wrapped event bus can't be null.");
		if(bus instanceof AsyncEventBus)
			throw new IllegalArgumentException("AsyncEventBuses can't wrap each other.");
	}
	
	@Override
	public synchronized void setDeadEventHandler(
			IDeadEventHandler deadEventHandler) {
		bus.setDeadEventHandler(deadEventHandler);
	}

	@Override
	public synchronized void setSubscriberExceptionHandler(
			ISubscriberExceptionHandler exceptionHandler) {
		bus.setSubscriberExceptionHandler(exceptionHandler);
	}

	@Override
	public synchronized void setLogger(Logger logger) {
		bus.setLogger(logger);
	}

	@Override
	public EnumHierarchyStrategy getHierarchyStrategy() {
		return bus.getHierarchyStrategy();
	}

	@Override
	public ISubscriberStrategy getSubscriberStrategy() {
		return bus.getSubscriberStrategy();
	}

	@Override
	public synchronized IDeadEventHandler getDeadEventHandler() {
		return bus.getDeadEventHandler();
	}

	@Override
	public synchronized ISubscriberExceptionHandler getSubscriberExceptionHandler() {
		return bus.getSubscriberExceptionHandler();
	}

	@Override
	public synchronized Logger getLogger() {
		return bus.getLogger();
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
