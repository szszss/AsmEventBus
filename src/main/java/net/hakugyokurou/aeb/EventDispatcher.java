package net.hakugyokurou.aeb;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class EventDispatcher{

	protected List<Entry> entries = new CopyOnWriteArrayList<Entry>();
	
	public boolean post(Object event) {
		if(entries.isEmpty())
			return false;
		Iterator<Entry> iterator = entries.iterator(); 
		for(;iterator.hasNext();)
		{
			Entry entry = iterator.next();
			entry.invoker.invoke(entry.receiver, event);
		}
		return true;
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
		
	}
}
