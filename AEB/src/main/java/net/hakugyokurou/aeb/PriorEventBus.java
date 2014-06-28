package net.hakugyokurou.aeb;

import java.lang.reflect.Method;

import net.hakugyokurou.aeb.strategy.EnumDispatchStrategy;
import net.hakugyokurou.aeb.strategy.EnumHierarchyStrategy;
import net.hakugyokurou.aeb.strategy.IPriorityStrategy;
import net.hakugyokurou.aeb.strategy.ISubscriberFinder;

//Prioritized
public class PriorEventBus extends EventBus{

	private final int priorities;
	private final EnumDispatchStrategy dispatchStrategy;
	private final IPriorityStrategy priorityStrategy;
	
	public PriorEventBus(IPriorityStrategy judges) {
		this(getName(),judges);
	}

	public PriorEventBus(String name, IPriorityStrategy priorityStrategy) {
		this(name, getDefaultSubscriberFinder(), priorityStrategy);
	}
	
	public PriorEventBus(String name, EnumHierarchyStrategy hierarchyStrategy, IPriorityStrategy priorityStrategy) {
		this(name, hierarchyStrategy, getDefaultSubscriberFinder(), priorityStrategy);
	}
	
	public PriorEventBus(String name, ISubscriberFinder finder, IPriorityStrategy priorityStrategy) {
		this(name, EnumHierarchyStrategy.EXTENDED_FIRST, finder, priorityStrategy);
	}
	
	public PriorEventBus(String name, EnumHierarchyStrategy hierarchyStrategy, ISubscriberFinder finder, IPriorityStrategy priorityStrategy) {
		super(name, finder);
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
