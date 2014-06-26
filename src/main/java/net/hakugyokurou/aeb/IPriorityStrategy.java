package net.hakugyokurou.aeb;

import java.lang.reflect.Method;

public interface IPriorityStrategy {
	
	public abstract int getPriorities();
	
	public abstract EnumDispatchStrategy getDispatchStrategy();
	
	public abstract int judgePriority(Method subscriber);
}
