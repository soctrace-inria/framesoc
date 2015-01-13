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
package fr.inria.soctrace.tools.framesoc.exporter.dbimporter;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.tools.management.PluginImporterJob;
import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.core.tools.model.IPluginToolJobBody;
import fr.inria.soctrace.framesoc.core.tools.model.FileInput;
import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.AnalysisResultQuery;
import fr.inria.soctrace.lib.query.ToolQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.dbmanager.DBManager;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.tools.framesoc.exporter.utils.ExportMetadata;
import fr.inria.soctrace.tools.framesoc.exporter.utils.ExporterConstants;
import fr.inria.soctrace.tools.framesoc.exporter.utils.Serializer;

/**
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocDBImporter extends FramesocTool {

	private final static Logger logger = LoggerFactory.getLogger(FramesocDBImporter.class);

	/**
	 * Plugin Tool Job body: we use a Job since we have to perform a long operation and we don't
	 * want to freeze the UI.
	 */
	private class FramesocDBImporterJobBody implements IPluginToolJobBody {

		private String args[]; // TODO use input with the new mechanism
		private String dbFile = "";
		private String metaFile = "";
		private ExportMetadata metadata = null;

		private boolean hasMeta() {
			return !metaFile.equals("");
		}

		public FramesocDBImporterJobBody(IFramesocToolInput input) {
			List<String> files= ((FileInput) input).getTraceFiles();
			this.args = files.toArray(new String[files.size()]);
		}

		private Trace prepareUnknownTrace(SystemDBObject sysDB) throws SoCTraceException {
			Trace t = new Trace(0); // the actual id is chosen at save time
			TraceType tt = new TraceType(0); // the actual id is chosen at save time
			tt.setName(ExporterConstants.UNKNOWN_TYPE_NAME);
			TraceType type = tt;
			t.setType(type);
			return t;
		}

		private void saveTrace(SystemDBObject sysDB) throws SoCTraceException {

			boolean isTypePresent = sysDB.isTraceTypePresent(metadata.trace.getType().getName());

			// trace
			Trace t = new Trace(sysDB.getNewId(FramesocTable.TRACE.toString(), "ID"));
			t.copyMetadata(metadata.trace);

			// type
			if (isTypePresent) {
				t.setType(sysDB.getTraceType(metadata.trace.getType().getName()));
			} else {
				TraceType tt = new TraceType(sysDB.getNewId(FramesocTable.TRACE_TYPE.toString(),
						"ID"));
				tt.setName(metadata.trace.getType().getName());
				t.setType(tt);
				IdManager idm = new IdManager();
				idm.setNextId(sysDB.getMaxId(FramesocTable.TRACE_PARAM_TYPE.toString(), "ID") + 1);
				for (TraceParamType tpt : metadata.trace.getType().getTraceParamTypes()) {
					TraceParamType ntpt = new TraceParamType(idm.getNextId());
					ntpt.setTraceType(t.getType());
					ntpt.setName(tpt.getName());
					ntpt.setType(tpt.getType());
				}
			}

			// trace param
			Map<String, TraceParam> tpm = metadata.trace.getParamMap();
			IdManager idm = new IdManager();
			idm.setNextId(sysDB.getMaxId(FramesocTable.TRACE_PARAM.toString(), "ID") + 1);
			for (TraceParamType tpt : t.getType().getTraceParamTypes()) {
				TraceParam tp = new TraceParam(idm.getNextId());
				if (tpm.get(tpt.getName()) == null) {
					logger.debug("TraceParam for TraceParamType " + tpt.getName() + " not present");
					continue;
				}
				tp.setTrace(t);
				tp.setTraceParamType(tpt);
				tp.setValue(tpm.get(tpt.getName()).getValue());
			}

			// save
			sysDB.save(t);
			for (TraceParam tp : t.getParams()) {
				sysDB.save(tp);
			}
			if (isTypePresent)
				return;
			sysDB.save(t.getType());
			for (TraceParamType tpt : t.getType().getTraceParamTypes()) {
				sysDB.save(tpt);
			}
		}

		@Override
		public void run(IProgressMonitor monitor) throws SoCTraceException {

			SystemDBObject sysDB = null;
			TraceDBObject traceDB = null;

			try {
				parseArgs(args);

				sysDB = SystemDBObject.openNewIstance();
				if (hasMeta()) {
					Serializer serializer = new Serializer();
					metadata = serializer.deserialize(metaFile);
				} else {
					// display to the user that the trace as no metadata
					// so trace metadata should be added manually
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							IWorkbenchWindow window = PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow();
							MessageDialog.openWarning(window.getShell(), "Warning",
									"No *.meta file found. The trace will be imported with default metadata");
						}
					});
					metadata = new ExportMetadata();
					metadata.trace = prepareUnknownTrace(sysDB);
				}
				metadata.trace.setDbName(FramesocManager.getInstance().getTraceDBName(
						"FramesocDBImporter"));

				if (monitor.isCanceled())
					return;

				// import trace as is, considering the DB name
				final DBManager dbm = DBManager.getDBManager(metadata.trace.getDbName());

				Thread th = new Thread() {
					@Override
					public void run() {
						logger.debug("thread body");
						try {
							try {
								dbm.importDB(dbFile);
								logger.debug("import done");
							} catch (Exception e) {
								logger.debug("dropping DB");
								dbm.dropDB();
								logger.debug("DB dropped");
							}
						} catch (SoCTraceException e) {
							e.printStackTrace();
						}
					}
				};

				th.start();

				while (th.isAlive()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						logger.debug("interrupted exception in main thread");
						th.interrupt();
						return;
					}
					if (monitor.isCanceled()) {
						logger.debug("user pressed cancel");
						logger.debug("interrupting thread");
						th.interrupt();
						return;
					}
				}

				// manage results, if any
				traceDB = TraceDBObject.openNewIstance(metadata.trace.getDbName());
				AnalysisResultQuery arq = new AnalysisResultQuery(traceDB);
				List<AnalysisResult> arl = arq.getList();
				if (arl.size() > 0) {
					// there are results
					if (!hasMeta() || metadata.tools == null) {
						// there are not metadata or no tool information, clean results
						for (AnalysisResult ar : arl)
							traceDB.delete(ar);
					} else {
						// there are metadata and tool information
						// see if matching tools
						ToolQuery tq = new ToolQuery(sysDB);
						List<Tool> tl = tq.getList();
						// tool name -> tool
						Map<String, Tool> name2tool = new HashMap<String, Tool>();
						Map<Integer, Tool> id2tool = new HashMap<Integer, Tool>();
						for (Tool t : tl) {
							name2tool.put(t.getName(), t);
							id2tool.put(t.getId(), t);
						}
						// old system tool id -> new system tool id
						Map<Integer, Integer> oldId2newId = new HashMap<Integer, Integer>();
						final Set<Integer> arIdsToKeep = new HashSet<Integer>();
						for (Tool t : metadata.tools) {
							if (name2tool.containsKey(t.getName())) {
								oldId2newId.put(t.getId(), name2tool.get(t.getName()).getId());
							}
						}
						// analysis result with matching tool
						final List<AnalysisResult> arToShow = new LinkedList<AnalysisResult>();
						Map<Integer, Integer> map = getOldToolIdMapping(traceDB);
						for (AnalysisResult ar : arl) {
							int oldToolId = map.get(ar.getId());
							if (oldId2newId.containsKey(oldToolId)) {
								// update the tool to match the new system one
								ar.setTool(id2tool.get(oldId2newId.get(oldToolId)));
								arToShow.add(ar);
							}
						}

						// display user dialog
						if (arToShow.size() > 0) {
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									IWorkbenchWindow window = PlatformUI.getWorkbench()
											.getActiveWorkbenchWindow();
									ImportDBResultDialog dlg = new ImportDBResultDialog(window
											.getShell(), arToShow);
									dlg.open(); // there's only the Import button
									Object[] keep = dlg.getResultsToKeep();
									// results to clean ids
									for (Object o : keep) {
										arIdsToKeep.add(((AnalysisResult) o).getId());
									}
								}
							});
						}
						for (AnalysisResult ar : arl) {
							if (!arIdsToKeep.contains(ar.getId()))
								traceDB.delete(ar);
							else
								traceDB.update(ar);
						}
					}
				}

				// close and commit
				saveTrace(sysDB);
				feedback(true, "");
			} catch (SoCTraceException e) {
				feedback(false, e.toString());
				if (sysDB != null) {
					sysDB.rollback();
					sysDB.close();
				}
			} finally {
				DBObject.finalClose(traceDB);
				DBObject.finalClose(sysDB);
			}
		}

		private void feedback(final boolean ok, final String s) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if (ok)
						MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
								"Importer", "Database correctly imported.\n" + s);
					else
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Importer",
								"Error while importing.\n" + s);
				}
			});
		}

		private Map<Integer, Integer> getOldToolIdMapping(TraceDBObject traceDB)
				throws SoCTraceException {
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			try {
				Statement stm = traceDB.getConnection().createStatement();
				ResultSet rs = stm.executeQuery("SELECT ID, TOOL_ID FROM "
						+ FramesocTable.ANALYSIS_RESULT);
				while (rs.next()) {
					map.put(rs.getInt(1), rs.getInt(2));
				}
				stm.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return map;
		}

		/**
		 * Parse the arguments checking them. After that dbFile and metaFile contain verified
		 * values.
		 * 
		 * @param args
		 *            arguments
		 * @throws SoCTraceException
		 */
		private void parseArgs(String[] args) throws SoCTraceException {
			dbFile = "";
			metaFile = "";

			for (String s : args) {
				if (s.endsWith(".db")) {
					dbFile = s;
					break;
				}
			}

			// check that dbFile has been specified
			if (dbFile.equals(""))
				throw new SoCTraceException("No *.db file specified.");

			// check that dbFile exists
			final File db = new File(dbFile);
			if (!db.exists())
				throw new SoCTraceException(dbFile + " not found.");

			// look for a matching meta file
			File[] metas = db.getParentFile().listFiles(new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.equals(FilenameUtils.removeExtension(db.getName()) + ".meta");
				}
			});
			if (metas.length > 0)
				metaFile = metas[0].getAbsolutePath();
		}

	}

	@Override
	public void launch(IFramesocToolInput input) {
		PluginImporterJob job = new PluginImporterJob("Framesoc DB Importer",
				new FramesocDBImporterJobBody(input));
		job.setUser(true);
		job.schedule();
	}

	@Override
	public ParameterCheckStatus canLaunch(IFramesocToolInput input) {

		ParameterCheckStatus status = new ParameterCheckStatus(true, "");

		List<String> files= ((FileInput) input).getTraceFiles();
		
		if (files.size() < 1) {
			status.message = "Missing *.db file.";
			status.valid = false;
			return status;
		}

		String file = files.iterator().next();

		if (!file.endsWith(".db")) {
			status.valid = false;
			status.message = "The provided file does not end with '.db'.";
			return status;
		}

		File f = new File(file);
		if (!f.exists()) {
			status.valid = false;
			status.message = "The provided file does not exist.";
			return status;			
		}

		return status;
	}

}
