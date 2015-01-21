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
package fr.inria.soctrace.lib.utils;

/**
 * Utility class to manage sequential IDs.
 * 
 * By default the first ID is 0 and the direction
 * is ascending.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class IdManager {
	
	/**
	 * Id counter growing direction.
	 */
	public enum Direction {
		ASCENDING,
		DESCENDING
	}
	
	private Direction dir = Direction.ASCENDING;
	
	/**
	 * Reserved id values ( < 0)
	 */
	public final static int RESERVED_NO_ID = Integer.MIN_VALUE;
	
	/**
	 * Next id to be used
	 */
	private int nextId = 0;
	
	public synchronized int getNextId() {
		int id = nextId;
		if (dir.equals(Direction.ASCENDING))
			nextId++;
		else 
			nextId--;
		return id;
	}
	
	public synchronized void resetNextId() {
		nextId = 0;
	}
	
	public synchronized void setNextId(int nextId) {
		this.nextId = nextId;
	}

	public synchronized Direction getDirection() {
		return dir;
	}

	public synchronized void setDirection(Direction dir) {
		this.dir = dir;
	}
	
}
