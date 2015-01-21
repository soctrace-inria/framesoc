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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.IModelElement;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.query.distribution.DistributionFactory;
import fr.inria.soctrace.lib.query.distribution.HEvent;
import fr.inria.soctrace.lib.query.distribution.HEventIterator;
import fr.inria.soctrace.lib.query.distribution.Histogram;
import fr.inria.soctrace.lib.query.distribution.HistogramLoader;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.test.junit.utils.BaseTraceDBTest;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * Test for Histogram Loader.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class HistogramLoaderTest extends BaseTraceDBTest {

	@Test
	public void testHistogramLoaderImpl() {
		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB);
		assertNotNull(loader);
	}

	@Test
	public void testLoadHistogramTypes() throws SoCTraceException {
		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB);
		List<EventType> types = new LinkedList<EventType>();
		types.add((EventType) traceDB.getEventTypeCache().getElementMap(EventType.class).get(1));
		types.add((EventType) traceDB.getEventTypeCache().getElementMap(EventType.class).get(2));
		types.add((EventType) traceDB.getEventTypeCache().getElementMap(EventType.class).get(3));
		types.add((EventType) traceDB.getEventTypeCache().getElementMap(EventType.class).get(4));
		Histogram hist = loader.loadHistogram(HistogramLoader.MIN_TIMESTAMP,
				HistogramLoader.MAX_TIMESTAMP, types, 2000);
		assertEquals(hist.getCount(), getCount(types));
	}

	@Test
	public void testLoadHistogramTypesAll() throws SoCTraceException {
		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB);
		List<EventType> types = new LinkedList<EventType>();
		Collection<IModelElement> etc = traceDB.getEventTypeCache().getElementMap(EventType.class)
				.values();
		for (IModelElement et : etc) {
			types.add((EventType) et);
		}
		Histogram hist = loader.loadHistogram(HistogramLoader.MIN_TIMESTAMP,
				HistogramLoader.MAX_TIMESTAMP, types, 100);
		assertEquals(hist.getCount(), getCount(types));
	}

	@Test
	public void testLoadHistogramType() throws SoCTraceException {
		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB);
		EventType type = (EventType) traceDB.getEventTypeCache().getElementMap(EventType.class)
				.get(4);
		Histogram hist = loader.loadHistogram(HistogramLoader.MIN_TIMESTAMP,
				HistogramLoader.MAX_TIMESTAMP, type, 100);
		List<EventType> tl = new LinkedList<EventType>();
		tl.add(type);
		assertEquals(hist.getCount(), getCount(tl));
	}

	@Test
	public void testLoadHistogramTypeTimestamps() throws SoCTraceException {
		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB);
		EventType type = (EventType) traceDB.getEventTypeCache().getElementMap(EventType.class)
				.get(0);

		// Considering the whole trace
		Histogram hist = loader.loadHistogram(HistogramLoader.MIN_TIMESTAMP,
				HistogramLoader.MAX_TIMESTAMP, type, 100);
		assertTrue(hist.getMin() <= hist.getMax());

		// Imposing timestamps
		long duration = VirtualImporter.getMaxTimestamp() - VirtualImporter.MIN_TIMESTAMP;
		long t1 = VirtualImporter.MIN_TIMESTAMP + duration / 8;
		long t2 = VirtualImporter.MIN_TIMESTAMP + duration / 4; 
		hist = loader.loadHistogram(t1, t2, type, 100);
		assertTrue(hist.getMin() >= t1);
		assertTrue(hist.getMax() <= t2);
	}

	@Test
	public void testGetMinTimestamp() throws SoCTraceException {
		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB);
		assertEquals(Math.max(traceDB.getMinTimestamp(), 0), loader.getMinTimestamp());
	}

	@Test
	public void testGetMaxTimestamp() throws SoCTraceException {
		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB);
		assertEquals(Math.max(traceDB.getMaxTimestamp(), 0), loader.getMaxTimestamp());
	}

	@Test
	public void testLoadHistogramFakeOne() throws SoCTraceException {
		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB,
				new TestIteratorStatic());
		List<EventType> types = new LinkedList<EventType>();
		types.add(new EventType(0, EventCategory.PUNCTUAL_EVENT));
		Histogram hist = loader.loadHistogram(0, 50, types, 5);
		assertEquals(0, hist.getMin());
		assertEquals(35, hist.getMax());
		assertEquals(12.2, hist.getMean().doubleValue(), 0.0000001);
	}

	@Test
	public void testLoadHistogramFakeTwo() throws SoCTraceException {
		long min = 1;
		long max = 100;
		double mean = 50;
		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB,
				new TestIteratorUniform(min, max));
		List<EventType> types = new LinkedList<EventType>();
		types.add(new EventType(0, EventCategory.PUNCTUAL_EVENT));
		Histogram hist = loader.loadHistogram(min, max, types, 10000);
		assertEquals(mean, hist.getMean().doubleValue(), 0.2);
	}

	@Test
	public void testLoadHistogramFakeThree() throws SoCTraceException {
		long mean = 1000;
		long dev = 10;
		long min = mean - 10 * dev;
		long max = mean + 10 * dev;
		int buckets = 10000;

		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB,
				new TestIteratorGauss(mean, dev));
		List<EventType> types = new LinkedList<EventType>();
		types.add(new EventType(0, EventCategory.PUNCTUAL_EVENT));

		Histogram hist = loader.loadHistogram(min, max, types, buckets);
		assertEquals(mean, hist.getMean().doubleValue(), 1.0);

		// Gaussian: 68.3% of values is between m-s and m+s
		double SIXTYEIGHT = 0.683;
		double HALFDIFF1 = (1.0 - SIXTYEIGHT) / 2;
		long b1 = hist.getUpperBoundForFactor(HALFDIFF1);
		assertEquals(b1, mean - dev, 1.0);

		// Gaussian: 95.5% of values is between m-2s and m+2s
		double NINETIFIVE = 0.955;
		double HALFDIFF2 = (1.0 - NINETIFIVE) / 2;
		long b2 = hist.getUpperBoundForFactor(HALFDIFF2);
		assertEquals(b2, mean - 2 * dev, 1.0);
	}

	/*
	 * Test iterators
	 */

	// Base test iterator
	abstract class TestIterator implements HEventIterator {
		@Override
		public void setTypes(List<EventType> types) throws SoCTraceException {
		}

		@Override
		public void setTimestamps(long startTimestamp, long endTimestamp) {
		}

		@Override
		public void setTraceDB(TraceDBObject traceDB) {
		}

		@Override
		public void clear() {
		}

		@Override
		public boolean hasNext() {
			return index < size;
		}

		@Override
		public HEvent getNext() {
			HEvent he = new HEvent();
			he.timestamp = ts[index++];
			return he;
		}

		private int index = 0;
		protected long ts[];
		protected int size;
	}

	// Random values uniformly distributed
	class TestIteratorUniform extends TestIterator {
		public TestIteratorUniform(long min, long max) {
			size = 1000000;
			ts = new long[size];
			Random r = new Random();
			for (int i = 0; i < size; i++) {
				ts[i] = (long) (min + (max - min) * r.nextDouble());
			}
		}
	}

	// Some predefined values
	class TestIteratorStatic extends TestIterator {
		public TestIteratorStatic() {
			size = 15;
			ts = new long[size];
			ts[0] = 0;
			ts[1] = 10;
			ts[2] = 11;
			ts[3] = 12;
			ts[4] = 13;
			ts[5] = 14;
			ts[6] = 15;
			ts[7] = 16;
			ts[8] = 21;
			ts[9] = 35;
			ts[10] = 1;
			ts[11] = 2;
			ts[12] = 3;
			ts[13] = 4;
			ts[14] = 5;
		}
	}

	// Values from a gaussian distribution
	class TestIteratorGauss extends TestIterator {
		public TestIteratorGauss(long mean, long dev) {
			size = 1000000;
			ts = new long[size];
			Random r = new Random();
			for (int i = 0; i < size; i++) {
				ts[i] = (long) (r.nextGaussian() * dev + mean); // 99.7% between
																// (m-3s) and
																// (m+3s)
			}
		}
	}

	// Utility

	private int getCount(List<EventType> types) throws SoCTraceException {
		EventQuery query = new EventQuery(traceDB);
		ValueListString vls = new ValueListString();
		for (EventType et : types) {
			vls.addValue(String.valueOf(et.getId()));
		}
		query.setElementWhere(new SimpleCondition("EVENT_TYPE_ID", ComparisonOperation.IN, vls
				.getValueString()));
		return query.getList().size();
	}

}
