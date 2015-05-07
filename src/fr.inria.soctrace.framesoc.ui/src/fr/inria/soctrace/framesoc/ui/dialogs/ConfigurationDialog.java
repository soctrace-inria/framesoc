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
package fr.inria.soctrace.framesoc.ui.dialogs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.init.Initializer;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.DBMS;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.IdManager.Direction;

/**
 * Eclipse Dialog to configure Framesoc settings
 * 
 * @author youenn
 *
 */
public class ConfigurationDialog extends Dialog {

	private Button btnIndexingTime;
	private Configuration config;
	private Button btnIndexingEP;
	private Spinner maxViewInstance;

	private final Integer MaxViewInstances = 100000;
	private final Integer MinViewInstances = -1;
	private final Integer IncrementViewInstances = 1;

	private Button btnAllowViewReplication;

	private ManageColorsComposite manageColorComposite;
	private ManageToolsComposite manageToolsComposite;

	/**
	 * Tools map, always synchronized with the viewer.
	 */
	private Map<Integer, Tool> toolsMap;

	/**
	 * Installed tool names. Names are unique for tools.
	 */
	private Set<String> oldToolNames;
	Map<Integer, Tool> oldTools;

	/**
	 * For added tools we use temporary negative IDs. Actual ID are assigned by
	 * the Dialog user.
	 */
	private IdManager newToolIdManager;
	private final int TMP_START_ID = -1000;
	private Composite databaseComposite;
	private Button btnLaunchDBWizard;


	public ConfigurationDialog(Shell parentShell) {
		super(parentShell);
		config = Configuration.getInstance();

		// Tool Management
		oldTools = loadTools();
		oldToolNames = new HashSet<String>();
		toolsMap = new HashMap<Integer, Tool>();
		Iterator<Entry<Integer, Tool>> iterator = oldTools.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Tool> pair = iterator.next();
			toolsMap.put(pair.getKey(), pair.getValue());
			oldToolNames.add(pair.getValue().getName());
		}
		newToolIdManager = new IdManager();
		newToolIdManager.setNextId(TMP_START_ID);
		newToolIdManager.setDirection(Direction.DESCENDING);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		final SashForm sashFormGlobal = new SashForm(composite, SWT.VERTICAL);
		sashFormGlobal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		sashFormGlobal.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		TabFolder tabFolder = new TabFolder(sashFormGlobal, SWT.NONE);

		// Database settings
		final TabItem tbtmDatabaseParameters = new TabItem(tabFolder, 0);
		tbtmDatabaseParameters.setText("Database");

		final SashForm sashFormDatabaseParameters = new SashForm(tabFolder,
				SWT.VERTICAL);
		tbtmDatabaseParameters.setControl(sashFormDatabaseParameters);

		final SashForm sashFormIndexing = new SashForm(
				sashFormDatabaseParameters, SWT.VERTICAL);

		final Group groupIndexingSettings = new Group(sashFormIndexing,
				SWT.NONE);
		groupIndexingSettings.setText("Indexing Settings");
		groupIndexingSettings.setLayout(new GridLayout(1, false));

		btnIndexingTime = new Button(groupIndexingSettings, SWT.CHECK);
		btnIndexingTime.setSelection(Boolean.valueOf(config
				.get(SoCTraceProperty.trace_db_ts_indexing)));
		btnIndexingTime.setText("Index traces on time");
		btnIndexingTime.setToolTipText("Imported traces are indexed on time");

		btnIndexingEP = new Button(groupIndexingSettings, SWT.CHECK);
		btnIndexingEP.setSelection(Boolean.valueOf(config
				.get(SoCTraceProperty.trace_db_eid_indexing)));
		btnIndexingEP.setText("Index traces on event ID");
		btnIndexingEP.setToolTipText("Imported traces are indexed on event ID");

		final SashForm sashFormDatabase = new SashForm(
				sashFormDatabaseParameters, SWT.VERTICAL);

