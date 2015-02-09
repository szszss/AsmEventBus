package net.hakugyokurou.aeb.test;

import static org.junit.Assert.*;
import net.hakugyokurou.aeb.EventBus;
import net.hakugyokurou.aeb.auxiliary.IDeadEventHandler;
import net.hakugyokurou.aeb.quickstart.EventSubscriber;
import net.hakugyokurou.aeb.strategy.EnumHierarchyStrategy;

import org.junit.Before;
import org.junit.Test;

public class EventBusTest {
	
	//private EventBus subject;
	private volatile int testNumber=0;
	
	@Before
	public void setUp() {
		testNumber = 0;
	}

	@Test
	public void testEB() {
		EventBus subject = new EventBus();
		Handler1 handler1 = new Handler1();
		//Register
		subject.register(handler1);
		//Post
		subject.post(Integer.valueOf(10));
		assertEquals(10, testNumber);
		//Unregister
		subject.unregister(handler1);
		subject.post(Integer.valueOf(0));
		assertEquals(10, testNumber);
	}
	
	@Test
	public void testEBHierarchy() {
		EventBus subject = new EventBus();
		Handler2a handler2a = new Handler2a();
		Handler2b handler2b = new Handler2b();
		subject.register(handler2a);
		subject.register(handler2b);
		subject.post(Integer.valueOf(2));
		//handler2a first got the event, and added 2. And than handler2b got the event, multiplied 3. Now is 6.
		subject.post(new Object());
		//Only handler2b got the event and multiplied 3. Now is 18.
		assertEquals(18, testNumber);
		//Let's test another.
		testNumber=0;
		subject = new EventBus("", EnumHierarchyStrategy.SUPER_FIRST);
		subject.register(handler2a);
		subject.register(handler2b);
		subject.post(Integer.valueOf(2));
		//handler2b first got the event, 0x3=0. And than handler2a got the event. Now is 2.
		subject.post(new Object());
		//Only handler2b got the event and multiplied 3. Now is 6.
		assertEquals(6, testNumber);
	}
	
	@Test
	public void testEBDeadEvent() {
		EventBus subject = new EventBus();
		subject.setDeadEventHandler(new DeadEventHandler());
		Handler3 handler3 = new Handler3();
		subject.post(Integer.valueOf(1));
		assertEquals(10, testNumber);
		subject.register(handler3);
		subject.post(Integer.valueOf(1));
		assertEquals(1, testNumber);
		subject.unregister(handler3);
		subject.post(Integer.valueOf(0));
		assertEquals(10, testNumber);
	}
	
	@Test
	public void testEBStaticMethod() {
		EventBus subject = new EventBus();
		subject.register(StaticMethodHandler.class);
		subject.post(Integer.valueOf(9));
		assertEquals(9, StaticMethodHandler.num);
		subject.register(new StaticMethodHandler());
		subject.post(Integer.valueOf(6));
		assertEquals(6, StaticMethodHandler.num);
	}
	
	private class Handler1 {	
		@EventSubscriber
		public void setNumber(Integer i) {
			testNumber = i;
		}
	}
	
	private class Handler2a {
		@EventSubscriber
		public void addNumber(Integer i) {
			testNumber += i;
		}	
	}
	
	private class Handler2b {
		@EventSubscriber
		public void mulNumber(Object i) {			
			testNumber *= 3;
		}	
	}
	
	private class Handler3 {	
		@EventSubscriber
		public void getDeadEvent(Integer e) {
			testNumber = (Integer)e;
		}
	}
	
	private class DeadEventHandler implements IDeadEventHandler {

		public void handleDeadEvent(EventBus eventBus, Object event) {
			testNumber = 10;
		}	
	}
	
	private static class StaticMethodHandler {
		private static int num;
		@EventSubscriber
		public static void setNumber(Integer i) {
			num = i;
		}
		public static int getNumber() {
			return num;
		}
	}
}
