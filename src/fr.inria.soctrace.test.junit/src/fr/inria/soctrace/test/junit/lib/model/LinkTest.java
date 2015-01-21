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
package fr.inria.soctrace.test.junit.lib.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.test.junit.utils.IModelFactory;

public class LinkTest {
	
	@Test
	public void testLink() {
		Link link = new Link(0);
		assertEquals(EventCategory.LINK, link.getCategory());
	}

	@Test
	public void testGetEndTimestamp() {
		Link link = new Link(0);
		link.setEndTimestamp(Long.MAX_VALUE);
		assertEquals(Long.MAX_VALUE, link.getEndTimestamp());
	}

	@Test
	public void testGetEndProducer() {	
		Link link = new Link(0);
		EventProducer ep = IModelFactory.INSTANCE.createEvent().getEventProducer();
		link.setEndProducer(ep);
		assertEquals(ep, link.getEndProducer());
	}

}
