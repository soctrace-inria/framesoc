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
package fr.inria.soctrace.framesoc.ui.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
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
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.DBMS;

/**
 * Eclipse Dialog to configure Framesoc settings
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ConfigurationDialog extends Dialog {

	private Button btnIndexingTime;
	private Configuration config;
	private Button btnIndexingEP;
	private Spinner maxViewInstance;
	private Button btnLaunchDBWizard;
	private Button btnAllowViewReplication;
	private Composite databaseComposite;

	/**
	 * Maximum value allowed for the number of view instances
	 */
	private final static Integer MAX_VIEW_INSTANCES = 100000;

	/**
	 * Minimum value allowed for the number of view instances
	 */
	private final static Integer MIN_VIEW_INSTANCES = -1;

	/**
	 * Incremental step for number of view instances
	 */
	private final static Integer INCREMENT_VIEW_INSTANCES = 1;

	/**
	 * Default value set when 0 is set as value in number of view instances
	 */
	private final static String REPLACE_0_INSTANCE_VALUE = "1";

	/**
	 * Composite for color management
	 */
	private ManageColorsComposite manageColorComposite;

	/**
	 * Composite for tool management
	 */
	private ManageToolsComposite manageToolsComposite;

	Map<Integer, Tool> oldTools;

	public ConfigurationDialog(Shell parentShell) {
		super(parentShell);
		config = Configuration.getInstance();

		// Tool Management
		oldTools = loadTools();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		// global composite
		final Composite global = new Composite(composite, SWT.NONE);
		global.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		global.setLayout(new GridLayout(1, false));
		global.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		// global tab folder
		TabFolder tabFolder = new TabFolder(global, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// Database settings

		// tab item + corresponding composite
		final TabItem tbtmDatabaseParameters = new TabItem(tabFolder, 0);
		tbtmDatabaseParameters.setText("Database");
		Composite compositeDatabaseParameters = new Composite(tabFolder, SWT.NONE);
		tbtmDatabaseParameters.setControl(compositeDatabaseParameters);
		compositeDatabaseParameters.setLayout(new GridLayout(1, false));

		// dbms settings
		final Group groupDatabaseSettings = new Group(compositeDatabaseParameters, SWT.NONE);
		groupDatabaseSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		groupDatabaseSettings.setText("Database Settings");
		groupDatabaseSettings.setLayout(new GridLayout(1, true));

		// dbms name
		final Composite dbmsComposite = new Composite(groupDatabaseSettings, SWT.NONE);
		dbmsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		dbmsComposite.setLayout(new GridLayout(2, true));

		final Label lblSqlCurrentDBMS = new Label(dbmsComposite, SWT.NONE);
		lblSqlCurrentDBMS.setText("Current DBMS:");
		lblSqlCurrentDBMS.setToolTipText("Current Database Management System");

		final Label lblCurrentDBMSName = new Label(dbmsComposite, SWT.NONE);
		lblCurrentDBMSName.setText(config.getDefault(SoCTraceProperty.soctrace_dbms));

		// wizard button
		btnLaunchDBWizard = new Button(groupDatabaseSettings, SWT.PUSH);
		btnLaunchDBWizard.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnLaunchDBWizard.setText("Launch DBMS Configuration");
		btnLaunchDBWizard.setToolTipText("Launch the DBMS configuration wizard");
		btnLaunchDBWizard.addSelectionListener(new LaunchDMBSWizard());

		// composite containing dbms specific content
		databaseComposite = new Composite(groupDatabaseSettings, SWT.NONE);
		databaseComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		loadDBSettings();

		// indexing settings
		final Group groupIndexingSettings = new Group(compositeDatabaseParameters, SWT.NONE);
		groupIndexingSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
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

		// GUI settings

		// tab item + corresponding composite
		final TabItem tbtmGUIParameters = new TabItem(tabFolder, 0);
		tbtmGUIParameters.setText("GUI");
		final Composite compositeGUIParameters = new Composite(tabFolder, SWT.NONE);
		tbtmGUIParameters.setControl(compositeGUIParameters);
		compositeGUIParameters.setLayout(new GridLayout(1, false));

		final Group groupGUISettings = new Group(compositeGUIParameters, SWT.NONE);
		groupGUISettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		groupGUISettings.setText("GUI Settings");
		groupGUISettings.setLayout(new GridLayout(1, false));

		Composite instances = new Composite(groupGUISettings, SWT.NONE);
		instances.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		instances.setLayout(new GridLayout(2, false));

		Label lblMaxViewInstance = new Label(instances, SWT.NONE);
		lblMaxViewInstance.setText("Maximum number of view instances: ");

		maxViewInstance = new Spinner(instances, SWT.BORDER);
		maxViewInstance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		maxViewInstance.setIncrement(INCREMENT_VIEW_INSTANCES);
		maxViewInstance.setMaximum(MAX_VIEW_INSTANCES);
		maxViewInstance.setMinimum(MIN_VIEW_INSTANCES);
		maxViewInstance.setSelection(Integer.valueOf(config
				.get(SoCTraceProperty.max_view_instances)));
		maxViewInstance.setToolTipText("Maximum number of instances of the same view (["
				+ MIN_VIEW_INSTANCES + ", " + MAX_VIEW_INSTANCES + "]; -1 = no limit).");

		btnAllowViewReplication = new Button(groupGUISettings, SWT.CHECK);
		btnAllowViewReplication.setSelection(Boolean.valueOf(config
				.get(SoCTraceProperty.allow_view_replication)));
		btnAllowViewReplication.setText("Allow view replication");
		btnAllowViewReplication
				.setToolTipText("Enable to open several instances of the same view on the same trace");

		// Colors

		// tab item + corresponding composite
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

		// tab item + corresponding composite
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

		// Check if value is 0
		if (!maxViewInstance.getText().equals("0")) {
			config.set(SoCTraceProperty.max_view_instances, maxViewInstance.getText());
		} else {
			// if 0, put a default value instead
			config.set(SoCTraceProperty.max_view_instances, REPLACE_0_INSTANCE_VALUE);
		}
		config.set(SoCTraceProperty.trace_db_ts_indexing,
				String.valueOf(btnIndexingTime.getSelection()));
		config.set(SoCTraceProperty.trace_db_eid_indexing,
				String.valueOf(btnIndexingEP.getSelection()));
		config.set(SoCTraceProperty.allow_view_replication,
				String.valueOf(btnAllowViewReplication.getSelection()));

		// Regenerate the configuration file
		config.saveOnFile();

		// Colors
		boolean hasChanged = manageColorComposite.saveColors();
		if (hasChanged) {
			ColorsChangeDescriptor des = new ColorsChangeDescriptor();
			des.setEntity(manageColorComposite.getEntity());
			FramesocBus.getInstance().send(
					FramesocBusTopic.TOPIC_UI_COLORS_CHANGED, des);
		}
		
		FramesocPartManager.getInstance().updateMaxInstances();

		super.okPressed();
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

	/**
	 * Update the displayed settings for the current DMBS
	 */
	public void loadDBSettings() {
		// Remove the currently displayed interface
		disposeChildren(databaseComposite);

		if (config.get(SoCTraceProperty.soctrace_dbms).equals(DBMS.MYSQL.toString())) {
			new MySQLDialog(databaseComposite, this);
		}

		if (config.get(SoCTraceProperty.soctrace_dbms).equals(DBMS.SQLITE.toString())) {
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
				loadDBSettings();
			}
		}
	}

	/**
	 * Dispose all the children widgets of a given composite
	 * 
	 * @param composite
	 *            the composite whose children are disposed
	 */
	void disposeChildren(Composite composite) {
		for (Control control : composite.getChildren()) {
			control.dispose();
		}
	}
}
