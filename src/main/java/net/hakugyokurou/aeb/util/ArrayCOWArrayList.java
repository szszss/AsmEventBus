package net.hakugyokurou.aeb.util;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArrayCOWArrayList<E> {

	private final CopyOnWriteArrayList<E>[] arrays;
	private final int sections;
	private int size = 0;
	
	public ArrayCOWArrayList(int sections) {
		this.arrays = new CopyOnWriteArrayList[sections];
		this.sections = sections;
	}
	
	public void add(E value, int section) {
		if(section<0 || section>=this.sections)
			throw new RuntimeException(); //TODO:An exception.
		arrays[section].add(value);
		size++;
	}
	
	public boolean remove(E value) {
		for(CopyOnWriteArrayList<E> cowal : arrays)
		{
			if(cowal.remove(value))
			{
				size--;
				return true;
			}
		}
		return false;
	}
	
	public Iterator<E> getIterator(int section) {
		return arrays[section].iterator();
	}
	
	public int getSections() {
		return sections;
	}
	
	public boolean isEmpty() {
		return size==0;
	}
	
	public int getSize() {
		return size;
	}
	
	public boolean isEmpty(int section) {
		return arrays[section].isEmpty();
	}
	
	public int getSize(int section) {
		return arrays[section].size();
	}
}
