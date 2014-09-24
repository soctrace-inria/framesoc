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
package fr.inria.soctrace.tools.importer.gstreamer.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Class providing an external sort algorithm implementation.
 * 
 * External sort is used to sort a large amount of data that
 * not necessarily fit into main memory.
 * To do that, a secondary storage device (disk) is used to swap
 * intermediate results.
 * 
 * Most of this code comes from: 
 * https://code.google.com/p/externalsortinginjava/
 */
public class ExternalSort {

	static int DEFAULTMAXTEMPFILES = 1024;

	private static File input;
	private static File output;

	public static void sort(File in) throws IOException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		try {
			if (checkIfSorted(in))
				return;
			input = in;
			output = new File(in.getPath() + ".out");
			mergeSortedFiles(ExternalSort.sortInBatch(input),output);
			rename();
		}
		finally {
			dm.end("sorting");			
		}
	}

	private static boolean checkIfSorted(File input) throws IOException {
		BufferedReader fbr = new BufferedReader(new InputStreamReader(
				new FileInputStream(input), Charset.defaultCharset()));
		try {
			while (true) {
				String line = fbr.readLine();
				if ( line == null)
					return false;
				if ( line.trim().equals(""))
					continue;
				if ( !line.startsWith("#"))
					return false;
				else {
					if (line.indexOf("sorted")!=-1)
						return true;
					else 
						continue;
				}
			}	
		} finally {
			fbr.close();
		}
	}

	private static void rename() {
		File tmp = new File(input.getPath());
		input.renameTo(new File(input.getPath()+".bkp"));
		output.renameTo(tmp);
	}

	private static Comparator<String> defaultcomparator = new Comparator<String>() {
		@Override
		public int compare(String r1, String r2) {
			if (r1.trim().equals(""))
				return 1;
			if (r2.trim().equals(""))
				return -1;
			if (r1.startsWith("#"))
				return -1;
			if (r2.startsWith("#"))
				return 1;
			Long a = Long.valueOf(r1.split(",")[0]);
			Long b = Long.valueOf(r2.split(",")[0]);
			return a.compareTo(b);
		}
	};

	private static long estimateBestSizeOfBlocks(File filetobesorted,
			int maxtmpfiles) {
		long sizeoffile = filetobesorted.length() * 2;
		long blocksize = sizeoffile / maxtmpfiles
				+ (sizeoffile % maxtmpfiles == 0 ? 0 : 1);

		long freemem = Runtime.getRuntime().freeMemory();
		if (blocksize < freemem / 2) {
			blocksize = freemem / 2;
		}
		return blocksize;
	}

	private static List<File> sortInBatch(File file)
			throws IOException {
		return sortInBatch(file, defaultcomparator, DEFAULTMAXTEMPFILES,
				Charset.defaultCharset(), null, false);
	}

	private static List<File> sortInBatch(File file, Comparator<String> cmp,
			int maxtmpfiles, Charset cs, File tmpdirectory,
			boolean distinct, int numHeader, boolean usegzip)
					throws IOException {
		List<File> files = new ArrayList<File>();
		BufferedReader fbr = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), cs));
		long blocksize = estimateBestSizeOfBlocks(file, maxtmpfiles);// in
		// bytes

		try {
			List<String> tmplist = new ArrayList<String>();
			String line = "";
			try {
				int counter = 0;
				while (line != null) {
					long currentblocksize = 0;// in bytes
					while ((currentblocksize < blocksize)
							&& ((line = fbr.readLine()) != null)) {
						// as  long as  you have enough memory
						if (counter < numHeader) {
							counter++;
							continue;
						}
						tmplist.add(line);
						currentblocksize += StringSizeEstimator
								.estimatedSizeOf(line);
					}
					files.add(sortAndSave(tmplist, cmp, cs,
							tmpdirectory, distinct, usegzip));
					tmplist.clear();
				}
			} catch (EOFException oef) {
				if (tmplist.size() > 0) {
					files.add(sortAndSave(tmplist, cmp, cs,
							tmpdirectory, distinct, usegzip));
					tmplist.clear();
				}
			}
		} finally {
			fbr.close();
		}
		return files;
	}

	private static List<File> sortInBatch(File file, Comparator<String> cmp,
			int maxtmpfiles, Charset cs, File tmpdirectory, boolean distinct)
					throws IOException {
		return sortInBatch(file, cmp, maxtmpfiles, cs, tmpdirectory,
				distinct, 0, false);
	}

	private static File sortAndSave(List<String> tmplist,
			Comparator<String> cmp, Charset cs, File tmpdirectory,
			boolean distinct, boolean usegzip) throws IOException {
		Collections.sort(tmplist, cmp);
		File newtmpfile = File.createTempFile("sortInBatch",
				"flatfile", tmpdirectory);
		newtmpfile.deleteOnExit();
		OutputStream out = new FileOutputStream(newtmpfile);
		int ZIPBUFFERSIZE = 2048;
		if (usegzip)
			out = new GZIPOutputStream(out, ZIPBUFFERSIZE) {
			{
				def.setLevel(Deflater.BEST_SPEED);
			}
		};
		BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(
				out, cs));
		String lastLine = null;
		try {
			for (String r : tmplist) {
				// Skip duplicate lines
				if (!distinct || !r.equals(lastLine)) {
					fbw.write(r);
					fbw.newLine();
					lastLine = r;
				}
			}
		} finally {
			fbw.close();
		}
		return newtmpfile;
	}

	private static int mergeSortedFiles(List<File> files, File outputfile) throws IOException {
		return mergeSortedFiles(files, outputfile, defaultcomparator,
				Charset.defaultCharset());
	}

	private static int mergeSortedFiles(List<File> files, File outputfile,
			final Comparator<String> cmp, Charset cs, boolean distinct,
			boolean append, boolean usegzip) throws IOException {
		ArrayList<BinaryFileBuffer> bfbs = new ArrayList<BinaryFileBuffer>();
		for (File f : files) {
			final int BUFFERSIZE = 2048;
			InputStream in = new FileInputStream(f);
			BufferedReader br;
			if (usegzip) {
				br = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(in, BUFFERSIZE), cs));
			} else {
				br = new BufferedReader(new InputStreamReader(in,
						cs));
			}

			BinaryFileBuffer bfb = new BinaryFileBuffer(br);
			bfbs.add(bfb);
		}
		BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputfile, append), cs));
		int rowcounter = merge(fbw,cmp,distinct, bfbs);
		for (File f : files) f.delete();
		return rowcounter;
	}

	private static int merge(BufferedWriter fbw, final Comparator<String> cmp, boolean distinct, 
			List<BinaryFileBuffer> buffers) throws IOException {
		PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(
				11, new Comparator<BinaryFileBuffer>() {
					@Override
					public int compare(BinaryFileBuffer i,
							BinaryFileBuffer j) {
						return cmp.compare(i.peek(), j.peek());
					}
				});
		for (BinaryFileBuffer bfb: buffers)
			if(!bfb.empty())
				pq.add(bfb);
		int rowcounter = 0;
		String lastLine = null;
		boolean written = false;
		try {
			while (pq.size() > 0) {
				BinaryFileBuffer bfb = pq.poll();
				String r = bfb.pop();
				if (!written) {
					written = true;
					fbw.write("# sorted\n");
				}
				// Skip duplicate lines
				if (!distinct || !r.equals(lastLine)) {
					fbw.write(r);
					fbw.newLine();
					lastLine = r;
				}
				++rowcounter;
				if (bfb.empty()) {
					bfb.fbr.close();
				} else {
					pq.add(bfb); // add it back
				}
			}
		} finally {
			fbw.close();
			for (BinaryFileBuffer bfb : pq)
				bfb.close();
		}
		return rowcounter;

	}

	private static int mergeSortedFiles(List<File> files, File outputfile,
			final Comparator<String> cmp, Charset cs, boolean distinct)
					throws IOException {
		return mergeSortedFiles(files, outputfile, cmp, cs, distinct,
				false, false);
	}

	private static int mergeSortedFiles(List<File> files, File outputfile,
			final Comparator<String> cmp, Charset cs) throws IOException {
		return mergeSortedFiles(files, outputfile, cmp, cs, false);
	}

}

