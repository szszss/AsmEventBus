package net.hakugyokurou.aeb.test;

import static org.junit.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import net.hakugyokurou.aeb.PriorEventBus;
import net.hakugyokurou.aeb.quickstart.EventSubscriber;
import net.hakugyokurou.aeb.strategy.EnumDispatchStrategy;
import net.hakugyokurou.aeb.strategy.IPriorityStrategy;

import org.junit.Test;

public class PriorEventBusTest {

	public int testNumber = 0;
	
	@Test
	public void testPEB() {
		PriorEventBus subject = new PriorEventBus(new TestPriorityStrategy(5, EnumDispatchStrategy.PRIORITY_FIRST));
		Handler1 handler1 = new Handler1();
		subject.register(handler1);
		subject.post(1);
		assertEquals(10, testNumber);
	}
	
	@Test
	public void testPEBHF() {
		PriorEventBus subject = new PriorEventBus(new TestPriorityStrategy(2, EnumDispatchStrategy.HIERARCHY_FIRST));
		Handler2 handler2 = new Handler2();
		subject.register(handler2);
		subject.post(2);
		assertEquals(2, testNumber);
		
		testNumber = 0;
		subject = new PriorEventBus(new TestPriorityStrategy(2, EnumDispatchStrategy.PRIORITY_FIRST));
		subject.register(handler2);
		subject.post(2);
		assertEquals(6, testNumber);
	}
	
	private class Handler1 {
		@EventSubscriber
		@TestPriority(priority=2)
		public void setNumberA(Integer i) {
			testNumber = i;
		}
		@EventSubscriber
		@TestPriority(priority=0)
		public void setNumberB(Integer i) {
			testNumber = i*10;
		}
		@EventSubscriber
		@TestPriority(priority=1)
		public void setNumberC(Integer i) {
			testNumber = i*100;
		}
		@EventSubscriber
		@TestPriority(priority=4)
		public void setNumberD(Integer i) {
			testNumber = i*1000;
		}
		@EventSubscriber
		@TestPriority(priority=3)
		public void setNumberE(Integer i) {
			testNumber = i*10000;
		}
	}
	
	private class Handler2 {
		@EventSubscriber
		@TestPriority(priority=0)
		public void setNumberA(Integer i) {
			testNumber = i;
		}
		@EventSubscriber
		@TestPriority(priority=1)
		public void setNumberB(Object i) {
			testNumber *= 3;
		}
	}
	
	private static class TestPriorityStrategy implements IPriorityStrategy {

		private int max;
		private EnumDispatchStrategy dispatchStrategy;
		
		public TestPriorityStrategy(int maxPriorities, EnumDispatchStrategy dispatchStrategy) {
			max = maxPriorities;
			this.dispatchStrategy = dispatchStrategy;
		}
		
		public int getPriorities() {
			return max;
		}

		public EnumDispatchStrategy getDispatchStrategy() {
			return dispatchStrategy;
		}

		public int judgePriority(Method subscriber) {
			TestPriority p = subscriber.getAnnotation(TestPriority.class);
			if(p!=null)
			{
				return p.priority();
			}
			return 2;
		}
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	private static @interface TestPriority {
		public int priority() default 2;
	}

}
