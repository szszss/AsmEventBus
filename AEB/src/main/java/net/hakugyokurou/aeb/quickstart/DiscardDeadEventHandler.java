package net.hakugyokurou.aeb.quickstart;

import java.lang.reflect.Method;

import net.hakugyokurou.aeb.EventBus;
import net.hakugyokurou.aeb.auxiliary.IDeadEventHandler;

/**
 * DiscardDeadEventHandler is a quick-starting implement of IDeadEventHandler.
 * It will silently discard the dead events when it receives them.
 * 
 * @author szszss
 */
public class DiscardDeadEventHandler implements IDeadEventHandler{

	/**
	 * The singleton of {@link DiscardDeadEventHandler}.
	 */
	public static final DiscardDeadEventHandler SINGLETON = new DiscardDeadEventHandler();
	
	public void handleDeadEvent(EventBus eventBus, Object event) {
		//Do nothing, silence is gold.
	}

}