class BinaryFileBuffer {
	public BufferedReader fbr;
	private String cache;
	private boolean empty;

	public BinaryFileBuffer(BufferedReader r)
			throws IOException {
		this.fbr = r;
		reload();
	}

	public boolean empty() {
		return this.empty;
	}

	private void reload() throws IOException {
		try {
			if ((this.cache = this.fbr.readLine()) == null) {
				this.empty = true;
				this.cache = null;
			} else {
				this.empty = false;
			}
		} catch (EOFException oef) {
			this.empty = true;
			this.cache = null;
		}
	}

	public void close() throws IOException {
		this.fbr.close();
	}

	public String peek() {
		if (empty())
			return null;
		return this.cache.toString();
	}

	public String pop() throws IOException {
		String answer = peek();
		reload();
		return answer;
	}
}

/**
 * @author Eleftherios Chetzakis
 */
final class StringSizeEstimator {

	private static int OBJ_HEADER;
	private static int ARR_HEADER;
	private static int INT_FIELDS = 12;
	private static int OBJ_REF;
	private static int OBJ_OVERHEAD;
	private static boolean IS_64_BIT_JVM;

	private StringSizeEstimator() {}

	static {
		IS_64_BIT_JVM = true;
		String arch = System.getProperty("sun.arch.data.model");
		if (arch != null) {
			if (arch.indexOf("32") != -1) {
				IS_64_BIT_JVM = false;
			}
		}
		OBJ_HEADER = IS_64_BIT_JVM ? 16 : 8;
		ARR_HEADER = IS_64_BIT_JVM ? 24 : 12;
		OBJ_REF = IS_64_BIT_JVM ? 8 : 4;
		OBJ_OVERHEAD = OBJ_HEADER + INT_FIELDS + OBJ_REF + ARR_HEADER;

	}

	public static long estimatedSizeOf(String s) {
		return (s.length() * 2) + OBJ_OVERHEAD;
	}
}
