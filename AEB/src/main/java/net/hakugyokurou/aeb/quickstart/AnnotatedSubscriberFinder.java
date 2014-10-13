package net.hakugyokurou.aeb.quickstart;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.hakugyokurou.aeb.strategy.EnumDispatchStrategy;
import net.hakugyokurou.aeb.strategy.IPriorityStrategy;
import net.hakugyokurou.aeb.strategy.ISubscriberStrategy;

/**
 * AnnotatedSubscriberFinder is a quick-starting implement of ISubscriberStrategy.
 * It will find the methods which have a given annotation from the handler.
 * 
 * @author szszss
 */
public class AnnotatedSubscriberFinder implements ISubscriberStrategy {
	
	/**
	 * The singleton of {@link AnnotatedSubscriberFinder}.
	 */
	public static final AnnotatedSubscriberFinder SINGLETON = new AnnotatedSubscriberFinder(EventSubscriber.class);
	
	protected final Class<? extends Annotation> annotation;
	
	public AnnotatedSubscriberFinder(Class<? extends Annotation> annotation) {
		this.annotation = annotation;
	}
	
	/**
	 * {@link AnnotatedSubscriberFinder} doesn't depend on the instance.
	 */
	public boolean isDependOnInstance() {
		return false;
	}

	public Method[] findSubscribers(Object handler) {
		Class<?> klass;
		if(handler instanceof Class<?>)
			klass = (Class<?>)handler;
		else 
			klass = handler.getClass(); //If the handler is an instance, get the class.
		Method[] methods = klass.getMethods();
		ArrayList<Method> cache = new ArrayList<Method>(methods.length);
		for(Method method : methods)
		{
			if(method.getAnnotation(annotation)!=null)
			{
				Class<?>[] params = method.getParameterTypes();
				if(!method.isVarArgs() && params.length==1 && method.getReturnType().equals(void.class))
					cache.add(method);
			}
		}
		return cache.toArray(new Method[cache.size()]);
	}
}
