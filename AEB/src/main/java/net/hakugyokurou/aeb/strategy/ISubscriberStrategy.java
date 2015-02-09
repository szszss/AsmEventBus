package net.hakugyokurou.aeb.strategy;

import java.lang.reflect.Method;

/**
 * ISubscriberStrategy describes that how event bus finds subscribers from the instance or class.<br/>
 * It has a default implement: AnnotatedSubscriberFinder.
 * 
 * @author szszss
 * 
 * @see AnnotatedSubscriberFinder
 */
public interface ISubscriberStrategy {
	
	/**
	 * ATTENTION:
	 * Although this method is <b>unused</b> so far, you should serious deal with it, because it may be put into service in some time.<br><br>
	 * 
	 * <code>baseInInstance</code> describes that whether the subscribers depend on class or instance of class. In other words,
	 * May the subscribers be different when they are from different instance of same class?<br/><br/>
	 * 
	 * If the subscribers are same so long as they are from same class, this method should return <code>false</code>, 
	 * since they don't depend on instance. This is the behavior of most event bus system, such as Google Guava.<br/>
	 * However, if the subscribers may be different though they are from same class, this method should return <code>true</code>, 
	 * because they depend on the difference of class' instance. <br/><br/>
	 * 
	 * For example:<br/>
	 * <code>
	 * EventBus eventBus = ...<br/>
	 * Handler h1 = new Handler();<br/>
	 * Handler h2 = new Handler();<br/>
	 * eventBus.register(h1);<br/>
	 * eventBus.register(h2);<br/>
	 * </code>
	 * If event bus get (or may get) different subscribers in two registering, then the subscribers depend on instance.<br/><br/>
	 * 
	 * Depending on instance can be used to implement some special function.
	 * 
	 * @return <code>false</code> if every instance of handler have same subscribers so long as instances are from same class, 
	 * <code>true</code> if the subscribers are base on handler's instance.
	 */
	public boolean isDependOnInstance();
	
	public abstract Method[] findSubscribers(Object handler);

}
