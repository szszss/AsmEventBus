package net.hakugyokurou.aeb.quickstart;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.hakugyokurou.aeb.strategy.ISubscriberFinder;

public class AnnotatedSubscriberFinder implements ISubscriberFinder{
	
	protected final Class<? extends Annotation> annotation;
	
	public AnnotatedSubscriberFinder(Class<? extends Annotation> annotation) {
		this.annotation = annotation;
	}

	public Method[] findSubscribers(Class<?> klass) {
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
