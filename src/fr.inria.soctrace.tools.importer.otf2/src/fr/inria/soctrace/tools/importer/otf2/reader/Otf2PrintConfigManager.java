/**
 * 
 */
package fr.inria.soctrace.tools.importer.otf2.reader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.framesoc.core.tools.management.ExternalProgramConfigManager;
import fr.inria.soctrace.tools.importer.otf2.Activator;

/**
 * Configuration manager for Otf2 Print Wrapper.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
/* package */class Otf2PrintConfigManager extends ExternalProgramConfigManager {

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

	@Override
	protected String getConfDir() {
		String eclipseDir = Platform.getInstallLocation().getURL().getPath();
		return eclipseDir + CONF_DIR;
	}

	@Override
	protected String getConfFilePath() {
		String eclipseDir = Platform.getInstallLocation().getURL().getPath();
		return eclipseDir + CONF_FILE;
	}

	@Override
	protected String getDefaultExePath() {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		Path path = new Path(DEFAULT_PATH);
		URL fileURL = FileLocator.find(bundle, path, null);
		try {
			return FileLocator.resolve(fileURL).getPath().toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
