/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Youenn Corre - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.eventtable.view;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocConstants;
import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableColumn;
import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableRow;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * This class implements the generation of a CSV file from the currently
 * displayed events in the table event view
 * 
 * Event custom parameters are added on the fly at the end of the CSV header.
 * Custom parameters with the same name are added into the same column
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class CSVExport {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(CSVExport.class);

	private final Object exportSyncObj = new Object();
	private StringBuilder csvExport = new StringBuilder();
	private StringBuilder csvHeader = new StringBuilder();
	private Map<EventTableColumn, Boolean> exportColumn = new HashMap<EventTableColumn, Boolean>();
	private int currentMaxNumberOfParameter = 0;
	private Map<String, Integer> parameterTypes = new HashMap<String, Integer>();
	private String filePath;
	private boolean stop = false;
	private int monitorCheck = 20000;
	private EventTableCache cache;

	public CSVExport(String filePath,
			Map<EventTableColumn, Boolean> exportColumn, EventTableCache cache) {
		this.filePath = filePath;
		this.exportColumn = exportColumn;
		this.cache = cache;
	}

	/**
	 * Export the events currently displayed in the table to csv format
	 * 
	 * @param filePath
	 *            name of the file in which the csv export will be written
	 * @throws SoCTraceException
	 */
	public void exportToCSV() {
		final String title = "Exporting to CSV";
		final Job job = new Job(title) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				PrintWriter writer = null;
				int countEvents = 0;
				boolean exportParameters = exportColumn
						.get(EventTableColumn.PARAMS);

				monitor.beginTask(title, cache.getIndexedRowCount()
						/ monitorCheck + 1);

				// Get columns selected for export in the same order that
				// they are displayed
				List<EventTableColumn> selectedColumns = new ArrayList<EventTableColumn>();
				for (EventTableColumn column : EventTableColumn.values())
					// Ignore Param column as it is a special case
					if (exportColumn.get(column) && (column != EventTableColumn.PARAMS))
						selectedColumns.add(column);

				try {
					writer = new PrintWriter(filePath,
							System.getProperty("file.encoding"));

					for (EventTableColumn column : selectedColumns) {
							csvHeader.append(column.toString()
									+ FramesocConstants.CSV_SEPARATOR);
					}

					// Delete last CSV_SEPARATOR
					if (!exportParameters)
						csvHeader.delete(csvHeader.length()
								- FramesocConstants.CSV_SEPARATOR.length(),
								csvHeader.length());

					String newLine = System.getProperty("line.separator");

					for (int i = 0; i < cache.getIndexedRowCount(); i++) {
						EventTableRow currentRow = cache.get(i);

						for (EventTableColumn column : selectedColumns) {
							csvExport.append(currentRow.get(column)
									+ FramesocConstants.CSV_SEPARATOR);
						}

						if (exportParameters)
							handleParameters(currentRow);
						else {
							// remove last CSV_SEPARATOR
							if (!selectedColumns.isEmpty())
								csvExport
										.delete(csvExport.length()
												- FramesocConstants.CSV_SEPARATOR
														.length(),
												csvExport.length());
						}

						// New line
						csvExport.append(newLine);

						countEvents++;
						if (countEvents % monitorCheck == 0) {
							monitor.worked(1);
							if (monitor.isCanceled() || stop) {
								writer.flush();
								writer.close();
								return Status.CANCEL_STATUS;
							}
						}
					}
					csvHeader.append(newLine);

					// Write data into the file
					writer.write(csvHeader.toString());
					writer.write(csvExport.toString());
					monitor.worked(1);
					monitor.done();
					return Status.OK_STATUS;
				} catch (FileNotFoundException e) {
					MessageDialog.openError(Display.getDefault()
							.getActiveShell(), "Exception", "File " + filePath
							+ " could not be created (" + e.getMessage() + ")");
				} catch (UnsupportedEncodingException e) {
					MessageDialog.openError(
							Display.getDefault().getActiveShell(),
							"Exception",
							"Unsupported encoding "
									+ System.getProperty("file.encoding")
									+ " (" + e.getMessage() + ")");
				} catch (SoCTraceException e) {
					MessageDialog.openError(Display.getDefault()
							.getActiveShell(), "Exception", e.getMessage());
				} finally {
					if (writer != null) {
						// Close the fd
						writer.flush();
						writer.close();
					}

					synchronized (exportSyncObj) {
						csvExport = null;
					}
				}
				return Status.CANCEL_STATUS;
			}
		};

		job.setUser(true);
		job.schedule();
	}

	private void handleParameters(EventTableRow currentRow)
			throws SoCTraceException {
		String tmpParameters = currentRow.get(EventTableColumn.PARAMS);
		// Keep the parameter value
		Map<Integer, String> currentParameterValue = new HashMap<Integer, String>();

		String[] parameters = tmpParameters
				.split(EventTableRow.PARAMETER_SEPARATOR);

		// Parse parameter
		for (int i = 0; i < parameters.length; i++) {
			String[] aParameter = parameters[i]
					.split(EventTableRow.PARAMETER_VALUE_SEPARATOR);

			if (aParameter.length < 2) {
				logger.error("Incorrect element encountered during the parsing of parameters: "
						+ parameters[i]);
				continue;
			}

			// Remove white space
			String paramType = aParameter[0].trim();

			// If we have not yet met this type of parameter
			if (!parameterTypes.containsKey(paramType)) {
				parameterTypes.put(paramType, currentMaxNumberOfParameter);
				if (currentMaxNumberOfParameter == 0)
					// Extend the header with it
					csvHeader.append(paramType);
				else
					// Extend the header with it
					csvHeader.append(FramesocConstants.CSV_SEPARATOR
							+ paramType);
				
				currentMaxNumberOfParameter++;
			}

			// Remove white space and quote
			String paramValue = aParameter[1].trim();
			// Check that the parameter ends with a quote
			if (paramValue.endsWith(EventTableRow.PARAMETER_VALUE_ESCAPE))
				paramValue = paramValue.substring(1, paramValue.length() - 1);
			else {
				// The param value contained at least one
				// PARAMETER_SEPARATOR
				paramValue = paramValue.substring(1, paramValue.length());
				while (true) {
					if (i + 1 < parameters.length) {
						String nextValue = parameters[i + 1].trim();
						// Are we at the end of the parameter
						if (nextValue
								.endsWith(EventTableRow.PARAMETER_VALUE_ESCAPE)) {
							paramValue = paramValue
									+ nextValue.substring(0,
											nextValue.length() - 1);
							i++;
							break;
						} else {
							paramValue = paramValue + parameters[i + 1];
						}
						i++;
					} else {
						break;
					}
				}
			}

			currentParameterValue
					.put(parameterTypes.get(paramType), paramValue);
		}

		// Complete csv with existing parameter values or blank otherwise
		for (int i = 0; i < currentMaxNumberOfParameter; i++) {
			if (i != 0)
				csvExport.append(FramesocConstants.CSV_SEPARATOR);
			if (currentParameterValue.containsKey(i)) {
				csvExport.append(currentParameterValue.get(i));
			}
		}
	}

	/**
	 * Cancel the export.
	 */
	public void cancel() {
		stop = true;
	}

}
