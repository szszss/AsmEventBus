package net.hakugyokurou.aeb.quickstart;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.hakugyokurou.aeb.strategy.EnumDispatchStrategy;
import net.hakugyokurou.aeb.strategy.IPriorityStrategy;

public class AnnotatedPriorityJudger implements IPriorityStrategy{

	/**
	 * The singleton of {@link AnnotatedPriorityJudger}.
	 */
	public static final AnnotatedPriorityJudger SINGLETON = new AnnotatedPriorityJudger(EventSubscriber.class);
	
	protected final Class<? extends EventSubscriber> annotation;
	protected final EnumDispatchStrategy dispatchStrategy;
	
	public AnnotatedPriorityJudger(Class<? extends EventSubscriber> annotation) {
		this(annotation, EnumDispatchStrategy.PRIORITY_FIRST);
	}
	
	public AnnotatedPriorityJudger(Class<? extends EventSubscriber> annotation, EnumDispatchStrategy dispatchStrategy) {
		this.annotation = annotation;
		this.dispatchStrategy = dispatchStrategy;
	}
	
	public int getPriorities() {
		return EventSubscriber.PRIORITY_HIGHEST;
	}

	public EnumDispatchStrategy getDispatchStrategy() {
		return dispatchStrategy;
	}

	public int judgePriority(Method subscriber) {
		EventSubscriber eventSubscriber = subscriber.getAnnotation(annotation);
		if(eventSubscriber!=null)
		{
			return eventSubscriber.priority();
		}
		return EventSubscriber.PRIORITY_NORMAL;
	}

}
