package net.hakugyokurou.aeb.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @Deprecated Nothing but a fucking bug fag. There's no evidence that SectionalLinkedList is faster than ArrayCOWArrayList, 
 * and I'm sure that this is a shit with tons of defect.
 */
@Deprecated
class SectionalLinkedList<E> implements Iterable<E>{
	
	private final Map<E, Entry<E>> nodes = new HashMap<E, SectionalLinkedList.Entry<E>>();
	private final Entry<E> rootNodes[];
	private final int sections;
	
	private Entry<E> headNode; 

	public SectionalLinkedList(int sections) {
		if(sections<=0)
			throw new RuntimeException(); //TODO:An exception.
		this.rootNodes = new Entry[sections];
		this.sections = sections;
	}
	
	public Iterator<E> iterator() {
		return iterator(sections-1);
	}
	
	public Iterator<E> iterator(int start) {
		synchronized (rootNodes) {
			return new SLLIterator<E>(sections, rootNodes, sections-1);
		}
	}
	
	public void add(E value, int section) {
		if(section<0 || section>=this.sections)
			throw new RuntimeException(); //TODO:An exception.
		synchronized (rootNodes) {
			Entry<E> entry = new Entry<E>(value, section);
			nodes.put(value, entry);
			Entry<E> ingress = rootNodes[section];
			if(ingress!=null)
			{
				entry.prev = ingress;
				entry.next = ingress.next;
				if(ingress.next!=null)
					ingress.next.prev = entry;
				ingress.next = entry;
			}
			else
			{
				//There are two situations, No any node, or this (when sections is 5):
				//     I
				//H I  I
				//222333
				//As you see, 0 and 1 haven't ingress.
				//Sorry for my poor English... I don't know how to express...
				if(headNode==null) //If no nodes.
				{
					headNode = entry;
					for(int i=section+1;i<sections;i++)
					{
						rootNodes[i] = entry;
					}
				}
				else
				{
					entry.next = headNode;
					headNode.prev = entry;
					headNode = entry;
					for(int i=section+1;rootNodes[i]==null;i++)
					{
						rootNodes[i] = entry;
					}
				}
			}
			rootNodes[section] = entry;
		}
	}
	
	public boolean remove(E value) {
		synchronized (rootNodes) {
			Entry<E> entry = nodes.remove(value);
			if(entry==null)
				return false;
			for(int i=0;i<sections;i++)
			{
				if(entry == rootNodes[i])
				{
					if(i==0)
					{
						if(entry.prev!=null) //0[0]12
						{
							entry.prev.next=entry.next;
							if(entry.next!=null)
							{
								entry.next.prev=entry.prev;
							}
							rootNodes[i] = entry.prev;
							//became 012
						}
						else  //[0]12
						{
							if(entry.next!=null)
							{
								entry.next.prev=null;
							}
							headNode = entry.next;
							//became 12
						}
					}
					else
					{
						for(int j=0;j<sections;j++)
						{
							if(rootNodes[j]==entry)
							{
								rootNodes[j]=entry.prev;
							}
						}
						if(entry.next!=null)
						{
							entry.next.prev=entry.prev;
						}
						if(entry.prev!=null)
						{
							entry.prev.next=entry.next;
						}
						else
						{
							headNode = entry.next;
						}
					}
				}
				return true;
			}
			entry.next.prev=entry.prev;
			if(entry.prev!=null)
			{
				entry.prev.next=entry.next;
			}
			else
			{
				headNode = entry.next;
			}
			return true;
		}
	}
	
	public boolean isEmpty() {
		return nodes.size()==0;
	}
	
	public int getSize() {
		return nodes.size();
	}
	
	public static class SLLIterator<E> implements Iterator<E> {

		private int sectionBreakpoint;
		private Entry<E> current;
		private Entry<E> next;
		
		public SLLIterator(int sections, Entry<E>[] rootNodes, int start) {
			next = (Entry<E>)rootNodes[start];
			sectionBreakpoint = sections;
		}
		
		public boolean hasNext() {
			return next!=null;
		}

		public E next() {
			current = next;
			next = next.prev;
			return current.value;
		}

		public void remove() { //Why fucking remove?
			throw new UnsupportedOperationException();
		}
		
		public boolean isBreakpoint() { //Shit, I'm sure that there is a bug. 如果多个rootNode指向一个节点会怎么样?
			if(current.section<sectionBreakpoint)
			{
				sectionBreakpoint = current.section;
				return true;
			}
			return false;
		}
	}
	
	private static final class Entry<E> {
		public Entry<E> next;
		public Entry<E> prev;
		public final E value;
		public final int section;
		
		public Entry(E value, int section) {
			this.value = value;
			this.section = section;
		}
		
		@Override
		public boolean equals(Object object) {
			if(!(object instanceof Entry))
				return false;
			Entry entry = (Entry)object;
			return entry.value==this.value&&entry.section==this.section;
		}
		
		@Override
		public int hashCode() {
			return value.hashCode()*(section+1);
		}
	}

	
}
