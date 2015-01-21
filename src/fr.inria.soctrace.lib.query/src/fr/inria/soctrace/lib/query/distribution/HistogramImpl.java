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
/*
 * Copyright 2011 LMAX Ltd.
 * 
 * NOTICE: this file has been modified by Generoso Pagano (2013).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Small contributions by Generoso Pagano:
 * - class name changed from Histogram to HistogramImpl
 * - class scope is package-private
 * - methods:
 *   - getIndexForValue()
 *   - getProbabilityAt()
 *   - getUpperBounds()
 * - fields:
 *   - count
 */
package fr.inria.soctrace.lib.query.distribution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Implementation of the {@link Histogram} interface.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
final class HistogramImpl implements Histogram {

	// tracks the upper intervals of each of the buckets/bars
	private final long[] upperBounds;
	// tracks the count of the corresponding bucket
	private final long[] counts;
	// minimum value so far observed
	private long minValue = Long.MAX_VALUE;
	// maximum value so far observed
	private long maxValue = 0L;
	// tracks the total count (Generoso Pagano)
	private long count = 0;

	/**
	 * Create a new Histogram with a provided list of interval bounds.
	 * 
	 * @param upperBounds
	 *            of the intervals. Bounds must be provided in order least to
	 *            greatest, and lowest bound must be greater than or equal to 1.
	 * @throws IllegalArgumentException
	 *             if any of the upper bounds are less than or equal to zero
	 * @throws IllegalArgumentException
	 *             if the bounds are not in order, least to greatest
	 */
	public HistogramImpl(final long[] upperBounds) {
		validateBounds(upperBounds);

		this.upperBounds = Arrays.copyOf(upperBounds, upperBounds.length);
		this.counts = new long[upperBounds.length];
	}

	/**
	 * Validates the input bounds; used by constructor only.
	 */
	private void validateBounds(final long[] upperBounds) {
		long lastBound = -1L;
		if (upperBounds.length <= 0) {
			throw new IllegalArgumentException("Must provide at least one interval");
		}
		for (final long bound : upperBounds) {
			if (bound <= 0L) {
				throw new IllegalArgumentException("Bounds must be positive values");
			}

			if (bound <= lastBound) {
				throw new IllegalArgumentException("bound " + bound + " is not greater than " + lastBound);
			}

			lastBound = bound;
		}
	}

	@Override
	public int getSize() {
		return upperBounds.length;
	}

	@Override
	public long getUpperBoundAt(final int index) {
		return upperBounds[index];
	}

	@Override
	public long getCountAt(final int index) {
		return counts[index];
	}

	@Override
	public boolean addObservation(final long value) {
		int low = 0;
		int high = upperBounds.length - 1;

		// do a classic binary search to find the high value
		while (low < high) {
			int mid = low + ((high - low) >> 1);
			if (upperBounds[mid] < value) {
				low = mid + 1;
			} else {
				high = mid;
			}
		}

		// if the binary search found an eligible bucket, increment
		if (value <= upperBounds[high]) {
			counts[high]++;
			count++; // Generoso Pagano
			trackRange(value);

			return true;
		}

		// otherwise value was not found
		return false;
	}

	@Override
	public void addObservations(final Histogram histogram) {
		// validate the intervals
		if (upperBounds.length != histogram.getSize()) {
			throw new IllegalArgumentException("Histograms must have matching intervals");
		}

		for (int i = 0, size = upperBounds.length; i < size; i++) {
			if (upperBounds[i] != histogram.getUpperBoundAt(i)) {
				throw new IllegalArgumentException("Histograms must have matching intervals");
			}
		}

		// increment all of the internal counts
		for (int i = 0, size = counts.length; i < size; i++) {
			counts[i] += histogram.getCountAt(i);
		}
		count = histogram.getCount();

		// refresh the minimum and maximum observation ranges
		trackRange(histogram.getMin());
		trackRange(histogram.getMax());
	}

	/**
	 * Keep min and max values updated
	 */
	private void trackRange(final long value) {
		if (value < minValue) {
			minValue = value;
		}

		if (value > maxValue) {
			maxValue = value;
		}
	}

