package net.hakugyokurou.aeb.generator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.hakugyokurou.aeb.EventInvoker;
import net.hakugyokurou.aeb.exception.AEBRegisterException;

public class ReflectionInvokerGenerator implements InvokerGenerator {
	
	public EventInvoker generateInvoker(Class<?> handler,
			Method subscriber, Class<?> event) throws AEBRegisterException {
		if((subscriber.getModifiers() & Modifier.SYNCHRONIZED) > 0)
			return new net.hakugyokurou.aeb.EventInvoker.SyncReflectedEventInvoker(subscriber);
		else 
			return new net.hakugyokurou.aeb.EventInvoker.AsyncReflectedEventInvoker(subscriber);
	}
}
