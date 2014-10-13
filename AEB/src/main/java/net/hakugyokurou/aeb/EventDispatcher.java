package net.hakugyokurou.aeb;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.hakugyokurou.aeb.util.ArrayCOWArrayList;

abstract class EventDispatcher{

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
		return this.eventType.get().isAssignableFrom(o2.eventType.get());
	}
	
	public final EventDispatcher getParent() {
		return parentDispatcher.get();
	}
	
	public final void setParent(EventDispatcher dispatcher) {
		hasParent = true;
		parentDispatcher = new WeakReference<EventDispatcher>(dispatcher);
	}
	
	public abstract boolean post(Object event);
	
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
	
	abstract void addReceiver(Object receiver, EventInvoker invoker);
	
	abstract void addReceiver(Object receiver, EventInvoker invoker, int priority);
	
	abstract void removeReceiver(Object receiver, EventInvoker invoker);
	
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
	
	static abstract class SinglePriorityEventDispatcher extends EventDispatcher {

		protected List<Entry> entries = new CopyOnWriteArrayList<Entry>();
		
		public SinglePriorityEventDispatcher(EventBus bus, Class<?> eventType) {
			super(bus, eventType);
		}
		
		void addReceiver(Object receiver, EventInvoker invoker) {
			entries.add(new Entry(receiver,invoker));
		}
		
		void addReceiver(Object receiver, EventInvoker invoker, int priority) {
			throw new UnsupportedOperationException();
		}
		
		void removeReceiver(Object receiver, EventInvoker invoker) {
			Entry entry = new Entry(receiver,invoker);
			entries.remove(entry);
		}
		
		protected final void invokeEveryone(Object event) {
			Iterator<Entry> iterator = entries.iterator(); 
			for(;iterator.hasNext();)
			{
				Entry entry = iterator.next();
				try {
					entry.invoker.invoke(entry.receiver, event);
				} catch (Throwable e) {
					bus.getSubscriberExceptionHandler().handleSubscriberException(bus, entry.receiver, entry.invoker.getSubscriber(), event, e);
				}
			}
		}
	}
	
	static class SPEventDispatcherSF extends SinglePriorityEventDispatcher {
		public SPEventDispatcherSF(EventBus bus, Class<?> eventType) {
			super(bus, eventType);
		}
		@Override
		public boolean post(Object event) {
			boolean parentPosted = postParent(event);
			if(entries.isEmpty())
				return parentPosted||false;
			invokeEveryone(event);
			return true;
		}
	}
	
	static class SPEventDispatcherEF extends SinglePriorityEventDispatcher {
		public SPEventDispatcherEF(EventBus bus, Class<?> eventType) {
			super(bus, eventType);
		}
		@Override
		public boolean post(Object event) {
			if(entries.isEmpty())
				return postParent(event)||false;
			invokeEveryone(event);
			postParent(event);
			return true;
		}
	}

	static abstract class MultiPrioritiesEventDispatcher extends EventDispatcher {
		
		protected final ArrayCOWArrayList<Entry> cowals;
		
		public MultiPrioritiesEventDispatcher(EventBus bus, int priorities, Class<?> eventType) {
			super(bus, eventType);
			this.cowals = new ArrayCOWArrayList<Entry>(priorities);
		}
		
		void addReceiver(Object receiver, EventInvoker invoker) {
			throw new UnsupportedOperationException();
		}
		
		void addReceiver(Object receiver, EventInvoker invoker, int priority) {
			cowals.add(new Entry(receiver,invoker),priority);
		}
		
		void removeReceiver(Object receiver, EventInvoker invoker) {
			Entry entry = new Entry(receiver,invoker);
			cowals.remove(entry);
		}
		
		protected final void invokeSomeone(Entry entry, Object event) {
			try {
				entry.invoker.invoke(entry.receiver, event);
			} catch (Throwable e) {
				bus.getSubscriberExceptionHandler().handleSubscriberException(bus, entry.receiver, entry.invoker.getSubscriber(), event, e);
			}
		}
	}
	
	static class MPEventDispatcherPFSF extends MultiPrioritiesEventDispatcher {
		//Priority first, super first.
		public MPEventDispatcherPFSF(EventBus bus, int priorities, Class<?> eventType) {
			super(bus, priorities, eventType);
		}
		
		@Override
		public boolean post(Object event) {
			boolean parentPosted = postParent(event);
			if(cowals.isEmpty())
				return parentPosted||false;
			int i = cowals.getSections();
			Iterator<Entry> iterator;
			for(--i;i>=0;i--)
			{
				iterator = cowals.getIterator(i);
				for(;iterator.hasNext();)
				{
					Entry entry = iterator.next();
					invokeSomeone(entry,event);
				}
			}
			return true;
		}
	}
	
	static class MPEventDispatcherHFSF extends MultiPrioritiesEventDispatcher {
		//Hierarchy first, super first.
		public MPEventDispatcherHFSF(EventBus bus, int priorities, Class<?> eventType) {
			super(bus, priorities, eventType);
		}
		
		@Override
		public boolean post(Object event) {
			if(cowals.isEmpty())
				return postParent(event)||false;
			int i = cowals.getSections();
			Iterator<Entry> iterator;
			for(--i;i>=0;i--)
			{
				callParent(event, i);
				iterator = cowals.getIterator(i);
				for(;iterator.hasNext();)
				{
					Entry entry = iterator.next();
					invokeSomeone(entry,event);
				}
			}
			return true;
		}
		
		
		protected boolean callParent(Object event, int priority) {
			if(hasParent)
			{
				MPEventDispatcherHFSF parent = (MPEventDispatcherHFSF)parentDispatcher.get();
				if(parent!=null)
				{
					return parent.callParent(event, priority);
				}
				else
				{
					hasParent = false;
					bus.repairHierarchy(this);
					return callParent(event, priority);
				}
			}
			if(cowals.isEmpty(priority))
				return false;
			else
			{
				Iterator<Entry> iterator  = cowals.getIterator(priority);
				for(;iterator.hasNext();)
				{
					Entry entry = iterator.next();
					invokeSomeone(entry,event);
				}
				return true;
			}
		}
	}
	
	static class MPEventDispatcherPFEF extends MultiPrioritiesEventDispatcher {
		//Priority first, extended first.
		public MPEventDispatcherPFEF(EventBus bus, int priorities, Class<?> eventType) {
			super(bus, priorities, eventType);
		}
		
		@Override
		public boolean post(Object event) {
			if(cowals.isEmpty())
				return postParent(event)||false;
			int i = cowals.getSections();
			Iterator<Entry> iterator;
			for(--i;i>=0;i--)
			{
				iterator = cowals.getIterator(i);
				for(;iterator.hasNext();)
				{
					Entry entry = iterator.next();
					invokeSomeone(entry,event);
				}
			}
			postParent(event);
			return true;
		}
	}
	
	static class MPEventDispatcherHFEF extends MultiPrioritiesEventDispatcher {
		//Hierarchy first, extended first.
		public MPEventDispatcherHFEF(EventBus bus, int priorities, Class<?> eventType) {
			super(bus, priorities, eventType);
		}
		
		@Override
		public boolean post(Object event) {
			if(cowals.isEmpty())
				return postParent(event)||false;
			int i = cowals.getSections();
			Iterator<Entry> iterator;
			for(--i;i>=0;i--)
			{
				iterator = cowals.getIterator(i);
				for(;iterator.hasNext();)
				{
					Entry entry = iterator.next();
					invokeSomeone(entry, event);
				}
				callParent(event, i);
			}
			return true;
		}
		
		
		protected boolean callParent(Object event, int priority) {
			if(hasParent)
			{
				MPEventDispatcherHFEF parent = (MPEventDispatcherHFEF)parentDispatcher.get();
				if(parent!=null)
				{
					if(!cowals.isEmpty(priority))
					{
						Iterator<Entry> iterator  = cowals.getIterator(priority);
						for(;iterator.hasNext();)
						{
							Entry entry = iterator.next();
							invokeSomeone(entry, event);
						}
					}
					return (!cowals.isEmpty(priority))||parent.callParent(event, priority);
				}
				else
				{
					hasParent = false;
					bus.repairHierarchy(this);
					return callParent(event, priority);
				}
			}
			if(cowals.isEmpty(priority))
				return false;
			else
			{
				Iterator<Entry> iterator  = cowals.getIterator(priority);
				for(;iterator.hasNext();)
				{
					Entry entry = iterator.next();
					invokeSomeone(entry, event);
				}
				return true;
			}
		}
	}
}
