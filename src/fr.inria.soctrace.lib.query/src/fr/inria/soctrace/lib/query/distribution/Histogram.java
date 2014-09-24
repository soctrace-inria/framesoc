/*******************************************************************************
 * Copyright (c) 2012-2014 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.lib.query.distribution;

import java.math.BigDecimal;

/**
 * <p>Histogram for tracking the frequency of observations of values below interval upper bounds.</p>
 *
 * <p>This class is useful for recording timings across a large number of observations
 * when high performance is required.<p>
 *
 * <p>The interval bounds are used to define the ranges of the histogram buckets. If provided bounds
 * are [10,20,30,40,50] then there will be five buckets, accessible by index 0-4. Any value
 * 0-10 will fall into the first interval bar, values 11-20 will fall into the
 * second bar, and so on.</p>
 */
public interface Histogram {

    /**
     * Add an observation to the histogram and increment the counter for the interval it matches.
     *
     * @param value for the observation to be added.
     * @return return true if in the range of intervals and successfully added observation; otherwise false.
     */
    boolean addObservation(final long value);
    
    /**
     * <p>Add observations from another Histogram into this one.</p>
     *
     * <p>Histograms must have the same intervals.</p>
     *
     * @param histogram from which to add the observation counts.
     * @throws IllegalArgumentException if interval count or values do not match exactly
     */
    public void addObservations(final Histogram histogram);
    
    /**
     * Size of the list of interval bars (i.e.: number of buckets).
     *
     * @return number of buckets
     */
    int getSize();
    
    /**
     * Count total number of recorded observations.
     *
     * @return the total number of recorded observations.
     */
    long getCount();

    /**
     * Get the index of the bucket that contains the value.
     * If the value is outside the bounds, -1 is returned.
     * 
     * @param value a value between 0 and the max
     * @return the index of the bucket containing the value.
     */
    int getIndexForValue(final long value);
    
    /**
     * Get the upper bound of an interval for an index.
     *
     * @param index of the upper bound.
     * @return the interval upper bound for the index.
     */
    long getUpperBoundAt(final int index);

    /**
     * Get the count of observations at a given index.
     *
     * @param index of the observations counter.
     * @return the count of observations at a given index.
     */
    long getCountAt(final int index);
    
    /**
     * Get the probability that an observation is in the
     * bucket at the given index.
     * 
     * @param index of the observations counter.
     * @return the probability that an observation is in the bucket at the given index.
     */
    BigDecimal getProbabilityAt(final int index);
    
    /**
     * Get the minimum observed value.
     *
     * @return the minimum value observed.
     */
    long getMin();

    /**
     * Get the maximum observed value.
     *
     * @return the maximum of the observed values;
     */
    long getMax();

    /**
     * <p>Calculate the mean of all recorded observations.</p>
     *
     * <p>The mean is calculated by summing the mid points of each interval multiplied by the count
     * for that interval, then dividing by the total count of observations.  The max and min are
     * considered for adjusting the top and bottom bin when calculating the mid point, this
     * minimizes skew if the observed values are very far away from the possible histogram values.</p>
     *
     * @return the mean of all recorded observations.
     */
    BigDecimal getMean();

    /**
     * Calculate the upper bound within which 99% of observations fall.
     *
     * @return the upper bound for 99% of observations.
     */
    long getTwoNinesUpperBound();

    /**
     * Calculate the upper bound within which 99.99% of observations fall.
     *
     * @return the upper bound for 99.99% of observations.
     */
    long getFourNinesUpperBound();

    /**
     * <p>Get the interval upper bound for a given factor of the observation population.</p>
     *
     * <p>Note this does not get the actual percentile measurement, it only gets the bucket</p>
     *
     * @param factor representing the size of the population.
     * @return the interval upper bound.
     * @throws IllegalArgumentException if factor &lt; 0.0 or factor &gt; 1.0
     */
    long getUpperBoundForFactor(final double factor);
    
    /**
     * Get a copy of the upper bounds vector
     * @return a copy of the upper bounds vector
     */
	long[] getUpperBounds();
    
    /**
     * Clear the list of interval counters
     */
    void clear();

}
