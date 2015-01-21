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
package fr.inria.soctrace.tools.framesoc.exporter.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Serializer {

	public void serialize(ExportMetadata metadata, String path) throws SoCTraceException {
		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(metadata);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new SoCTraceException(e);
		}
	}

	public ExportMetadata deserialize(String path) throws SoCTraceException {
		ExportMetadata metadata = null;
		try {
			FileInputStream fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			metadata = (ExportMetadata)in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			throw new SoCTraceException(e);
		} 
		return metadata;
	}

}