		final Group groupDatabaseSettings = new Group(sashFormDatabase,
				SWT.NONE);
		groupDatabaseSettings.setText("Database Settings");
		groupDatabaseSettings.setLayout(new GridLayout(1, true));

		Composite databaseComposite2 = new Composite(groupDatabaseSettings,
				SWT.NONE);
		databaseComposite2.setLayout(new GridLayout(3, true));

		final Label lblSqlCurrentDBMS = new Label(databaseComposite2, SWT.NONE);
		lblSqlCurrentDBMS.setText("Current DBMS:");
		lblSqlCurrentDBMS.setToolTipText("Current DataBase Management System");

		final Label lblCurrentDBMSName = new Label(databaseComposite2, SWT.NONE);
		lblCurrentDBMSName.setText(config
				.getDefault(SoCTraceProperty.soctrace_dbms));

		btnLaunchDBWizard = new Button(groupDatabaseSettings, SWT.PUSH);
		btnLaunchDBWizard.setText("Launch DBMS Configuration");
		btnLaunchDBWizard
				.setToolTipText("Launch the DBMS configuration wizard");
		btnLaunchDBWizard.addSelectionListener(new LaunchDMBSWizard());

		databaseComposite = new Composite(groupDatabaseSettings, SWT.NONE);
		databaseComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		changeDBSettings();

		sashFormDatabaseParameters.setWeights(new int[] { 1, 3 });

		
		// GUI settings
		final TabItem tbtmGUIParameters = new TabItem(tabFolder, 0);
		tbtmGUIParameters.setText("GUI");

		final SashForm sashFormGUIParameters = new SashForm(tabFolder,
				SWT.VERTICAL);
		tbtmGUIParameters.setControl(sashFormGUIParameters);

		final SashForm sashFormViewsParameters = new SashForm(
				sashFormGUIParameters, SWT.VERTICAL);

		final Group groupGUISettings = new Group(sashFormViewsParameters,
				SWT.NONE);
		groupGUISettings.setText("GUI Settings");
		groupGUISettings.setLayout(new GridLayout(2, false));

		Label lblMaxViewInstance = new Label(groupGUISettings, SWT.NONE);
		lblMaxViewInstance.setText("Maximum number of view instances: ");

		maxViewInstance = new Spinner(groupGUISettings, SWT.BORDER);
		maxViewInstance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		maxViewInstance.setIncrement(IncrementViewInstances);
		maxViewInstance.setMaximum(MaxViewInstances);
		maxViewInstance.setMinimum(MinViewInstances);
		maxViewInstance.setSelection(Integer.valueOf(config
				.get(SoCTraceProperty.max_view_instances)));
		maxViewInstance
				.setToolTipText("Maximum number of instances of the same view (["
						+ MinViewInstances
						+ ", "
						+ MaxViewInstances
						+ "]; -1 = no limit).");

