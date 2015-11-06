package fr.inria.soctrace.framesoc.headless;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.headless.launcher.HeadlessPluginLauncher;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

public class CommandManager {
	protected Map<String, String> commandBundle;
	protected Map<String, String> commands;
	
	protected String selectedType;

	private static final String POINT_ID = "fr.inria.soctrace.framesoc.headless.command"; //$NON-NLS-1$
	private static final String OP_CLASS = "class"; //$NON-NLS-1$
	private static final String OP_COMMAND = "command"; //$NON-NLS-1$
	
	private static final Logger logger = LoggerFactory
			.getLogger(CommandManager.class);
	
	public CommandManager() {
		try {
			intialize();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Map<String, String> getCommands() {
		return commands;
	}

	private void intialize() throws SoCTraceException {
		commands = new HashMap<String, String>();
		commandBundle = new HashMap<String, String>();

		final IExtensionRegistry reg = Platform.getExtensionRegistry();
		final IConfigurationElement[] config = reg
				.getConfigurationElementsFor(POINT_ID);
		logger.debug(config.length + " commands detected:");

		for (final IConfigurationElement e : config) {
			commands.put(e.getAttribute(OP_COMMAND), e.getAttribute(OP_CLASS));
			commandBundle.put(e.getAttribute(OP_COMMAND), e.getContributor()
					.getName());
			logger.debug("    " + e.getAttribute(OP_COMMAND));
		}
	}
	
	public HeadlessPluginLauncher instantiateLauncher(final String command) {
		HeadlessPluginLauncher aNewLauncher = null;
		
		final Bundle mybundle = Platform.getBundle(commandBundle.get(command));
		try {
			aNewLauncher = (HeadlessPluginLauncher) mybundle.loadClass(
					commands.get(command)).newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return aNewLauncher;
	}

}
