package net.hakugyokurou.aeb.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.hakugyokurou.aeb.util.ArrayCOWArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ArrayCOWArrayListTest {
	
	private ArrayCOWArrayList<Integer> subject;

	@Parameters
	public static Collection getParameters() {
		/*
		 * Format:
		 * Sections,{targetSection,targetValue},...
		 */
		Object[][] params = {
			{1,new int[]{0,3}},
			{3,new int[]{0,1 ,1,2 ,2,3}},
			{4,new int[]{0,1 ,1,2 ,2,3 ,0,4 ,0,0 ,0,6 ,2,4 ,1,5 ,1,-5}}
		};
		return Arrays.asList(params);
	}
	
	private final int sections;
	private final int[] targetSection;
	private final int[] targetValue;
	private final List<Integer>[] dataInSection;
	
	public ArrayCOWArrayListTest(int sections,int ... data) {
		this.sections = sections;
		if(sections<0 || sections>=ArrayCOWArrayList.MAX_SECTIONS)
			fail("Invaild sections.");
		if(data.length%2!=0)
			fail("Invaild parameters.");
		targetSection = new int[data.length/2];
		targetValue = new int[data.length/2];
		dataInSection = new List[sections];
		for(int i=0;i<sections;i++)
		{
			dataInSection[i] = new ArrayList<Integer>();
		}
		for(int i=0,j=0;i<data.length;i++,j++)
		{
			int section = data[i];
			int value = data[++i];
			if(section<0 || section>=sections)
				fail("Invaild parameter for OOB.");
			targetSection[j] = section;
			targetValue[j] = value;
			dataInSection[section].add(value);
		}
	}
	
	@Before
	public void setUp() throws Exception {
		subject = new ArrayCOWArrayList<Integer>(sections);
	}

	@Test
	public void testCommon() {
		int size = targetSection.length;
		int[] sectionSize = new int[sections];
		//Empty
		assertTrue(subject.isEmpty());
		for(int i=0;i<sections;i++)
		{
			assertTrue(subject.isEmpty(i));
		}
		//Add
		for(int i=0;i<size;i++)
		{
			subject.add(targetValue[i], targetSection[i]);
			sectionSize[targetSection[i]]++;
		}
		//GetSize
		assertEquals(size, subject.getSize());
		//GetSections & GetSize(some section)
		int[] realSectionSize = new int[subject.getSections()];
		for(int i=0;i<realSectionSize.length;i++)
		{
			realSectionSize[i] = subject.getSize(i);
		}
		assertArrayEquals(sectionSize, realSectionSize);
		//Iterator
		for(int i=0;i<sections;i++)
		{
			Iterator<Integer> iterator1 = subject.getIterator(i);
			Iterator<Integer> iterator2 = dataInSection[i].iterator();
			for(;iterator1.hasNext();)
			{
				assertEquals(iterator1.next(), iterator2.next());
			}
			assertFalse(iterator2.hasNext());
		}
		//Remove
		for(int i=0;i<size;i++)
		{
			subject.remove(targetValue[i]);
		}
		assertTrue(subject.isEmpty());
	}
}
