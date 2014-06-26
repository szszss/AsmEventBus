package net.hakugyokurou.aeb;

import java.lang.reflect.Method;

public interface ISubscriberFinder {
	
	public abstract Method[] findSubscribers(Class klass);

}