	@Override
	public void clear() {
		maxValue = 0L;
		minValue = Long.MAX_VALUE;

		for (int i = 0, size = counts.length; i < size; i++) {
			counts[i] = 0L;
		}

		count = 0; // Generoso Pagano
	}

	@Override
	public long getCount() {
		return count; // Generoso Pagano
	}

	@Override
	public long getMin() {
		return minValue;
	}

	@Override
	public long getMax() {
		return maxValue;
	}

	@Override
	public BigDecimal getMean() {
		// early exit to avoid divide by zero later
		if (0L == getCount()) {
			return BigDecimal.ZERO;
		}

		// precalculate the initial lower bound; needed in the loop
		long lowerBound = counts[0] > 0L ? minValue : 0L;
		// use BigDecimal to avoid precision errors
		BigDecimal total = BigDecimal.ZERO;

		// midpoint is calculated as the average between the lower and upper
		// bound
		// (after taking into account the min & max values seen)
		// then, simply multiply midpoint by the count of values at the interval
		// (intervalTotal)
		// and add to running total (total)
		for (int i = 0, size = upperBounds.length; i < size; i++) {
			if (0L != counts[i]) {
				long upperBound = Math.min(upperBounds[i], maxValue);
				long midPoint = lowerBound + ((upperBound - lowerBound) / 2L);

				BigDecimal intervalTotal = new BigDecimal(midPoint).multiply(new BigDecimal(counts[i]));
				total = total.add(intervalTotal);
			}

			// and recalculate the lower bound for the next time around the loop
			lowerBound = Math.max(upperBounds[i] + 1L, minValue);
		}

		return total.divide(new BigDecimal(getCount()), 2, RoundingMode.HALF_UP);
	}

	@Override
	public long getTwoNinesUpperBound() {
		return getUpperBoundForFactor(0.99d);
	}

	@Override
	public long getFourNinesUpperBound() {
		return getUpperBoundForFactor(0.9999d);
	}

	@Override
	public long getUpperBoundForFactor(final double factor) {
		if (0.0d >= factor || factor >= 1.0d) {
			throw new IllegalArgumentException("factor must be >= 0.0 and <= 1.0");
		}

		final long totalCount = getCount();
		final long tailTotal = totalCount - Math.round(totalCount * factor);
		long tailCount = 0L;

		// reverse search the intervals ('tailCount' from end)
		for (int i = counts.length - 1; i >= 0; i--) {
			if (0L != counts[i]) {
				tailCount += counts[i];
				if (tailCount >= tailTotal) {
					return upperBounds[i];
				}
			}
		}

		return 0L;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Histogram{");

		sb.append("min=").append(getMin()).append(", ");
		sb.append("max=").append(getMax()).append(", ");
		sb.append("mean=").append(getMean()).append(", ");
		sb.append("99%=").append(getTwoNinesUpperBound()).append(", ");
		sb.append("99.99%=").append(getFourNinesUpperBound()).append(", ");

		sb.append('[');
		for (int i = 0, size = counts.length; i < size; i++) {
			sb.append(upperBounds[i]).append('=').append(counts[i]).append(", ");
		}

		if (counts.length > 0) {
			sb.setLength(sb.length() - 2);
		}
		sb.append(']');

		sb.append('}');

		return sb.toString();
	}

	/*
	 * Added by Generoso Pagano
	 */

	@Override
	public int getIndexForValue(final long value) {
		if (value < 0 || value > upperBounds[upperBounds.length - 1])
			return -1;
		int low = 0;
		int high = upperBounds.length - 1;
		while (low < high) {
			int mid = low + ((high - low) >> 1);
			if (upperBounds[mid] < value)
				low = mid + 1;
			else
				high = mid;
		}
		return high;
	}

	@Override
	public BigDecimal getProbabilityAt(final int index) {
		if (count == 0)
			return BigDecimal.ZERO;
		return new BigDecimal(counts[index] / (double) count);
	}
	
	@Override
	public long[] getUpperBounds() {
		return Arrays.copyOf(upperBounds, upperBounds.length);
	}
}
