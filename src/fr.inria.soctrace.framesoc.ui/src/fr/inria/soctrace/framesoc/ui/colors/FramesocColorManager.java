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
package fr.inria.soctrace.framesoc.ui.colors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Singleton for color management in Framesoc.
 * 
 * <p>
 * The typical user code usage of this class implies
 * getting or setting colors for entities.
 * 
 * <p>
 * An entity is identified by a string:
 * <ul>
 * <li> for {@link EventType} this string shall be the result of {@link EventType#getName()}
 * <li> for {@link EventProducer} this string shall be the result of {@link EventProducer#getWholeName()} 
 * </ul>
 * 
 * <p>
 * Note that when you request a color for an unknown 
 * entity, a random color is generated. If you want 
 * to save these randomly generated colors, remember 
 * to explicitly call the save methods provided.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocColorManager {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(FramesocColorManager.class);	

	private final static String CONF_DIR = "configuration" + File.separator + Activator.PLUGIN_ID + File.separator;
	private final static String ET_FILE = CONF_DIR + "event_type_colors";
	private final static String EP_FILE = CONF_DIR + "event_producer_colors";
	private final static String SEPARATOR = ",";
	private final static String EQUAL = "=";
	private final static String ERROR_NEAR = "Color configuration file error near: ";
	
	private Map<String, FramesocColor> etColors = new HashMap<String, FramesocColor>();
	private Map<String, FramesocColor> epColors = new HashMap<String, FramesocColor>();
	
	private static FramesocColorManager instance = new FramesocColorManager();
	
	private boolean dirtyTypes = false;
	private boolean dirtyProducers = false;
	
	private FramesocColorManager() {
		try {
			loadFile(etColors, ET_FILE);
			loadFile(epColors, EP_FILE);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Public API
	 */
	
	/**
	 * Singleton instance getter
	 * @return the instance of the manager
	 */
	public static FramesocColorManager getInstance() {
		return instance;
	}	
	
	/**
	 * Add the event type colors that are NOT already known to
	 * the system. 
	 * 
	 * This method is used by importers to set default colors.
	 * 
	 * @param colors map between event type names and colors
	 */
	public void addEventTypeColors(Map<String, FramesocColor> colors) {
		addColors(colors, etColors);
		updateFile(etColors, ET_FILE);
	}

	/**
	 * Add the event producer colors that are NOT already known to
	 * the system. 
	 * 
	 * This method is used by importers to set default colors.
	 * 
	 * @param colors map between event producer names and colors
	 */
	public void addEventProducerColors(Map<String, FramesocColor> colors) {
		addColors(colors, epColors);
		updateFile(epColors, EP_FILE);
	}

	/**
	 * Get the event type color, given the name.
	 * @param name event type name, defined as the return of {@link EventType#getName()}
	 * @return the event type color
	 */
	public FramesocColor getEventTypeColor(String name) {
		return getColor(name, etColors);
	}
	
	/**
	 * Get the event producer color, given the name.
	 * @param name event producer name, defined as the return of {@link EventProducer#getWholeName()}
	 * @return the event producer name
	 */
	public FramesocColor getEventProducerColor(String name) {
		return getColor(name, epColors);
	}
	
	/**
	 * Set the event type color.
	 * @param name event type name, defined as the return of {@link EventType#getName()}
	 * @param color Framesoc color for the event type
	 */
	public void setEventTypeColor(String name, FramesocColor color) {
		setColor(name, color, etColors);
	}
	
	/**
	 * Set the event producer color.
	 * @param name event producer name, defined as the return of {@link EventProducer#getWholeName()}
	 * @param color Framesoc color for the event producer
	 */
	public void setEventProducerColor(String name, FramesocColor color) {
		setColor(name, color, epColors);
	}
	
	/**
	 * Remove the event type color.
	 * @param name event type name, defined as the return of {@link EventType#getName()}
	 */
	public void removeEventTypeColor(String name) {
		removeColor(name, etColors);
	}
	
	/**
	 * Remove the event producer color.
	 * @param name event producer name, defined as the return of {@link EventProducer#getWholeName()}
	 */
	public void removeEventProducerColor(String name) {
		removeColor(name, epColors);
	}

	/**
	 * Save the event type colors on the configuration file.
	 */
	public void saveEventTypeColors() {
		updateFile(etColors, ET_FILE);
	}

	/**
	 * Save the event producer colors on the configuration file.
	 */
	public void saveEventProducerColors() {
		updateFile(epColors, EP_FILE);
	}
	
	/**
	 * Load the event type colors configuration file from disk.
	 */
	public void loadEventTypeColors() {
		try {
			loadFile(etColors, ET_FILE);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load the event producer colors configuration file from disk.
	 */
	public void loadEventProducerColors() {
		try {
			loadFile(etColors, ET_FILE);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a collection of all known event type names associated
	 * with colors.
	 * @return A collection of event type names
	 */
	public Collection<String> getEventTypeNames() {
		return Collections.unmodifiableCollection(etColors.keySet());
	}
	
	/**
	 * Get a collection of all known event producer names associated
	 * with colors.
	 * @return A collection of event producer names
	 */
	public Collection<String> getEventProducerNames() {
		return Collections.unmodifiableCollection(epColors.keySet());
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Event Type Colors\n");
		builder.append(printMap(etColors));
		builder.append("Event Producer Colors\n");
		builder.append(printMap(epColors));
		return builder.toString();
	}
	
	/*
	 * utilities
	 */
	
	private void setDirty(Map<String, FramesocColor> colors, boolean dirty) {
		if (colors.equals(etColors))
			dirtyTypes = dirty;
		else
			dirtyProducers = dirty;		
	}

	private boolean isDirty(Map<String, FramesocColor> colors) {
		if (colors.equals(etColors))
			return dirtyTypes;
		else
			return dirtyProducers;		
	}

	private String printMap(Map<String, FramesocColor> colors) {
		StringBuilder builder = new StringBuilder();
		Iterator<Entry<String, FramesocColor>> it = colors.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, FramesocColor> e = it.next();
			builder.append(e.getKey());
			builder.append("=");
			builder.append(e.getValue().toString());
			builder.append("\n");
		}
		return builder.toString();
	}
		
	private void addColors(Map<String, FramesocColor> newColors,
			Map<String, FramesocColor> colors) {
		for (String newName: newColors.keySet()) {
			if (!colors.containsKey(newName)) {
				colors.put(newName, newColors.get(newName));
				setDirty(colors, true);
			}
		}
	}
	
	private FramesocColor getColor(String name, Map<String, FramesocColor> colors) {
		if (!colors.containsKey(name)) {
			colors.put(name, FramesocColor.generateFramesocColor(name));
			setDirty(colors, true);
		}
		return colors.get(name);
	}
	
	private void setColor(String name, FramesocColor color, Map<String, FramesocColor> colors) {
		if (colors.containsKey(name))
			colors.get(name).dispose();
		colors.put(name, color);
		setDirty(colors, true);
	}
	
	private void removeColor(String name, Map<String, FramesocColor> colors) {
		if (colors.containsKey(name)) {
			colors.remove(name);	
			setDirty(colors, true);
		}
	}
	
	private synchronized void loadFile(Map<String, FramesocColor> colors, String path) throws SoCTraceException {
		
		disposeColors(colors);
	
		try {			
			File dir = new File(Platform.getInstallLocation().getURL().getPath() + CONF_DIR);
			if (!dir.exists())
				dir.mkdir();
			String absolutePath = Platform.getInstallLocation().getURL().getPath() + path; 
			File file = new File(absolutePath);
			if (!file.exists()) {
				logger.debug("Configuration file not found. Create an empty one: {}", absolutePath);
				file.createNewFile();
			} else {
				logger.debug("Configuration file found: {}", absolutePath);
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = "";
				while ( (line = br.readLine()) != null ) {
					if( line.equals("") ) continue;
					if( line.startsWith("#") ) continue;
					String [] prop = line.split(EQUAL);
					if (prop.length!=2) {
						br.close();
						throw new SoCTraceException(ERROR_NEAR + line);
					}
					colors.put(prop[0], getFramesocColor(prop[1]));
				}
				br.close();
			}
			setDirty(colors, false);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private synchronized void updateFile(Map<String, FramesocColor> colors, String path) {
		
		if (!isDirty(colors))
			return;
		
		try {
			File file = new File(Platform.getInstallLocation().getURL().getPath() + path);
			file.delete();
			file.createNewFile();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			Iterator<Entry<String, FramesocColor>> it = colors.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, FramesocColor> entry = it.next();
				bw.write(entry.getKey()+EQUAL+getString(entry.getValue())+"\n");
			}
			bw.close();
			setDirty(colors, false);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private FramesocColor getFramesocColor(String color) throws SoCTraceException {
		String [] colors = color.split(SEPARATOR);
		if (colors.length != 3)
			throw new SoCTraceException(ERROR_NEAR + color);
		try {
			int r = Integer.valueOf(colors[0]);
			int g = Integer.valueOf(colors[1]);		
			int b = Integer.valueOf(colors[2]);	
			return new FramesocColor(r, g, b);
		} catch (NumberFormatException e) {
			throw new SoCTraceException(ERROR_NEAR + color);
		}
	}
	
	private String getString(FramesocColor color) {
		return color.red + SEPARATOR + color.green + SEPARATOR + color.blue;
	}
	
	private void disposeColors(Map<String, FramesocColor> colors) {
		for (FramesocColor c: colors.values())
			c.dispose();
		colors.clear();
	}
	
}
