package net.hakugyokurou.aeb;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import net.hakugyokurou.aeb.EventInvoker.ReflectedEventInvoker;
import net.hakugyokurou.aeb.auxiliary.IDeadEventHandler;
import net.hakugyokurou.aeb.auxiliary.ISubscriberExceptionHandler;
import net.hakugyokurou.aeb.exception.AEBRegisterException;
import net.hakugyokurou.aeb.generator.InvokerGenerator;
import net.hakugyokurou.aeb.generator.AsmInvokerGenerator;
import net.hakugyokurou.aeb.generator.ReflectionInvokerGenerator;
import net.hakugyokurou.aeb.quickstart.AnnotatedSubscriberFinder;
import net.hakugyokurou.aeb.quickstart.EventSubscriber;
import net.hakugyokurou.aeb.quickstart.LoggingSubscriberExceptionHandler;
import net.hakugyokurou.aeb.quickstart.DiscardDeadEventHandler;
import net.hakugyokurou.aeb.strategy.EnumHierarchyStrategy;
import net.hakugyokurou.aeb.strategy.EnumInvokerGenerator;
import net.hakugyokurou.aeb.strategy.ISubscriberStrategy;

/**
 * EventBus can dispatch events to event listeners which have registered.
 * 
 * <h2></h2>
 * @author szszss
 */
public class EventBus {
	
	private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
	
	protected final transient int id;
	protected final String name;
	protected final EnumHierarchyStrategy hierarchyStrategy;
	protected final ISubscriberStrategy subscriberStrategy;
	protected final boolean baseOnInstance;
	
	protected IDeadEventHandler deadEventHandler;
	protected ISubscriberExceptionHandler exceptionHandler;
	protected Logger logger;
	
	protected static Map<Method, EventInvoker> eventInvokerCache = Collections.synchronizedMap(new WeakHashMap<Method, EventInvoker>(64));
	
	protected final InvokerGenerator invokerGenerator;
	protected Map<Class<?>, EventDispatcher> eventMappingInvoker = new WeakHashMap<Class<?>, EventDispatcher>();
	protected ReadWriteLock eventMappingInvokerLock = new ReentrantReadWriteLock();
	
	protected Map<Object, Method[]> handlerMappingMethods = Collections.synchronizedMap(new WeakHashMap<Object, Method[]>());
	
	
	public EventBus() {
		this(null);
	}
	
	public EventBus(String name) {
		this(name, getDefaultSubscriberStrategy());
	}
	
	public EventBus(String name, EnumInvokerGenerator invokerGenerator) {
		this(name, EnumHierarchyStrategy.EXTENDED_FIRST, getDefaultSubscriberStrategy(), EnumInvokerGenerator.getDefault());
	}
	
	public EventBus(String name, ISubscriberStrategy subscriberStrategy) {
		this(name, EnumHierarchyStrategy.EXTENDED_FIRST, subscriberStrategy, EnumInvokerGenerator.getDefault());
	}
	
	public EventBus(String name, EnumHierarchyStrategy hierarchyStrategy) {
		this(name, hierarchyStrategy, getDefaultSubscriberStrategy(), EnumInvokerGenerator.getDefault());
	}
	
	public EventBus(String name, EnumHierarchyStrategy hierarchyStrategy, 
			ISubscriberStrategy subscriberStrategy, EnumInvokerGenerator invokerGenerator) {
		this.id = ID_GENERATOR.getAndIncrement();
		this.name = name==null?getDefaultName():name;
		this.hierarchyStrategy = hierarchyStrategy;
		this.subscriberStrategy = subscriberStrategy;
		this.baseOnInstance = subscriberStrategy.isDependOnInstance();
		switch (invokerGenerator) {
		case ASM:
			this.invokerGenerator = new AsmInvokerGenerator();
			break;
		default:
			this.invokerGenerator = new ReflectionInvokerGenerator();
			break;
		}
	}
	
	protected synchronized static String getDefaultName() {
		return "EventBus"+(System.nanoTime()%10000L);
	}
	
	public static ISubscriberStrategy getDefaultSubscriberStrategy() {
		return AnnotatedSubscriberFinder.SINGLETON;
	}
	
	public static IDeadEventHandler getDefaultDeadEventHandler() {
		return DiscardDeadEventHandler.SINGLETON;
	}
	
	public static ISubscriberExceptionHandler getDefaultSubscriberExceptionHandler() {
		return LoggingSubscriberExceptionHandler.SINGLETON;
	}
	
