package net.hakugyokurou.aeb.strategy;

import java.lang.reflect.Method;

/**
 * IPriorityStrategy is used by PriorEventBus. It is used to decide which priority subscriber belonged to, 
 * how many priorities dose the prior event bus have, and how the event dispatch.<br/>
 * It has a default implement: AnnotatedPriorityJudger.
 * 
 * @author szszss
 * 
 * @see AnnotatedPriorityJudge
 * @see EnumDispatchStrategy
 */
public interface IPriorityStrategy {
	
	public abstract int getPriorities();
	
	public abstract EnumDispatchStrategy getDispatchStrategy();
	
	public abstract int judgePriority(Method subscriber);
}
