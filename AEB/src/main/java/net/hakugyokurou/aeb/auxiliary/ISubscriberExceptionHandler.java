package net.hakugyokurou.aeb.auxiliary;

import java.lang.reflect.Method;

import net.hakugyokurou.aeb.EventBus;

public interface ISubscriberExceptionHandler {

	public abstract void handleSubscriberException(EventBus eventBus, Object handler, Method subscriber, Object event, Throwable e);
}
