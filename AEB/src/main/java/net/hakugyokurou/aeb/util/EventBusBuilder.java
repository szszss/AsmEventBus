package net.hakugyokurou.aeb.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.hakugyokurou.aeb.AsyncEventBus;
import net.hakugyokurou.aeb.EventBus;
import net.hakugyokurou.aeb.PriorEventBus;
import net.hakugyokurou.aeb.auxiliary.IDeadEventHandler;
import net.hakugyokurou.aeb.auxiliary.ISubscriberExceptionHandler;
import net.hakugyokurou.aeb.quickstart.AnnotatedPriorityJudge;
import net.hakugyokurou.aeb.quickstart.AnnotatedSubscriberFinder;
import net.hakugyokurou.aeb.quickstart.LoggingSubscriberExceptionHandler;
import net.hakugyokurou.aeb.quickstart.DiscardDeadEventHandler;
import net.hakugyokurou.aeb.strategy.EnumDispatchStrategy;
import net.hakugyokurou.aeb.strategy.EnumHierarchyStrategy;
import net.hakugyokurou.aeb.strategy.EnumInvokerGenerator;
import net.hakugyokurou.aeb.strategy.IPriorityStrategy;
import net.hakugyokurou.aeb.strategy.ISubscriberStrategy;

/**
 * EventBusBuilder is a tool which can create a EventBus in Builder Pattern.<br/>
 * <p>Examples:</p>
 * 
 * <p><code>
 *    //Build a normal event bus<br/><br/>
 *    //Build a normal EB with default settings:<br/>
 *    EventBusBuilder builder = EventBusBuilder.createBuilder().get();<br/><br/>
 *    //Build a normal EB with a custom name and hierarchy strategy:<br/>
 *    EventBusBuilder builder = EventBusBuilder.createBuilder().
 *    buildEventBus.setName("Power★Daze").setHierarchyStrategy(EnumHierarchyStrategy.SUPER_FIRST).get();<br/>
 * </code></p>
 * <p><code>
 *    //Build a prioritized event bus with default settings:<br/>
 *    EventBusBuilder builder = EventBusBuilder.createBuilder().buildPriorEventBus().get();<br/><br/>
 * </code></p>
 * <p><code>
 *    //Build an async event bus with auto-generated wrapped normal event bus and default settings:<br/>
 *    EventBusBuilder builder = EventBusBuilder.createBuilder().buildAsyncEventBus().get();<br/><br/>
 * </code></p>
 * @author szszss
 */
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
	protected EnumInvokerGenerator invokerGenerator = EnumInvokerGenerator.getDefault(); //ASM
	protected IPriorityStrategy priorityStrategy = AnnotatedPriorityJudge.SINGLETON;
	protected ISubscriberStrategy subscriberStrategy = AnnotatedSubscriberFinder.SINGLETON;
	protected IDeadEventHandler deadEventHandler = DiscardDeadEventHandler.SINGLETON;
	protected ISubscriberExceptionHandler exceptionHandler = LoggingSubscriberExceptionHandler.SINGLETON;
	protected Logger logger;
	
	/**
	 * Create an instance of EventBusBuilder.
	 * @return An instance of EventBusBuilder
	 */
	public static EventBusBuilder createBuilder() {
		return new EventBusBuilder();
	}
	
	/**
	 * Request for building a normal event bus. This is the default type.
	 * @return Builder
	 */
	public EventBusBuilder buildEventBus() { 
		type = EVENT_BUS;
		return this;
	}
	
	/**
	 * Request for building a prioritized event bus.
	 * @return Builder
	 */
	public EventBusBuilder buildPriorEventBus() { 
		type = EVENT_BUS;
		return this;
	}
	
	/**
	 * Request for building an async event bus.
	 * @return Builder
	 */
	public EventBusBuilder buildAsyncEventBus() { 
		type = EVENT_BUS;
		return this;
	}
	
	/**
	 *  Set the name of event bus. This is nullable, since event bus will get a default name if this is null.
	 * @param name The name of event bus. Nullable
	 * @return Builder
	 */
	public EventBusBuilder setName(String name) {
		this.name = name;
		return this;
	}
	
	/**
	 * Set an executor for async event bus. Although the executor is non-nullable in asyncEB,
	 * this param is nullable, because it will create a single thread executor as default executor.
	 * However, this is feature is just a fool-proofing. Users shouldn't depend on this feature.
	 * @param executor The executor of async event bus. Nullable
	 * @return Builder
	 */
	public EventBusBuilder setExecutor(Executor executor) {
		this.executor = executor;
		return this;
	}
	
	/**
	 * Set a sync event bus for async event bus. Although the wrapped event bus is non-nullable in asyncEB,
	 * this param is nullable, because it will create a normal event bus with default settings as default wrappedEB.
	 * However, this is feature is just a fool-proofing. Users shouldn't depend on this feature.
	 * @param wrapped The wrapped sync event bus of async event bus. Nullable
	 * @return Builder
	 */
	public EventBusBuilder setWrappedEventBus(EventBus eb) {
		this.wrapped = eb;
		return this;
	}
	
	/**
	 * Set the dispatch strategy for prioritized event bus. The default value is PRIORITY_FIRST.
	 * @param dispatchStrategy The dispatch strategy of prioritized event bus
	 * @return Builder
	 * @see EnumDispatchStrategy
	 */
	public EventBusBuilder setDispatchStrategy(EnumDispatchStrategy dispatchStrategy) {
		this.dispatchStrategy = dispatchStrategy;
		return this;
	}
	
	/**
	 * Set the hierarchy strategy. The default value is EXTENDED_FIRST.
	 * @param hierarchyStrategy The hierarchy strategy of event bus
	 * @return Builder
	 * @see EnumHierarchyStrategy
	 */
	public EventBusBuilder setHierarchyStrategy(EnumHierarchyStrategy hierarchyStrategy) {
		this.hierarchyStrategy = hierarchyStrategy;
		return this;
	}
	
	/**
	 * Set the priority strategy for prioritized event bus. The default value is the singleton of {@link AnnotatedPriorityJudge}.
	 * @param priorityStrategy The priority strategy of prioritized event bus
	 * @return Builder
	 * @see IPriorityStrategy
	 */
	public EventBusBuilder setPriorityStrategy(IPriorityStrategy priorityStrategy) {
		this.priorityStrategy = priorityStrategy;
		return this;
	}
	
	/**
	 * Set the subscriber strategy for event bus. The default value is the singleton of {@link AnnotatedSubscriberFinder}.
	 * @param subscriberStrategy The subscriber strategy of event bus
	 * @return Builder
	 * @see ISubscriberStrategy
	 */
	public EventBusBuilder setSubscriberStrategy(ISubscriberStrategy subscriberStrategy) {
		this.subscriberStrategy = subscriberStrategy;
		return this;
	}
	
	/**
	 * Set the dead event handler for event bus. The default value is the singleton of {@link DiscardDeadEventHandler}.
	 * @param deadEventHandler The dead event handler of event bus.
	 * @return Builder
	 * @see IDeadEventHandler
	 */
	public EventBusBuilder setDeadEventHandler(IDeadEventHandler deadEventHandler) {
		this.deadEventHandler = deadEventHandler;
		return this;
	}
	
	/**
	 * Set the subscriber exception handler for event bus. The default value is the singleton of {@link LoggingSubscriberExceptionHandler}.
	 * @param subscriberExceptionHandler The subscriber exception handler of event bus.
	 * @return Builder
	 * @see ISubscriberExceptionHandler
	 */
	public EventBusBuilder setSubscriberExceptionHandler(ISubscriberExceptionHandler subscriberExceptionHandler) {
		this.exceptionHandler = subscriberExceptionHandler;
		return this;
	}
	
	/**
	 * Set the logger for event bus. This is nullable.
	 * @param logger The logger of event bus.
	 * @return Builder
	 */
	public EventBusBuilder setLogger(Logger logger) {
		this.logger = logger;
		return this;
	}
	
	/**
	 * Build the event bus with the settings. After build, the builder is still available. 
	 * However, you may need to change the name of new event bus.
	 * @return An instance of event bus you requested
	 */
	public EventBus get() {
		EventBus eventBus;
		switch (type) {
		case PRIOR_EVENT_BUS:
			eventBus = new PriorEventBus(name, hierarchyStrategy, subscriberStrategy, EnumInvokerGenerator.getDefault(), priorityStrategy);
			break;
		case ASYNC_EVENT_BUS:
			EventBus eb = wrapped;
			Executor exe = executor;
			if(eb==null)
				eb = new EventBus(name + "$DEFAULT_EB", hierarchyStrategy, subscriberStrategy, EnumInvokerGenerator.getDefault());
			if(exe==null)
				exe = Executors.newSingleThreadExecutor();
			eventBus = new AsyncEventBus(name, executor, eb);
			break;
		default:
			eventBus = new EventBus(name, hierarchyStrategy, subscriberStrategy, EnumInvokerGenerator.getDefault());
			break; 
		}
		eventBus.setDeadEventHandler(deadEventHandler);
		eventBus.setSubscriberExceptionHandler(exceptionHandler);
		if(logger!=null)
			eventBus.setLogger(logger);
		return eventBus;
	}
}
