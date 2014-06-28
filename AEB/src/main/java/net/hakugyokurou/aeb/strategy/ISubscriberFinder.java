package net.hakugyokurou.aeb.strategy;

import java.lang.reflect.Method;

public interface ISubscriberFinder {
	
	public abstract Method[] findSubscribers(Class<?> klass);

}
