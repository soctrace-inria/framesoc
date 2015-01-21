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
package fr.inria.soctrace.lib.search.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.IModelElement;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Utility class providing printing methods for the objects
 * of the data model or collection of such objects, as returned
 * by the search interface methods.
 * 
 * <p>
 * Methods to browse among analysis results are also provided.
 * 
 * <p>
 * It may be extended in the future in parallel with the
 * evolution of the search interface, in order to answer to 
 * real use cases.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Printer {

	/*
	 * Print methods
	 */
	
	/**
	 * Print a list of Trace objects.
	 * @param tlist list of Trace objects 
	 */
	public static void printTraceList(List<Trace> tlist) {
		if (tlist == null) {
			System.out.println("Empty set");
		} else {
			System.out.println("Number of traces: " + tlist.size());
			for (Trace t: tlist) {
				t.print(true);
			}
		}
	}
	
	/**
	 * Print a list of Event objects
	 * @param elist list of Event objects
	 */
	public static void printEventList(List<Event> elist) {
		if (elist == null) {
			System.out.println("Empty set");
		} else {
			System.out.println("Number of events: " + elist.size());
			for (Event e: elist) {
				e.print(true);
			}
		}
	}

	/**
	 * Print a list of AnalysisResult objects.
	 * @param alist list of AnalysisResult objects.
	 * @throws SoCTraceException
	 */
	public static void printAnalysisResultList(List<AnalysisResult> alist) throws SoCTraceException {
		if (alist == null) {
			System.out.println("Empty set");
		} else {
			System.out.println("Number of analysis results: " + alist.size());
			for (AnalysisResult a: alist) {
				a.print();
			}
		}
	}

	/**
	 * Print a list of IModelElement objects.
	 * @param tlist list of IModelElement objects.
	 */
	public static void printIModelElementsList(List<? extends IModelElement> tlist) {
		if (tlist == null) {
			System.out.println("Empty set");
		} else {
			System.out.println("Number of elements: " + tlist.size());
			for (IModelElement t: tlist) {
				System.out.println(t.toString());
			}
		}
	}
	
	/**
	 * Print a generic IModelElement object.
	 * @param element IModelElement
	 */
	public static void printIModelElement(IModelElement element) {
	
		if (element == null)
			System.out.println("Empty set");
		else
			System.out.println(element.toString());
	}
	
	/**
	 * Print the current method name.
	 */
	public static void printCurrentMethodName() {
	     StackTraceElement stackTraceElements[] = (new Throwable()).getStackTrace();
	     String name = stackTraceElements[1].getMethodName();
	     System.out.println ("############# " + name + " #############");
	}

	/*
	 * Result browsing
	 */
	
	/**
	 * Interactively browse among analysis results, searching them by specifying:
	 * - the trace
	 * - the analysis tool
	 * - the type of analysis result
	 * - the analysis result instance of that type
	 */
	public static void selectResult() {
		ITraceSearch traceSearch = null;
		try {
			traceSearch = new TraceSearch().initialize();
		
			while (true) {
				
				// Get trace
				List<Trace> trlist = traceSearch.getTraces();
				Trace trace = (Trace) selectObject("Trace", trlist);
				
				// Get analysis tool
				List<Tool> tlist = traceSearch.getToolByType("ANALYSIS");
				Tool tool = (Tool) selectObject("Analysis Tool", tlist);
				
				// Get type
				List<AnalysisResultType> typeList = Arrays.asList(AnalysisResultType.values());
				AnalysisResultType type = (AnalysisResultType) selectObject("Analysis Result Type", typeList);
				
				// Get results
				List<AnalysisResult> alist = traceSearch.getAnalysisResultsByToolAndType(trace, tool, type);
				System.out.println("Result of type '" + type.toString() + "' produced by tool '" + tool.getName() + "'");
				choseAndDisplayResult(alist, traceSearch, trace);
				System.out.println("");
				
				// Continue/Exit
				System.out.println("****************");
				System.out.println("Exit/Continue ?");
				System.out.println("0 - exit");
				System.out.println("1 - continue");
				System.out.println("****************");
				System.out.println("Choice? ");
				int input = readInt(2);
				if ( input == 0 ) {
					break;
				}
				System.out.println("");
			}
			
			System.out.println("The End");
			
		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			TraceSearch.finalUninitialize(traceSearch);
		}

	}

	/**
	 * Provide to the user a list of analysis results and retrieve the
	 * analysis result data for the selected one. Selection is interactive.
	 * 
	 * @param alist List of AnalysisResult objects
	 * @param traceSearch trace search object
	 * @param trace trace we are working on
	 * @throws SoCTraceException
	 */
	public static void choseAndDisplayResult(List<AnalysisResult> alist, ITraceSearch traceSearch, Trace trace)
			throws SoCTraceException {
				
		if (alist.size() == 0) {
			System.out.println("No result of this type for this tool");
			return;
		}
		
		Map<Integer, AnalysisResult> tmp = new HashMap<Integer, AnalysisResult>();
		int i = 0;
		for (AnalysisResult ar: alist) {
			System.out.println(String.valueOf(i) + " - " + ar.toString());
			tmp.put(i++, ar);
		}
		
		System.out.println("Result? ");
		AnalysisResult ar = tmp.get(readInt(tmp.size()));
		if (ar == null)
			throw new SoCTraceException("input error");
		System.out.println("");
		
		DeltaManager dm = new DeltaManager();
		dm.start();
		traceSearch.getAnalysisResultData(trace, ar);
		dm.end("getAnalysisResultData: ");
		
		System.out.println(ar.getDescription());
		ar.print();
		System.out.println("");
	
	}
	
	/**
	 * Read an integer in [0, sup).
	 * @param sup Sup of the set of number (1 is assumed if sup<=0)
	 * @return the inserted number
	 * @throws SoCTraceException
	 */
	private static int readInt(int sup) throws SoCTraceException {
		if (sup<=0)
			sup=1;
		try {
			BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				try {
					String line = is.readLine();
					int value = Integer.parseInt(line);
					if (value>=0 && value<sup)
						return value;
					else 
						System.err.println("Warning: value out of range [0, " + (sup-1) + "]");
				} catch (NumberFormatException nfe) {
					System.err.println("Warning: insert an integer!");
				}
			}
		} catch (IOException e) {
			throw new SoCTraceException(e);
		} 
	}
	
	/**
	 * Interactively select an object in a list specifying is positional
	 * number. The object is then returned.
	 * 
	 * @param name Name of the class of objects to be displayed
	 * @param list List of objects
	 * @return the selected object
	 * @throws SoCTraceException
	 */
	private static <T> T selectObject(String name, List<T> list) throws SoCTraceException {

		System.out.println("Select the " + name);
		Map<Integer, T> objMap = new HashMap<Integer, T>();
		int i = 0;
		for (T t: list) {
			System.out.println(String.valueOf(i) + " - " + t.toString());
			objMap.put(i++, t);
		}
		System.out.println(name + "?");	
		T obj = objMap.get(readInt(objMap.size()));
		if (obj == null)
			throw new SoCTraceException("input error");
		System.out.println("");
		return obj;
		
	}
		
}
