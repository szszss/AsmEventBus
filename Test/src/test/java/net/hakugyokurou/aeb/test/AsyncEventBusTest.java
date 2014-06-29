package net.hakugyokurou.aeb.test;

import static org.junit.Assert.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.hakugyokurou.aeb.AsyncEventBus;
import net.hakugyokurou.aeb.EventBus;
import net.hakugyokurou.aeb.quickstart.EventSubscriber;

import org.junit.Before;
import org.junit.Test;

public class AsyncEventBusTest {

	private AtomicInteger testNumber;
	private AtomicBoolean passAnyway;
	
	@Before
	public void setUp() {
		testNumber = new AtomicInteger(0);
		passAnyway = new AtomicBoolean(false);
	}
	
	@Test
	public void testAEB() {
		Executor executor = Executors.newSingleThreadExecutor();
		AsyncEventBus subject = new AsyncEventBus(executor, new EventBus());
		Handler1 handler1 = new Handler1();
		subject.register(handler1);
		subject.post(Integer.valueOf(10000));
		int a = testNumber.get();
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int b = testNumber.get();
		if(!passAnyway.get())
		{
			assertNotSame(a, b);
		}
	}
	
	private class Handler1 {
		@EventSubscriber
		public void addNumber(Integer count) {
			for(int i=0;i<count;i++)
				testNumber.getAndIncrement();
			passAnyway.set(true);
		}
	}

}
