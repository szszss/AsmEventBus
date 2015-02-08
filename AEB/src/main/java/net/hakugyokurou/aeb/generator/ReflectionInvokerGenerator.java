package net.hakugyokurou.aeb.generator;

import java.lang.reflect.Method;

import net.hakugyokurou.aeb.EventInvoker;
import net.hakugyokurou.aeb.EventInvoker.ReflectedEventInvoker;
import net.hakugyokurou.aeb.exception.AEBRegisterException;

public class ReflectionInvokerGenerator implements InvokerGenerator {
	
	public EventInvoker generateInvoker(Class<?> handler,
			Method subscriber, Class<?> event) throws AEBRegisterException {
		return new ReflectedEventInvoker(subscriber);
	}
}
