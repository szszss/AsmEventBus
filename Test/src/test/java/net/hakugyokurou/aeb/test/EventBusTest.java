package net.hakugyokurou.aeb.test;

import static org.junit.Assert.*;
import net.hakugyokurou.aeb.EventBus;
import net.hakugyokurou.aeb.EventSubscriber;

import org.junit.Before;
import org.junit.Test;

public class EventBusTest {
	
	//private EventBus subject;
	private volatile int testNumber=0;

	@Test
	public void testNormalEB() {
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
	
	private class Handler1 {
		
		@EventSubscriber
		public void setNumber(Integer i) {
			testNumber = i;
		}
	}

}
