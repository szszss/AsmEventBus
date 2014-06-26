package net.hakugyokurou.aeb;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class EventDispatcher{

	protected List<Entry> entries = new CopyOnWriteArrayList<Entry>();
	protected WeakReference<EventDispatcher> parentDispatcher = new WeakReference<EventDispatcher>(null);
	protected boolean hasParent = false;
	protected final WeakReference<Class<?>> eventType;
	protected final EventBus bus;
	
	public EventDispatcher(EventBus bus, Class<?> eventType) {
		this.bus = bus;
		this.eventType = new WeakReference<Class<?>>(eventType);
	}
	
	/**
	 * Return that if left-value's event is the super class of right-value's event.<br/>
	 * @param o2 the right-value
	 * @return
	 */
	public final boolean isSuper(EventDispatcher o2) {
		return o2.eventType.get().isAssignableFrom(this.eventType.get());
	}
	
	public final EventDispatcher getParent() {
		return parentDispatcher.get();
	}
	
	public final void setParent(EventDispatcher dispatcher) {
		hasParent = true;
		parentDispatcher = new WeakReference<EventDispatcher>(dispatcher);
	}
	
	public boolean post(Object event) {
		boolean parentPosted = postParent(event);
		if(entries.isEmpty())
			return parentPosted||false;
		Iterator<Entry> iterator = entries.iterator(); 
		for(;iterator.hasNext();)
		{
			Entry entry = iterator.next();
			entry.invoker.invoke(entry.receiver, event);
		}
		return true;
	}
	
	protected final boolean postParent(Object event) {
		if(hasParent)
		{
			EventDispatcher parent = parentDispatcher.get();
			if(parent!=null)
			{
				return parent.post(event);
			}
			else
			{
				hasParent = false;
				bus.repairHierarchy(this);
				return postParent(event);
			}
		}
		return false;
	}
	
	void addReceiver(Object receiver, EventInvoker invoker) {
		entries.add(new Entry(receiver,invoker));
	}
	
	void removeReceiver(Object receiver, EventInvoker invoker) {
		Entry entry = new Entry(receiver,invoker);
		entries.remove(entry);
	}
	
	protected static class Entry {
		
		public final Object receiver;
		public final EventInvoker invoker;
		
		public Entry(Object receiver, EventInvoker invoker) {
			this.receiver = receiver;
			this.invoker = invoker;
		}
		
		@Override
		public boolean equals(Object object) {
			if(!(object instanceof Entry))
				return false;
			Entry entry = (Entry)object;
			return entry.receiver==this.receiver && entry.invoker==this.invoker;
		}
		
		@Override
		public int hashCode() {
			return receiver.hashCode() + invoker.hashCode();
		}
	}

	static class PriorEventDispatcher extends EventDispatcher {
		
		public PriorEventDispatcher(EventBus bus, Class<?> eventType) {
			super(bus, eventType);
		}
	}
}
