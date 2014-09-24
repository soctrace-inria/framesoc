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

/**
 * Constants for GStreamer parser
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * @author "Damien Rousseau"
 */
public class GStreamerConstants {
	
	/**
	 * Default trace header
	 */
	public static final String DEFAULT_HEADER = "timestamp,processID,ThreadID,DebugCategory,UnknowInfo,DebugLevel,source_file,line,function,function:line,object,message";
	
	/**
	 * Default field separator
	 */
	public static final String DEFAULT_SEPARATOR = ",";
	
	// pages
	
	/**
	 * Expected size for a page
	 */
	// DRO 19/08/2013 - Page size optimization
	public static final int PAGE_EXPECTED_SIZE = 200000;	// TODO make this configurable (e.g. -p=30000)

	/**
	 * Maximum page variation
	 */
	public static final int PAGE_SIZE_DELTA = 300;
	
	// launch options
	
	/**
	 * Default event type name for frame start.
	 */
	public static String DEFAULT_FRAME_START = "ffmpeg:gst_ffmpegdec_chain:'Received";                                      
	
	/**
	 * Event type name command line option
	 */
	public static String FRAME_START_OPT = "s";
	
	/**
	 * Default value for allowing overlapping frames over pages.
	 */
	public static boolean DEFAULT_FRAME_OVELAPPING = false;
	
	/**
	 * Flag to allow overlapping frames over pages.
	 */
	public static String FRAME_OVELAPPING_FLAG = "o"; 
	
	// event type
	
	/**
	 * Param Duration Name
	 */
	public static String DURATION_NAME = "DURATION";

	/**
	 * Param Duration Name
	 */
	public static String DURATION_TYPE = "LONG";
	
	// trace type
	
	/**
	 * Trace Type name
	 */
	public static final String TRACE_TYPE = "GStreamer.hadas.0.0";

	/**
	 * Parameters describing a GStreamer trace configuration.
	 */
	public static enum GStreamerTraceParamType {
		
		FRAME_START_EVENT_TYPE_ID("FRAME_START_EVENT_TYPE_ID" ,"INTEGER"),
		NUMBER_OF_FRAMES("NUMBER_OF_FRAMES" ,"INTEGER");

		/** Name of the parameters in the DB */
		private String name;
		/** Type of the parameters in the DB */
		private String type;

		/**
		 * @param label
		 * @param name
		 * @param type
		 */
		private GStreamerTraceParamType(String name, String type) {
			this.name = name;
			this.type = type;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}
		
	}
	
	// event producers 
	
	/**
	 * Possible types of GStreamer event producers.
	 */
	public static enum GStreamerEventProducer {
		
		PROCESS("PROCESS"),
		THREAD("THREAD");
		
		private String name;

		/**
		 * @param name
		 */
		private GStreamerEventProducer(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}		
	}

}
