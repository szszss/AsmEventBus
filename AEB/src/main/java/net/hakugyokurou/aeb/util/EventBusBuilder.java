package net.hakugyokurou.aeb.util;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import net.hakugyokurou.aeb.AsyncEventBus;
import net.hakugyokurou.aeb.EventBus;
import net.hakugyokurou.aeb.PriorEventBus;
import net.hakugyokurou.aeb.auxiliary.IDeadEventHandler;
import net.hakugyokurou.aeb.auxiliary.ISubscriberExceptionHandler;
import net.hakugyokurou.aeb.quickstart.AnnotatedSubscriberFinder;
import net.hakugyokurou.aeb.quickstart.LoggingSubscriberExceptionHandler;
import net.hakugyokurou.aeb.quickstart.SilentDeadEventHandler;
import net.hakugyokurou.aeb.strategy.EnumDispatchStrategy;
import net.hakugyokurou.aeb.strategy.EnumHierarchyStrategy;
import net.hakugyokurou.aeb.strategy.IPriorityStrategy;
import net.hakugyokurou.aeb.strategy.ISubscriberStrategy;

public class EventBusBuilder {
	
	protected static final int EVENT_BUS = 0;
	protected static final int PRIOR_EVENT_BUS = 1;
	protected static final int ASYNC_EVENT_BUS = 2;
	
	protected int type = EVENT_BUS;
	protected String name;
	protected Executor executor;
	protected EventBus wrapped;
	protected EnumDispatchStrategy dispatchStrategy = EnumDispatchStrategy.PRIORITY_FIRST;
	protected EnumHierarchyStrategy hierarchyStrategy = EnumHierarchyStrategy.EXTENDED_FIRST;
	protected IPriorityStrategy priorityStrategy = null;
	protected ISubscriberStrategy subscriberStrategy = AnnotatedSubscriberFinder.QUICKSTART_SINGLETON;
	protected IDeadEventHandler deadEventHandler = SilentDeadEventHandler.QUICKSTART_SINGLETON;
	protected ISubscriberExceptionHandler exceptionHandler = LoggingSubscriberExceptionHandler.QUICKSTART_SINGLETON;
	protected Logger logger;
	
	public static EventBusBuilder createBuilder() {
		return new EventBusBuilder();
	}
	
	public EventBusBuilder buildEventBus() { 
		type = EVENT_BUS;
		return this;
	}
	
	public EventBusBuilder buildPriorEventBus() { 
		type = EVENT_BUS;
		return this;
	}
	
	public EventBusBuilder buildAsyncEventBus() { 
		type = EVENT_BUS;
		return this;
	}
	
	public EventBusBuilder setName(String name) {
		this.name = name;
		return this;
	}
	
	public EventBusBuilder setExecutor(Executor executor) {
		this.executor = executor;
		return this;
	}
	
	public EventBusBuilder setWrappedEventBus(EventBus eb) {
		this.wrapped = eb;
		return this;
	}
	
	public EventBusBuilder setDispatchStrategy(EnumDispatchStrategy dispatchStrategy) {
		this.dispatchStrategy = dispatchStrategy;
		return this;
	}
	
	public EventBusBuilder setHierarchyStrategy(EnumHierarchyStrategy hierarchyStrategy) {
		this.hierarchyStrategy = hierarchyStrategy;
		return this;
	}
	
	public EventBusBuilder setPriorityStrategy(IPriorityStrategy priorityStrategy) {
		this.priorityStrategy = priorityStrategy;
		return this;
	}
	
	public EventBusBuilder setSubscriberStrategy(ISubscriberStrategy subscriberStrategy) {
		this.subscriberStrategy = subscriberStrategy;
		return this;
	}
	
	public EventBusBuilder setDeadEventHandler(IDeadEventHandler deadEventHandler) {
		this.deadEventHandler = deadEventHandler;
		return this;
	}
	
	public EventBusBuilder setSubscriberExceptionHandler(ISubscriberExceptionHandler subscriberExceptionHandler) {
		this.exceptionHandler = subscriberExceptionHandler;
		return this;
	}
	
	public EventBusBuilder setLogger(Logger logger) {
		this.logger = logger;
		return this;
	}
	
	public EventBus get() {
		EventBus eventBus;
		switch (type) {
		case PRIOR_EVENT_BUS:
			eventBus = new PriorEventBus(name, hierarchyStrategy, subscriberStrategy, priorityStrategy);
			break;
		case ASYNC_EVENT_BUS:
			eventBus = new AsyncEventBus(name, executor, wrapped);
			break;
		default:
			eventBus = new EventBus(name, hierarchyStrategy, subscriberStrategy);
			break; 
		}
		eventBus.setDeadEventHandler(deadEventHandler);
		eventBus.setSubscriberExceptionHandler(exceptionHandler);
		if(logger!=null)
			eventBus.setLogger(logger);
		return eventBus;
	}
}