	public static Logger getDefaultLogger(EventBus eventBus) {
		return Logger.getLogger(eventBus.getClass().getName()+"."+eventBus.getName());
	}
	
	public synchronized void setDeadEventHandler(IDeadEventHandler deadEventHandler) {
		this.deadEventHandler = deadEventHandler;
	}
	
	public synchronized void setSubscriberExceptionHandler(ISubscriberExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}
	
	public synchronized void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public EnumHierarchyStrategy getHierarchyStrategy() {
		return hierarchyStrategy;
	}
	
	public ISubscriberStrategy getSubscriberStrategy() {
		return subscriberStrategy;
	}
	
	public synchronized IDeadEventHandler getDeadEventHandler() {
		if(deadEventHandler==null)
			deadEventHandler = getDefaultDeadEventHandler();
		return deadEventHandler;
	}
	
	public synchronized ISubscriberExceptionHandler getSubscriberExceptionHandler() {
		if(exceptionHandler==null)
			exceptionHandler = getDefaultSubscriberExceptionHandler();
		return exceptionHandler;
	}
	
	public synchronized Logger getLogger() {
		if(logger==null)
			logger = getDefaultLogger(this);
		return logger;
	}
	
	public void register(Object handler) {
		Class<?> klass = handler instanceof Class ? (Class)handler : handler.getClass();
		Method[] methods = subscriberStrategy.findSubscribers(handler);
		//EventInvoker[] invokers = new EventInvoker[methods.length];
		//int index=0;
		boolean hasError = false; //TODO:What should we do if there are wrong things?
		for(Method method : methods) {
			Class<?> event = method.getParameterTypes()[0];
			EventInvoker invoker = eventInvokerCache.get(method);
			if(invoker==null)
			{
				try {
					invoker = invokerGenerator.generateInvoker(klass, method, event);
					eventInvokerCache.put(method, invoker);
					//invokers[index++] = invoker;
				} catch (AEBRegisterException e) {
					e.printStackTrace();
					hasError = true;
					continue;
				}
			}
			addReceiver(event, handler, method, invoker);
		}
		handlerMappingMethods.put(handler, methods);
	}
	
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
					new EventDispatcher.SPEventDispatcherEF(this, event):new EventDispatcher.SPEventDispatcherSF(this, event);
			eventMappingInvokerLock.writeLock().lock();
			try {
				dealHierarchy(dispatcher);
				eventMappingInvoker.put(event, dispatcher);
			}
			finally {
				eventMappingInvokerLock.writeLock().unlock();
			}
		}
		dispatcher.addReceiver(handler, invoker);
	}
	
	protected final void dealHierarchy(EventDispatcher dispatcher) {
		for(EventDispatcher o2 : eventMappingInvoker.values())
		{
			if(o2.isSuper(dispatcher))
			{
				if(dispatcher.getParent()==null || o2.isSuper(dispatcher.getParent()))
				{
					dispatcher.setParent(o2);
				}
			}
			else if(dispatcher.isSuper(o2))
			{
				if(o2.getParent()==null  || dispatcher.isSuper(o2.getParent()))
				{
					o2.setParent(dispatcher);
				}
			}
				
		}
	}
	
	protected final void repairHierarchy(EventDispatcher dispatcher) {
		eventMappingInvokerLock.writeLock().lock();
		try {
			dealHierarchy(dispatcher);
		}
		finally {
			eventMappingInvokerLock.writeLock().unlock();
		}
	}
	
	public void unregister(Object handler) {
		Class<?> klass = handler instanceof Class ? (Class)handler : handler.getClass();
		Method[] methods = handlerMappingMethods.get(handler);
		if(methods==null)
		{
			//TODO:DO SOMETHINGS
			return;
		}
		boolean hasError = false;
		for(Method method : methods) {
			Class<?> event = method.getParameterTypes()[0];
			EventInvoker invoker = eventInvokerCache.get(method);
			if(invoker==null)
			{
				continue;
			}
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
				continue;
			}
			dispatcher.removeReceiver(handler, invoker);
		}
		//TODO:This method is a big sucker, fix it on some day.
	}
	
	public void post(Object event) {
		EventDispatcher dispatcher;
		eventMappingInvokerLock.readLock().lock();
		try {
			dispatcher = eventMappingInvoker.get(event.getClass());
		}
		finally {
			eventMappingInvokerLock.readLock().unlock();
		}
		boolean dead = true;
		if(dispatcher!=null)
		{
			dead = !dispatcher.post(event);
		}
		if(dead)
		{
			getDeadEventHandler().handleDeadEvent(this, event);
		}
	}
}
