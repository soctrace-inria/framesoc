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
/**
 * 
 */
package fr.inria.soctrace.tools.framesoc.exporter.utils;

import java.io.Serializable;
import java.util.List;

import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.utils.DBMS;

/**
 * Version 0 of export metadata.
 * A simple version number is used for future evolutions.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ExportMetadata implements Serializable {

	/**
	 * Generated UID for serialization 
	 */
	private static final long serialVersionUID = -6736687970099438062L;

	/**
	 * Version
	 */
	public final int version = 0;
	
	/**
	 * Exported trace
	 */
	public Trace trace = null;
	
	/**
	 * Source system tools
	 */
	public List<Tool> tools = null;
	
	/**
	 * DBMS
	 */
	public DBMS dbms = DBMS.SQLITE;
	
}
