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
package fr.inria.soctrace.lib.query.iterators;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Iterator for trace events using pages.
 * 
 * <p>
 * The iterator reads events page by page, from the
 * smallest page to the biggest one.
 * For each page events are ordered by timestamp.
 * 
 * <p>
 * The user does not have page visibility, but simply
 * calls getNext() to get the next event of the current
 * page or the first event of the next page.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PageEventIterator extends AbstractEventIterator {
	
	protected final long MIN_PAGE;
	protected final long MAX_PAGE;
	
	/**
	 * Next page we will read
	 */
	protected long nextPage;
	
	/**
	 * Iterator to iterate over trace events, loaded page
	 * by page. The user does not need to care about pages
	 * but he simply uses the hasNext() / getNext() methods.
	 * 
	 * @param traceDB database object
	 * @throws SoCTraceException
	 */
	public PageEventIterator(TraceDBObject traceDB) throws SoCTraceException {
		super(traceDB);
		this.MIN_PAGE = this.traceDB.getMinPage();
		this.MAX_PAGE = this.traceDB.getMaxPage();
		this.nextPage = MIN_PAGE;
	}
	
	@Override
	public Event getNext() throws SoCTraceException {
		checkValid();
		if (eIterator==null || !eIterator.hasNext()) {
			debug("load page " + nextPage);
			
			if (nextPage>MAX_PAGE) {
				clear();
				return null;
			}
			
			eIterator = null;
			if (eList!=null) {
				eList.clear();
				eList = null;
			}
			query.clear();
			
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.EQ, String.valueOf(nextPage)));
			query.setOrderBy("TIMESTAMP", OrderBy.ASC);
			eList = query.getList();
			debug("loaded events: " + eList.size());
			eIterator = eList.iterator();
			nextPage++;
		}
		if (eIterator.hasNext()) // in the case an empty list has been loaded
			return eIterator.next();
		return eIterator.next();
	}
	
	@Override
	public boolean hasNext() throws SoCTraceException {
		checkValid();
		if (eIterator==null || !eIterator.hasNext())
			if (nextPage>MAX_PAGE) 
				return false;
		return true;
	}

}
