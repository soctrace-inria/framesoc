/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.test.junit.lib.query;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.soctrace.lib.query.distribution.Histogram;
import fr.inria.soctrace.lib.query.distribution.DistributionFactory;

public class HistogramTest {

	static final int NUM_VALUES = 1034400;
	static final long MIN = 1450;
	static final long MAX = 4345341;
	static final int BUCKETS = 155;
	
	static long MIN_VALUE = Long.MAX_VALUE;
	static long MAX_VALUE = Long.MIN_VALUE;
	static long countAtLast = 0;
	static long uppers[];
		
	static Histogram hist;
	
	static final boolean DEBUG = false;
			
	private static void debug(String s) {
		if (DEBUG)
			System.out.println(s);
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		uppers = new long[BUCKETS];
		long delta = Math.max((MAX-MIN+1)/(BUCKETS), 1);
		uppers[BUCKETS-1] = MAX;		
		for (int i=BUCKETS-2; i>=0; i--) {
			uppers[i] = uppers[i+1] - delta;	
		}
		hist = DistributionFactory.INSTANCE.createHistogram(uppers);
		Random randomGenerator = new Random();
		for (int i=0; i< NUM_VALUES; i++) {
			long v = MIN + Math.round(randomGenerator.nextDouble()*(MAX-MIN));
			if (v<MIN)v=MIN;
			if (v>MAX)v=MAX;
			
			hist.addObservation(v);
			
			if (v<MIN_VALUE) MIN_VALUE = v;
			if (v>MAX_VALUE) MAX_VALUE = v;
			if (v>uppers[uppers.length-2]) countAtLast++;
		}
		
		debug("min value: " + MIN_VALUE);
		debug("max value: " + MAX_VALUE);
		debug("min value: " + hist.getMin());
		debug("max value: " + hist.getMax());

		debug(hist.toString());
		debug("delta: " + delta);
		for (int i=0; i<BUCKETS; i++) {
			debug("bin: "+i+" ("+ ((i==0)?0:uppers[i-1])+", "+uppers[i]+")");
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		hist.clear();
	}

	@Test
	public void testGetSize() {
		assertEquals(BUCKETS, hist.getSize());
	}

	@Test
	public void testGetUpperBoundAt() {
		int index = 3;
		long bound = MAX - (BUCKETS-1-index)*(Math.max(MAX-MIN, 1)/BUCKETS);
		assertEquals(bound, hist.getUpperBoundAt(index));
	}

	@Test
	public void testGetCountAt() {
		assertEquals(countAtLast, hist.getCountAt(BUCKETS-1));
	}
	
	@Test
	public void testClear() {
		Histogram second = DistributionFactory.INSTANCE.createHistogram(uppers);
		second.addObservation(MIN+1);
		second.addObservation(MIN+2);
		assertEquals(2, second.getCount());
		second.clear();
		assertEquals(0, second.getCount());
	}

	@Test
	public void testGetCount() {
		assertEquals(NUM_VALUES, hist.getCount());
	}

	@Test
	public void testGetMin() {
		assertEquals(MIN_VALUE, hist.getMin());
	}

	@Test
	public void testGetMax() {
		assertEquals(MAX_VALUE, hist.getMax());
	}

	@Test
	public void testGetUpperBoundForFactor() {
		assertEquals(hist.getFourNinesUpperBound(), hist.getUpperBoundForFactor(0.9999d));
	}

	@Test
	public void testGetIndexForValue() {
		Histogram hist = DistributionFactory.INSTANCE.createHistogram(new long[]{10,20,30,40,50});
		assertEquals(-1, hist.getIndexForValue(-1));
		assertEquals(-1, hist.getIndexForValue(51));
		assertEquals(0, hist.getIndexForValue(2));
		assertEquals(4, hist.getIndexForValue(41));
		assertEquals(3, hist.getIndexForValue(40));
	}
	
	@Test
	public void testGetUpperBounds() {
		Histogram hist = DistributionFactory.INSTANCE.createHistogram(new long[]{10,20,30,40,50});
		Histogram hist2 = DistributionFactory.INSTANCE.createHistogram(hist.getUpperBounds());
		assertTrue(Arrays.equals(hist2.getUpperBounds(), hist.getUpperBounds()));
	}

	@Test
	public void testAddObservations() {
		Histogram copy = DistributionFactory.INSTANCE.createHistogram(hist.getUpperBounds());
		copy.addObservations(hist);
		for (int i=0; i<copy.getSize(); i++) {
			assertEquals(hist.getCountAt(i), copy.getCountAt(i));
			assertEquals(hist.getUpperBoundAt(i), copy.getUpperBoundAt(i));
		}
		assertEquals(hist.getMean(), copy.getMean());
		assertEquals(hist.getMax(), copy.getMax());
		assertEquals(hist.getMin(), copy.getMin());
	}

}