		btnAllowViewReplication = new Button(groupGUISettings, SWT.CHECK);
		btnAllowViewReplication.setSelection(Boolean.valueOf(config
				.get(SoCTraceProperty.allow_view_replication)));
		btnAllowViewReplication.setText("Allow view replication");
		btnAllowViewReplication
				.setToolTipText("Enable to open several instances of the same view on the same trace");

		
		// Colors
		final TabItem tbtmColorsParameters = new TabItem(tabFolder, 0);
		tbtmColorsParameters.setText("Colors");
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);

		manageColorComposite = new ManageColorsComposite(tabFolder, this);
		manageColorComposite.setLayout(layout);
		manageColorComposite.setBackground(tabFolder.getBackground());
		manageColorComposite.createPartControl();
		tbtmColorsParameters.setControl(manageColorComposite);

		// Tools
		final TabItem tbtmToolsParameters = new TabItem(tabFolder, 0);
		tbtmToolsParameters.setText("Tools");


		GridLayout layoutTools = new GridLayout();
		layoutTools.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layoutTools.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layoutTools.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layoutTools.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);

		manageToolsComposite = new ManageToolsComposite(tabFolder, oldTools);
		manageToolsComposite.setLayout(layoutTools);
		manageColorComposite.setBackground(tabFolder.getBackground());
		manageToolsComposite.createPartControl();
		tbtmToolsParameters.setControl(manageToolsComposite);
		
		return composite;
	}

	@Override
	protected void okPressed() {
		config.set(SoCTraceProperty.max_view_instances,
				maxViewInstance.getText());
		config.set(SoCTraceProperty.trace_db_ts_indexing,
				String.valueOf(btnIndexingTime.getSelection()));
		config.set(SoCTraceProperty.trace_db_eid_indexing,
				String.valueOf(btnIndexingEP.getSelection()));
		config.set(SoCTraceProperty.allow_view_replication,
				String.valueOf(btnAllowViewReplication.getSelection()));

		// Regenerate the configuration file
		config.saveOnFile();

		// Colors
		manageColorComposite.saveColors();
		ColorsChangeDescriptor des = new ColorsChangeDescriptor();
		des.setEntity(manageColorComposite.getEntity());
		FramesocBus.getInstance().send(
				FramesocBusTopic.TOPIC_UI_COLORS_CHANGED, des);

		FramesocPartManager.getInstance().updateMaxInstances();

		super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		// Colors
		manageColorComposite.loadColors();
		ColorsChangeDescriptor des = new ColorsChangeDescriptor();
		des.setEntity(manageColorComposite.getEntity());
		FramesocBus.getInstance().send(
				FramesocBusTopic.TOPIC_UI_COLORS_CHANGED, des);
		super.cancelPressed();
	}

	@Override
	public boolean close() {
		manageColorComposite.disposeImages();
		return super.close();
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(550, 604);
	}

	/**
	 * Set a customize title for the setting window
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Framesoc Configuration");
	}


	/************************* Manage tools stuff ****************************/

	private Map<Integer, Tool> loadTools() {
		Map<Integer, Tool> toolsMap = new HashMap<Integer, Tool>();
		ITraceSearch searchInterface = null;
		try {
			searchInterface = new TraceSearch().initialize();
			List<Tool> tools = searchInterface.getTools();
			for (Tool t : tools) {
				toolsMap.put(t.getId(), t);
			}
			searchInterface.uninitialize();
		} catch (SoCTraceException e) {
			MessageDialog.openError(getShell(), "Exception", e.getMessage());
		} finally {
			TraceSearch.finalUninitialize(searchInterface);
		}
		return toolsMap;
	}

	public ManageToolsComposite getManageToolsComposite() {
		return manageToolsComposite;
	}

	public void setManageToolsComposite(ManageToolsComposite manageToolsComposite) {
		this.manageToolsComposite = manageToolsComposite;
	}

	public Map<Integer, Tool> getOldTools() {
		return oldTools;
	}

	public void setOldTools(Map<Integer, Tool> oldTools) {
		this.oldTools = oldTools;
	}

	/************************* Database stuff ****************************/

	public void changeDBSettings() {
		// Remove the currently displayed interface
		disposeChildren(databaseComposite);

		if (config.get(SoCTraceProperty.soctrace_dbms).equals(
				DBMS.MYSQL.toString())) {
			new MySQLDialog(databaseComposite, this);
		}

		if (config.get(SoCTraceProperty.soctrace_dbms).equals(
				DBMS.SQLITE.toString())) {
			new SQLiteDialog(databaseComposite, this);
		}

		// Update the DB view
		databaseComposite.layout();
		databaseComposite.update();
	}

	private class LaunchDMBSWizard extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (Initializer.INSTANCE.initializeSystem(getShell(), false)) {
				Initializer.INSTANCE.manageTools(getShell());
				changeDBSettings();
			}
		}
	}

	void disposeChildren(Composite composite) {
		for (Control control : composite.getChildren()) {
			control.dispose();
		}
	}

}
