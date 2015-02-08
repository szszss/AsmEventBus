package net.hakugyokurou.aeb.generator;

import java.lang.reflect.Method;

import net.hakugyokurou.aeb.EventInvoker;
import net.hakugyokurou.aeb.exception.AEBRegisterException;

public interface InvokerGenerator {
	public abstract EventInvoker generateInvoker(Class<?> handler, Method subscriber, Class<?> event) throws AEBRegisterException;
}
