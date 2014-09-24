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
/**
 * 
 */
package fr.inria.soctrace.tools.framesoc.exporter.dbexporter;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.ToolQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.dbmanager.DBManager;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.DBMS;
import fr.inria.soctrace.lib.utils.Portability;
import fr.inria.soctrace.tools.framesoc.exporter.utils.ExportMetadata;
import fr.inria.soctrace.tools.framesoc.exporter.utils.Serializer;

/**
 * The exporter generates the following output:
 * - <DBNAME>.meta file, containing a serialized ExportMetadata object.
 * - <DBNAME>.db file, containing the dump of the DB. The format of
 *   this file differs according to the DBMS.
 *  
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ExporterJob extends Job {

	private final static Logger logger = LoggerFactory.getLogger(ExporterJob.class);

	/**
	 * Input
	 */
	private ExporterInput input;

	public ExporterJob(String name, ExporterInput input) {
		super(name);
		this.input = input;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		monitor.setTaskName(getName());
		monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);

		try {
			
			Thread th = new Thread() {
				@Override
				public void run(){
					logger.debug("thread body");		
					try {
						export();
						feedback(true, "");
						logger.debug("export done");
					} catch (Exception e) {
						logger.debug("cleaning");
						delete(getDumpPath()+".db");
						delete(getDumpPath()+".meta");
					}
				}
			};

			th.start();

			while(th.isAlive()) {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException ie) {
					logger.debug("interrupted exception in main thread");
					th.interrupt();
					throw new SoCTraceException(ie);
				}
				if (monitor.isCanceled()) {
					logger.debug("user pressed cancel");
					logger.debug("interrupting thread");
					th.interrupt();
					return Status.CANCEL_STATUS;
				}
			}

		} catch (SoCTraceException e) {
			e.printStackTrace();
			feedback(false, e.toString());
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}


	private void export() throws SoCTraceException {
		// export metadata
		Serializer serializer = new Serializer();
		ExportMetadata metadata = new ExportMetadata();
		metadata.dbms = DBMS.toDbms(Configuration.getInstance().get(SoCTraceProperty.soctrace_dbms.toString()));
		metadata.trace = input.trace;
		
		SystemDBObject sysDB = null;
		try {
			sysDB = SystemDBObject.openNewIstance();
			ToolQuery tq = new ToolQuery(sysDB);
			metadata.tools = tq.getList();
		} finally {
			DBObject.finalClose(sysDB);
		}
		serializer.serialize(metadata, getDumpPath() + ".meta");
		
		// export db
		DBManager dbm = DBManager.getDBManager(input.trace.getDbName());
		dbm.exportDB(getDumpPath() + ".db");			

	}

	private void feedback(final boolean ok, final String s) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (ok)
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
							"Exporter", "Database correctly exported.\n" + s);
				else
					MessageDialog.openError(Display.getCurrent().getActiveShell(), 
							"Exporter", "Error while exporting.\n" + s);
			}
		});
	}

	private String getDumpPath() {
		return Portability.normalize(input.directory + "/" + input.trace.getDbName());
	}

	private void delete(String file) {
		try{
			File f = new File(file);
			if(f.delete()){
				logger.debug("File " + f.getName() + " deleted");
			}else{
				logger.debug("Error deleting " + f.getName());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
