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
package fr.inria.soctrace.framesoc.ui;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColorService;
import fr.inria.soctrace.framesoc.ui.init.Initializer;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPerspective;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.DBMS;

/**
 * Eclipse startup class for the Framesoc UI plugin.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocUiStartup implements IStartup {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(FramesocUiStartup.class);

	@Override
	public void earlyStartup() {

		logger.debug("Early Framesoc UI startup");

		// Clean Framesoc views
		FramesocPartManager.getInstance().cleanFramesocParts(); // asyncExec

		// Check configuration file
		if (!Configuration.getInstance().fileExists()) {
			logger.debug("No configuration file found");
			setup();
		} else {
			logger.debug("Configuration file found");
			if (!validateConfFile(true))
				setup(); // the first time the configuration file is found invalid, we propose the
							// setup
		}

		if (!validateConfFile(false)) {
			message("The configuration file still contains non valid parameters.\nPlease initialize the system with correct values, using the menu Framesoc > Preferences.",
					true);
			logger.error("Configuration file still not valid.");
			return; // the second time, if still not valid, we terminate
		}

		// Manage tools if a System DB exists (it should be the case...)
		try {
			if (!FramesocManager.getInstance().isSystemDBExisting()) {
				logger.error("System DB still not existing. An error occurred.");
				return;
			} else {
				logger.debug("manage databases and tools");
				manageDatabasesAndTools();
			}
		} catch (SoCTraceException e) {
			logger.error(e.getMessage());
		}

		// Create the color service
		new FramesocColorService();

	}

	/**
	 * Validate the configuration file. The file is considered valid if: - the DBMS is correctly set
	 * - it is possible to successfully open a connection to the System DB
	 * 
	 * @return true if the file is valid, false otherwise
	 */
	private boolean validateConfFile(boolean show) {
		// check valid DBMS
		DBMS dbms = DBMS.toDbms(Configuration.getInstance().get(SoCTraceProperty.soctrace_dbms));
		if (dbms.equals(DBMS.UNKNOWN)) {
			message("The DBMS specified in the configuration file is unknown.", show);
			return false;
		} else {
			// check connection to DB
			if (dbms.equals(DBMS.SQLITE)) {
				if (!validateSQLite(show))
					return false;
			} else if (dbms.equals(DBMS.MYSQL)) {
				if (!validateMySQL(show))
					return false;
			}
		}
		return true;
	}

	private boolean validateMySQL(boolean show) {
		// check the connection trying to open the SystemDB
		try {
			return FramesocManager.getInstance().isSystemDBExisting();
		} catch (SoCTraceException e) {
			message("The MySQL parameters found in the configuration file are wrong.", show);
			return false;
		}
	}

	private boolean validateSQLite(boolean show) {
		// check if the DB folder exists
		String dbPath = Configuration.getInstance().get(SoCTraceProperty.sqlite_db_directory);
		File file = new File(dbPath);
		if (!file.exists()) {
			message("The SQLite database directory has not been found", show);
			return false;
		}

		// check the connection trying to open the SystemDB
		try {
			return FramesocManager.getInstance().isSystemDBExisting();
		} catch (SoCTraceException e) {
			message("The SQLite parameters found in the configuration file are wrong.", show);
			return false;
		}
	}

	private void message(final String s, boolean show) {
		if (!show) {
			logger.debug(s);
			return;
		}
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(),
						"Configuration Error", s);
			}
		});
	}

	private void setup() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {

				// Propose the initialization wizard
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (Initializer.INSTANCE.initializeSystem(window.getShell(), true)) {
					Initializer.INSTANCE.manageTools(window.getShell());
				}

				try {
					// XXX
					// If we don't remove the welcome screen (if present) programmatically
					// the perspective is not correctly loaded: the information used at
					// startup by the FramesocPartManager are probably not valid otherwise.
					// To be investigated.
					IIntroManager introManager = PlatformUI.getWorkbench().getIntroManager();
					IIntroPart part = introManager.getIntro();
					introManager.closeIntro(part);
					PlatformUI.getWorkbench().showPerspective(FramesocPerspective.ID, window);
				} catch (WorkbenchException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void manageDatabasesAndTools() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				Initializer.INSTANCE.manageDatabases();
				FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED, true);
				Initializer.INSTANCE.manageTools(window.getShell());
			}
		});
	}

}
