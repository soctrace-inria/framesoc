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
package fr.inria.soctrace.tools.importer.gstreamer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.importer.gstreamer.core.ExternalSort;
import fr.inria.soctrace.tools.importer.gstreamer.core.GStreamerConstants;
import fr.inria.soctrace.tools.importer.gstreamer.core.GStreamerRecord;

/**
 * Simple class to test the file sorting.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class GStreamerTestSorter {

	/**
	 * @param args: filename
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		if (args.length<1) {
			System.out.println("filename needed");
			return;
		}

		String filename = args[0];
		System.out.println("filename: " + filename);
		DeltaManager dm = new DeltaManager();
		
		dm.start();
		System.out.println("Internal sort");
		List<GStreamerRecord> records = getSortedRecords(filename);
		System.out.println("records: " + records.size());
		dm.end("internal sort (write to disk not included)");
		
		System.out.println("External sort");
		dm.start();
		ExternalSort.sort(new File(filename));
		dm.end("external sort");
	}

	public static List<GStreamerRecord> getSortedRecords(String filename) throws IOException {

		List<GStreamerRecord> records = new LinkedList<GStreamerRecord>();

		FileInputStream fstream = new FileInputStream(filename);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		DeltaManager dm = new DeltaManager();
		dm.start();
		GStreamerRecord record = getEventRecord(br);
		while (record!=null) {
			records.add(record);
			record = getEventRecord(br);
		}
		dm.end("read");

		dm.start();
		Collections.sort(records, new Comparator<GStreamerRecord>() {
			@Override
			public int compare(GStreamerRecord o1, GStreamerRecord o2) {
				return (o1.timestamp > o2.timestamp ? 1 : (o1==o2 ? 0 : -1));
			}
		});
		dm.end("sort");

		return records;
	}

	/**
	 * Get an event record from the given reader.
	 * 
	 * @param br reader
	 * @return the record or null if the file is finished
	 * @throws IOException 
	 */
	public static GStreamerRecord getEventRecord(BufferedReader br) throws IOException {
		String strLine;
		GStreamerRecord record = null;
		while (record == null) {
			if ((strLine = br.readLine()) == null)
				return null;

			strLine = strLine.trim();
			if( strLine.equals("") ) continue;
			if( strLine.startsWith("#")) continue;

			record = new GStreamerRecord(GStreamerConstants.DEFAULT_HEADER, strLine);
		}
		return record;	
	}

}
