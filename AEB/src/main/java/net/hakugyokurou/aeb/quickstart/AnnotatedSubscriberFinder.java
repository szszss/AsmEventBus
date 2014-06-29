package net.hakugyokurou.aeb.quickstart;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.hakugyokurou.aeb.strategy.ISubscriberStrategy;

public class AnnotatedSubscriberFinder implements ISubscriberStrategy{
	
	public static final AnnotatedSubscriberFinder QUICKSTART_SINGLETON = new AnnotatedSubscriberFinder(EventSubscriber.class);
	
	protected final Class<? extends Annotation> annotation;
	
	public AnnotatedSubscriberFinder(Class<? extends Annotation> annotation) {
		this.annotation = annotation;
	}

	public boolean isDependOnInstance() {
		return false;
	}

	public Method[] findSubscribers(Object handler) {
		Class<?> klass;
		if(handler instanceof Class<?>)
			klass = (Class<?>)handler;
		else 
			klass = handler.getClass();
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
