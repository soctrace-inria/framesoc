/**
 * 
 */
package fr.inria.soctrace.framesoc.core.tools.management;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration file manager for external programs wrappers.
 * 
 * It looks for the executable path in the configuration file. If this configuration file is not
 * found, one is created with a default value.
 * 
 * The various path are defined by concrete subclasses.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class ExternalProgramConfigManager {

	private final static Logger logger = LoggerFactory
			.getLogger(ExternalProgramConfigManager.class);

	/**
	 * Get the absolute path of the configuration directory
	 * 
	 * @return configuration directory absolute path
	 */
	protected abstract String getConfDir();

	/**
	 * Get the absolute path of the configuration file
	 * 
	 * @return configuration file absolute path
	 */
	protected abstract String getConfFilePath();

	/**
	 * Get the absolute path of the default executable
	 * 
	 * @return default executable absolute path
	 */
	protected abstract String getDefaultExePath();

	/**
	 * Read the executable path from the configuration file or a default value
	 * 
	 * @return the executable path
	 */
	public String readPath() {

		// configuration directory
		File dir = new File(getConfDir());
		if (!dir.exists())
			dir.mkdir();

		// configuration file
		String confFilePath = getConfFilePath();
		File file = new File(getConfFilePath());

		try {
			// executable path
			String defaultExePath = getDefaultExePath();
			
			if (!file.exists()) {
				logger.debug("Configuration file not found. Create it: {}", confFilePath);
				System.err.println("Configuration file '" + confFilePath
						+ "' not found. Create it with default value (" + defaultExePath + ")");
				file.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				bw.write(defaultExePath);
				bw.close();
			} else {
				logger.debug("Configuration file found: {}", confFilePath);
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
				defaultExePath = line;
			}
			
			// Check for executable permission, since they are not set by
			// default when installed from update site
			File exeFile = new File(defaultExePath);
			if (exeFile.exists() && !exeFile.canExecute()) {
				exeFile.setExecutable(true);
			}
			
			return defaultExePath;
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;

	}

}
