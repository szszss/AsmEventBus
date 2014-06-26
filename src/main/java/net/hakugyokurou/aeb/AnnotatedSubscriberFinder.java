package net.hakugyokurou.aeb;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class AnnotatedSubscriberFinder implements ISubscriberFinder{
	
	protected final Class annotation;
	
	public AnnotatedSubscriberFinder(Class annotation) {
		this.annotation = annotation;
	}

	public Method[] findSubscribers(Class klass) {
		Method[] methods = klass.getMethods();
		ArrayList<Method> cache = new ArrayList<Method>(methods.length);
		for(Method method : methods)
		{
			if(method.getAnnotation(annotation)!=null)
			{
				Class[] params = method.getParameterTypes();
				if(!method.isVarArgs() && params.length==1 && method.getReturnType().equals(void.class))
					cache.add(method);
			}
		}
		return cache.toArray(new Method[cache.size()]);
	}
}
