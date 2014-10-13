package net.hakugyokurou.aeb;

import java.lang.reflect.Method;

import net.hakugyokurou.aeb.strategy.EnumDispatchStrategy;
import net.hakugyokurou.aeb.strategy.EnumHierarchyStrategy;
import net.hakugyokurou.aeb.strategy.EnumInvokerGenerator;
import net.hakugyokurou.aeb.strategy.IPriorityStrategy;
import net.hakugyokurou.aeb.strategy.ISubscriberStrategy;

//Prioritized
public class PriorEventBus extends EventBus{

	private final int priorities;
	private final EnumDispatchStrategy dispatchStrategy;
	private final IPriorityStrategy priorityStrategy;
	
	public PriorEventBus(IPriorityStrategy priorityStrategy) {
		this(null,priorityStrategy);
	}

	public PriorEventBus(String name, IPriorityStrategy priorityStrategy) {
		this(name, getDefaultSubscriberStrategy(), priorityStrategy);
	}
	
	public PriorEventBus(String name, EnumHierarchyStrategy hierarchyStrategy, IPriorityStrategy priorityStrategy) {
		this(name, hierarchyStrategy, getDefaultSubscriberStrategy(), EnumInvokerGenerator.getDefault(), priorityStrategy);
	}
	
	public PriorEventBus(String name, ISubscriberStrategy subscriberStrategy, IPriorityStrategy priorityStrategy) {
		this(name, EnumHierarchyStrategy.EXTENDED_FIRST, subscriberStrategy, EnumInvokerGenerator.getDefault(), priorityStrategy);
	}
	
	public PriorEventBus(String name, EnumInvokerGenerator invokerGenerator, IPriorityStrategy priorityStrategy) {
		this(name, EnumHierarchyStrategy.EXTENDED_FIRST, getDefaultSubscriberStrategy(), invokerGenerator, priorityStrategy);
	}
	
	public PriorEventBus(String name, EnumHierarchyStrategy hierarchyStrategy, ISubscriberStrategy subscriberStrategy, 
			EnumInvokerGenerator invokerGenerator, IPriorityStrategy priorityStrategy) {
		super(name, hierarchyStrategy, subscriberStrategy, invokerGenerator);
		if(priorityStrategy==null)
			throw new NullPointerException("PriorityStrategy can't be null.");
		this.priorities = priorityStrategy.getPriorities();
		this.dispatchStrategy = priorityStrategy.getDispatchStrategy();
		this.priorityStrategy = priorityStrategy;
	}

	@Override
	protected void addReceiver(Class<?> event, Object handler, Method subscriber, EventInvoker invoker) {
		EventDispatcher dispatcher;
		eventMappingInvokerLock.readLock().lock();
		try {
			dispatcher = eventMappingInvoker.get(event);
		}
		finally {
			eventMappingInvokerLock.readLock().unlock();
		}
		if(dispatcher==null)
		{
			dispatcher = hierarchyStrategy==EnumHierarchyStrategy.EXTENDED_FIRST?
					dispatchStrategy==EnumDispatchStrategy.PRIORITY_FIRST?new EventDispatcher.MPEventDispatcherPFEF(this, priorities, event):
																		  new EventDispatcher.MPEventDispatcherHFEF(this, priorities, event):
					dispatchStrategy==EnumDispatchStrategy.PRIORITY_FIRST?new EventDispatcher.MPEventDispatcherPFSF(this, priorities, event):
																		  new EventDispatcher.MPEventDispatcherHFSF(this, priorities, event);
			eventMappingInvokerLock.writeLock().lock();
			try {
				dealHierarchy(dispatcher);
				eventMappingInvoker.put(event, dispatcher);
			}
			finally {
				eventMappingInvokerLock.writeLock().unlock();
			}
		}
		//int priority = priorityStrategy.judgePriority(subscriber);
		dispatcher.addReceiver(handler, invoker, priorityStrategy.judgePriority(subscriber));
	}
}
