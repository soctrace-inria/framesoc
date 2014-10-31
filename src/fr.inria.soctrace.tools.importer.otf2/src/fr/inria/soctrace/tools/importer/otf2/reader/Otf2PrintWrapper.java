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
package fr.inria.soctrace.tools.importer.otf2.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.tools.management.ExternalProgramWrapper;
import fr.inria.soctrace.tools.importer.otf2.Activator;

/**
 * Wrapper for otf2-print program.
 * 
 * It looks for the otf2-print executable path in the configuration file
 * ./<eclipse.dir>/configuration/<plugin.name>/otf2-print.path.
 * 
 * If this file is not found, one is created with a default value, pointing to
 * the precompiled executable (./<plugin.name>/exe/otf2-print).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Otf2PrintWrapper extends ExternalProgramWrapper {

	private final static Logger logger = LoggerFactory.getLogger(Otf2PrintWrapper.class);

	/**
	 * Configuration directory
	 */
	private final static String CONF_DIR = "configuration" + File.separator + Activator.PLUGIN_ID
			+ File.separator;

	/**
	 * Configuration file
	 */
	private final static String CONF_FILE = CONF_DIR + "otf2-print.path";

	/**
	 * Default oft2-print executable location
	 */
	private static final String DEFAULT_PATH = "exe" + File.separator + "otf2-print";

	/**
	 * Constructor
	 * 
	 * @param arguments
	 *            program arguments
	 */
	public Otf2PrintWrapper(List<String> arguments) {
		super(readPath(), arguments);
	}

	/**
	 * Read the executable path from the configuration file
	 * 
	 * @return the executable path
	 */
	private static String readPath() {

		String eclipseDir = Platform.getInstallLocation().getURL().getPath();

		// configuration directory
		File dir = new File(eclipseDir + CONF_DIR);
		if (!dir.exists())
			dir.mkdir();

		// configuration file
		String absolutePath = eclipseDir + CONF_FILE;
		File file = new File(absolutePath);

		try {
			// executable path
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			Path path = new Path(DEFAULT_PATH);
			URL fileURL = FileLocator.find(bundle, path, null);
			String executablePath = FileLocator.resolve(fileURL).getPath().toString();

			if (!file.exists()) {
				logger.debug("Configuration file not found. Create it: {}", absolutePath);
				System.err.println("Configuration file '" + absolutePath
						+ "' not found. Create it with default value (" + executablePath + ")");
				file.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				bw.write(executablePath);
				bw.close();
			} else {
				logger.debug("Configuration file found: {}", absolutePath);
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = "";
				while ((line = br.readLine()) != null) {
					if (line.equals(""))
						continue;
					if (line.startsWith("#"))
						continue;
					break;
				}
				br.close();
				executablePath = line;
			}
			return executablePath;
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;

	}

}
