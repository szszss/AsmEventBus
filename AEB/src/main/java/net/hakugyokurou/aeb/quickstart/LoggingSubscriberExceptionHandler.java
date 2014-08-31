package net.hakugyokurou.aeb.quickstart;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.hakugyokurou.aeb.EventBus;
import net.hakugyokurou.aeb.auxiliary.ISubscriberExceptionHandler;

public class LoggingSubscriberExceptionHandler implements ISubscriberExceptionHandler{

	/**
	 * The singleton of {@link LoggingSubscriberExceptionHandler}.
	 */
	public static final LoggingSubscriberExceptionHandler SINGLETON = new LoggingSubscriberExceptionHandler();
	
	public void handleSubscriberException(EventBus eventBus, Object handler,
			Method subscriber, Object event, Throwable e) {
		Logger logger = eventBus.getLogger();
		logger.log(Level.SEVERE, "Event handler:"+handler+" met an exception in "+subscriber.getName()+". Cause:"+e.getCause());
	}
}
