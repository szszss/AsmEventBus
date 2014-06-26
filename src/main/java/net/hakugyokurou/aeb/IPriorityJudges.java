package net.hakugyokurou.aeb;

import java.lang.reflect.Method;

public interface IPriorityJudges {
	
	public abstract int getPriorities();
	
	public abstract int judgePriority(Method subscriber);

}
