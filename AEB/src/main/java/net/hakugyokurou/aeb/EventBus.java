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
 * <h2>How to use</h2>
 * <p>(1).Create a instance of event bus.<br/>
 * (2).Register subscriber.<br/>
 * (3).Post events.</p>
 * 
 * <h2>How to create</h2>
 * <p>You can just create the event bus by its constructor, or build it by {@link net.hakugyokurou.aeb.util.EventBusBuilder}.<br/>
 * There are 6 constructors in EventBus:<br/>
 * <i>(No param)</i>: An auto-generating name and default settings.<br/>
 * <i>String</i>: Use a custom name and default settings.<br/>
 * <i>String, EnumInvokerGenerator</i>: Use a custom name, custom invoker generator and default other settings.<br/>
 * <i>String, ISubscriberStrategy</i>: Use a custom name, custom subscriber strategy and default other settings.<br/>
 * <i>String, EnumHierarchyStrategy</i>: Use a custom name, custom hierarchy strategy and default other settings.<br/>
 * <i>String, EnumHierarchyStrategy, ISubscriberStrategy, EnumInvokerGenerator</i>: 
 * Use a custom name and custom settings.<br/><br/>
 * To get more information about settings (strategies), check the <b>see also</b>.</p>
 * 
 * <h2>How to register subscriber</h2>
 * <p>AsmEventBus allows user customize the strategy of subscriber. It has provided a preset strategy: 
 * {@link net.hakugyokurou.aeb.quickstart.AnnotatedSubscriberFinder} as a <b>default</b> subscriber strategy. 
 * It will find the methods which have a special annotation. If you want to use it, put a 
 * {@link net.hakugyokurou.aeb.quickstart.EventSubscriber} annotation in your subscriber method. 
 * The method must be public, non-abstract, no return and has (and only has) one non-primitive parameter. 
 * The parameter will be considered <b>acceptable event</b>. When post events, if the type of a event is equal to the 
 * type of acceptable event, the event will be sent to this method (However, even if they are not equal, if the type of 
 * acceptable event is the super class of type of event, it will also be sent).</p>
 * <p>To register a subscriber, uses:<br/>
 * <code>eventBus.register(subscriber);</code><br/>
 * The subscriber can be a instance of subscriber class, and it can also be the subscriber class, since event bus <b>allows 
 * static subscriber method</b>.<br/>
 * You can use eventbus.unregister to remove subscriber.</p>
 * 
 * <h2>How to post event</h2>
 * <p>You can use <code>eventbus.post(event)</code> to post event. This event bus is <b>sync event bus</b>, whose subscriber will be 
 * invoke in poster's thread. If you want the subscribers run in another thread, use {@link AsyncEventBus}.</p>
 * 
 * @author szszss
 * 
 * @see ISubscriberStrategy
 * @see EnumHierarchyStrategy
 * @see EnumInvokerGenerator
 * @see IDeadEventHandler
 * @see ISubscriberExceptionHandler
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
