package net.hakugyokurou.aeb.auxiliary;

import java.lang.reflect.Method;

import net.hakugyokurou.aeb.EventBus;

public interface IDeadEventHandler {

	public abstract void handleDeadEvent(EventBus eventBus, Object event);
}
