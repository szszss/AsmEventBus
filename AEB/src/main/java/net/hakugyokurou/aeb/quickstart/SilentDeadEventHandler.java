package net.hakugyokurou.aeb.quickstart;

import java.lang.reflect.Method;

import net.hakugyokurou.aeb.EventBus;
import net.hakugyokurou.aeb.auxiliary.IDeadEventHandler;

public class SilentDeadEventHandler implements IDeadEventHandler{

	public static final SilentDeadEventHandler QUICKSTART_SINGLETON = new SilentDeadEventHandler();
	
	public void handleDeadEvent(EventBus eventBus, Object event) {
		//Do nothing, silence is gold.
	}

}
