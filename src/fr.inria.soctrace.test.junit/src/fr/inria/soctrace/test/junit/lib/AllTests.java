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
package fr.inria.soctrace.test.junit.lib;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses 
({
	fr.inria.soctrace.test.junit.lib.storage.AllTests.class, 
	fr.inria.soctrace.test.junit.lib.query.AllTests.class,
	fr.inria.soctrace.test.junit.lib.search.AllTests.class,
	fr.inria.soctrace.test.junit.lib.utils.AllTests.class,
	fr.inria.soctrace.test.junit.lib.model.AllTests.class
})
public class AllTests {

}
