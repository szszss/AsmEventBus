package net.hakugyokurou.aeb.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ArrayCOWArrayListTest.class,
	EventBusTest.class,
	PriorEventBusTest.class,
	AsyncEventBusTest.class
})
public class TestCaseMain {

}
