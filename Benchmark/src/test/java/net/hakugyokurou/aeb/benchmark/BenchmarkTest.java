package net.hakugyokurou.aeb.benchmark;

import static org.junit.Assert.*;
import net.hakugyokurou.aeb.quickstart.AnnotatedSubscriberFinder;
import net.hakugyokurou.aeb.strategy.EnumHierarchyStrategy;
import net.hakugyokurou.aeb.strategy.EnumInvokerGenerator;

import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.PUBLIC_MEMBER;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class BenchmarkTest {

	private int testNumber;
	private final static int count = 100000;
	
	@Before
	public void setUp() {
		testNumber = 0;
	}
	
	@Test
	public void testAEB() {
		net.hakugyokurou.aeb.EventBus aeb = new net.hakugyokurou.aeb.EventBus("",new AnnotatedSubscriberFinder(Subscribe.class));
		//net.hakugyokurou.aeb.EventBus aeb = new net.hakugyokurou.aeb.EventBus("",EnumHierarchyStrategy.EXTENDED_FIRST, new AnnotatedSubscriberFinder(Subscribe.class), EnumInvokerGenerator.REFLECT);
		aeb.register(new AEBHandle());
		long t0 = System.nanoTime();
		for(int i=0;i<count;i++){
			aeb.post(new Event(1));
		}
		//System.out.println("AEB:  "+(System.nanoTime()-t0));
		assertEquals(count, testNumber);
	}
	
	@Test
	public void testGuava() {
		com.google.common.eventbus.EventBus guava = new com.google.common.eventbus.EventBus();
		guava.register(new AEBHandle());
		long t0 = System.nanoTime();
		for(int i=0;i<count;i++){
			guava.post(new Event(1));
		}
		//System.out.println("Guava:"+(System.nanoTime()-t0));
		assertEquals(count, testNumber);
	}
	
	
	private static class Event {
		public final int number;
		public Event(int i) {
			number = i;
		}
	}
	
	private class AEBHandle {
		@Subscribe
		public void addNumber(Event e) {
			testNumber += e.number;
		}
	}
}
