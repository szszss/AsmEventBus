package net.hakugyokurou.aeb.quickstart;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventSubscriber {
	public final static int PRIORITY_HIGHEST = 4;
	public final static int PRIORITY_HIGH    = 3;
	public final static int PRIORITY_NORMAL  = 2;
	public final static int PRIORITY_LOW     = 1;
	public final static int PRIORITY_LOWEST  = 0;
	public int priority() default PRIORITY_NORMAL;
}
