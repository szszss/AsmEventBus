package net.hakugyokurou.aeb;

import java.lang.reflect.Method;

public interface IPriorityStrategy {
	
	public abstract int getPriorities();
	
	public abstract DispatchStrategy getDispatchStrategy();
	
	public abstract int judgePriority(Method subscriber);
	
	public static enum DispatchStrategy {
		HIERARCHY_FIRST,
		PRIORITY_FIRST;
	}
}
