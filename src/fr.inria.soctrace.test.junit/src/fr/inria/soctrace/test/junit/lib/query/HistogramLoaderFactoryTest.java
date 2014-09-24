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
package fr.inria.soctrace.test.junit.lib.query;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import fr.inria.soctrace.lib.query.distribution.HistogramLoader;
import fr.inria.soctrace.lib.query.distribution.DistributionFactory;
import fr.inria.soctrace.test.junit.utils.BaseTraceDBTest;

public class HistogramLoaderFactoryTest extends BaseTraceDBTest {

	@Test
	public void testCreateHistogramLoader() {
		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB);
		assertNotNull(loader);
	}

}
