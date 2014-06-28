package net.hakugyokurou.aeb.util;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArrayCOWArrayList<E> {

	private final CopyOnWriteArrayList<E>[] arrays;
	private final int sections;
	private int size = 0;
	
	/**
	 * Max section count. Now is 65535.
	 */
	public static final int MAX_SECTIONS;
	
	static {
		MAX_SECTIONS = 65535; //Prevent compile-time inline.
	}
	
	@SuppressWarnings("unchecked")
	public ArrayCOWArrayList(int sections) {
		if(sections<0 || sections>MAX_SECTIONS)
			throw new IllegalArgumentException("Invaild sections:"+sections);
		this.arrays = new CopyOnWriteArrayList[sections];
		for(int i=0;i<sections;i++)
		{
			arrays[i] = new CopyOnWriteArrayList<E>();
		}
		this.sections = sections;
	}
	
	public void add(E value, int section) {
		if(section<0 || section>=this.sections)
			throw new IndexOutOfBoundsException("Section:"+section+" is out of <0-"+(sections-1)+">.");
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
