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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.init.Initializer;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;
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
	private Combo comboModelEntity;
	private Text textFilter;
	private TableViewer tableViewer;
	private Button btnEdit;
	private Button btnReset;
	private Button btnEditTools;
	private Button btnRemove;

	private String mysqlUserName;
	private String mysqlPassWord;
	private String mysqlURL;

	private String sqliteDBDirectory;

	/**
	 * Color images
	 */
	protected Map<String, Image> images;

	/**
	 * Entity managed
	 */
	private ModelEntity entity;

	protected class Entity {
		String name;
		ModelEntity entity;

		public Entity(String name, ModelEntity entity) {
			this.name = name;
			this.entity = entity;
		}
	}

	protected Map<Integer, Entity> entities;
	private ListViewer listViewer;

	/**
	 * Tools map, always synchronized with the viewer.
	 */
	private Map<Integer, Tool> toolsMap;

	/**
	 * Installed tool names. Names are unique for tools.
	 */
	private Set<String> oldToolNames;
	Map<Integer, Tool> oldTools = loadTools();

	/**
	 * For added tools we use temporary negative IDs. Actual ID are assigned by
	 * the Dialog user.
	 */
	private IdManager newToolIdManager;
	private final int TMP_START_ID = -1000;
	private Composite databaseComposite;
	private Button btnLaunchDBWizard;

	protected final static String ET_NAME = "Event Types";
	protected final static String EP_NAME = "Event Producers";

	public ConfigurationDialog(Shell parentShell) {
		super(parentShell);
		config = Configuration.getInstance();

		// Color Management
		this.images = new HashMap<String, Image>();
		this.entities = new TreeMap<Integer, Entity>();
		this.entities.put(0, new Entity(EP_NAME, ModelEntity.EVENT_PRODUCER));
		this.entities.put(1, new Entity(ET_NAME, ModelEntity.EVENT_TYPE));

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
		lblMaxViewInstance.setText("Maximum number of view instances");

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

		final SashForm sashFormColorsParameters = new SashForm(tabFolder,
				SWT.VERTICAL);
		tbtmColorsParameters.setControl(sashFormColorsParameters);

		comboModelEntity = new Combo(sashFormColorsParameters, SWT.READ_ONLY);
		comboModelEntity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		Iterator<Entry<Integer, Entity>> it = entities.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Entity> e = it.next();
			comboModelEntity.add(e.getValue().name, e.getKey());
			comboModelEntity.select(e.getKey()); // select the last
			entity = e.getValue().entity;
		}
		comboModelEntity.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				entity = entities.get(comboModelEntity.getSelectionIndex()).entity;
				textFilter.setText("");
				tableViewer.setInput(getNames());
				tableViewer.setSelection(null);
				tableViewer.refresh(true);
				textFilter.setText("");
			}
		});

		textFilter = new Text(sashFormColorsParameters, SWT.BORDER);
		textFilter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				tableViewer.refresh();
			}
		});
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Composite compositeColor = new Composite(sashFormColorsParameters,
				SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		compositeColor.setLayout(gl_composite);
		compositeColor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));

		Composite names = new Composite(compositeColor, SWT.NONE);
		names.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		GridLayout gl_names = new GridLayout(1, false);
		gl_names.horizontalSpacing = 0;
		gl_names.marginHeight = 0;
		gl_names.marginWidth = 0;
		gl_names.verticalSpacing = 0;
		names.setLayout(gl_names);
		names.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// list
		tableViewer = new TableViewer(names, SWT.BORDER | SWT.V_SCROLL
				| SWT.SINGLE);
		Table table = tableViewer.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table.widthHint = 422;
		table.setLayoutData(gd_table);
		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) tableViewer
								.getSelection();
						if (selection.size() <= 0) {
							btnEdit.setEnabled(false);
						}
						if (selection.size() == 1) {
							btnEdit.setEnabled(true);
						} else {
							btnEdit.setEnabled(false);
						}
					}
				});

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(new RowLabelProvider());
		tableViewer.setSorter(new ViewerSorter());
		tableViewer.addFilter(new RowFilter());
		tableViewer.setInput(getNames());

		// buttons
		Composite compositeButtons = new Composite(compositeColor, SWT.NONE);
		compositeButtons.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
				true, true, 1, 1));
		compositeButtons.setLayout(new GridLayout(1, false));
		compositeButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));

		btnEdit = new Button(compositeButtons, SWT.NONE);
		btnEdit.setEnabled(false);
		btnEdit.setToolTipText("Edit Color");
		btnEdit.setImage(ResourceManager.getPluginImage(
				"fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				Iterator<?> it = selection.iterator();
				while (it.hasNext()) {
					String name = (String) it.next();
					ColorDialog dialog = new ColorDialog(getShell());
					FramesocColor c = getColor(name);
					dialog.setRGB(new RGB(c.red, c.green, c.blue));
					RGB rgb = dialog.open();
					if (rgb == null)
						continue;
					setColor(name, new FramesocColor(rgb.red, rgb.green,
							rgb.blue));
					disposeImages();
					btnReset.setEnabled(true);
					tableViewer.refresh(true);
				}
			}
		});

		btnReset = new Button(compositeButtons, SWT.NONE);
		btnReset.setEnabled(false);
		btnReset.setToolTipText("Reload from Configuration File");
		btnReset.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID,
				"icons/load.png"));
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadColors();
				disposeImages();
				btnReset.setEnabled(false);
				tableViewer.refresh(true);
			}
		});

		sashFormColorsParameters.setWeights(new int[] { 1, 1, 10 });

		// Tools
		final TabItem tbtmToolsParameters = new TabItem(tabFolder, 0);
		tbtmToolsParameters.setText("Tools");

		final SashForm sashFormToolsParameters = new SashForm(tabFolder,
				SWT.HORIZONTAL);
		tbtmToolsParameters.setControl(sashFormToolsParameters);

		// list
		listViewer = new ListViewer(sashFormToolsParameters, SWT.BORDER
				| SWT.V_SCROLL | SWT.MULTI);
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				if (selection.size() > 1) {
					btnEditTools.setEnabled(false);
					btnRemove.setEnabled(false);
					return;
				}
				Tool tool = (Tool) selection.getFirstElement();
				if (tool == null)
					return;
				if (tool.isPlugin()) {
					btnEditTools.setEnabled(false);
					btnRemove.setEnabled(false);
				} else {
					btnEditTools.setEnabled(true);
					btnRemove.setEnabled(true);
				}
			}
		});
		org.eclipse.swt.widgets.List list = listViewer.getList();
		GridData gd_list = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_list.widthHint = 472;
		list.setLayoutData(gd_list);
		listViewer.setContentProvider(ArrayContentProvider.getInstance());
		listViewer.setInput(toolsMap.values());
		listViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				Tool t = (Tool) element;
				return (oldToolNames.contains(t.getName()) ? "" : "*")
						+ t.getName();
			}
			// TODO: different icon for plugins and bin
		});
		listViewer.setSorter(new ViewerSorter());

		// buttons
		Composite composite_1 = new Composite(sashFormToolsParameters, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));

		Button btnAdd = new Button(composite_1, SWT.NONE);
		btnAdd.setBounds(0, 0, 92, 33);
		btnAdd.setText("Add");
		btnAdd.setToolTipText("Add an external tool to the system");
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EditToolDialog dialog = new EditToolDialog(getShell());
				if (dialog.open() != Window.OK)
					return;
				Tool tool = new Tool(newToolIdManager.getNextId());
				tool.setCommand(dialog.getToolCommand());
				tool.setName(dialog.getToolName());
				tool.setType(dialog.getToolType());
				tool.setDoc(dialog.getToolDoc());
				toolsMap.put(tool.getId(), tool);
				listViewer.refresh(false);
			}
		});

		btnEditTools = new Button(composite_1, SWT.NONE);
		btnEditTools.setBounds(0, 50, 92, 33);
		btnEditTools.setText("Edit");
		btnEditTools.setToolTipText("Edit external tool details");
		btnEditTools.setEnabled(false);
		btnEditTools.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				Tool tool = (Tool) selection.getFirstElement();
				// show a dialog to edit the tool
				EditToolDialog dialog = new EditToolDialog(getShell(), tool
						.getName(), tool.getCommand(), tool.getType(), tool
						.getDoc());
				if (dialog.open() != Window.OK)
					return;
				tool.setCommand(dialog.getToolCommand());
				tool.setName(dialog.getToolName());
				tool.setType(dialog.getToolType());
				tool.setDoc(dialog.getToolDoc());
				toolsMap.put(tool.getId(), tool);
				listViewer.refresh(false);
			}
		});

		btnRemove = new Button(composite_1, SWT.NONE);
		btnRemove.setBounds(0, 100, 92, 33);
		btnRemove.setText("Remove");
		btnRemove.setToolTipText("Remove external tool");
		btnRemove.setEnabled(false);
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				Iterator<?> it = selection.iterator();
				while (it.hasNext()) {
					toolsMap.remove(((Tool) it.next()).getId());
				}
				listViewer.refresh(false);
			}
		});

		sashFormToolsParameters.setWeights(new int[] { 4, 1 });

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
		saveColors();
		ColorsChangeDescriptor des = new ColorsChangeDescriptor();
		des.setEntity(entity);
		FramesocBus.getInstance().send(
				FramesocBusTopic.TOPIC_UI_COLORS_CHANGED, des);

		FramesocPartManager.getInstance().updateMaxInstances();

		super.okPressed();
	}

	@Override
	protected void cancelPressed() {

		// Colors
		loadColors();
		ColorsChangeDescriptor des = new ColorsChangeDescriptor();
		des.setEntity(entity);
		FramesocBus.getInstance().send(
				FramesocBusTopic.TOPIC_UI_COLORS_CHANGED, des);
		super.cancelPressed();
	}

	@Override
	public boolean close() {
		disposeImages();
		return super.close();
	}

	/**
	 * Set a customize title for the setting window
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Framesoc Configuration");
	}

	/************************* Manage colors stuff ****************************/

	private void disposeImages() {
		for (Image img : images.values()) {
			img.dispose();
		}
		images.clear();
	}

	protected FramesocColor getColor(String name) {
		if (entity.equals(ModelEntity.EVENT_TYPE))
			return FramesocColorManager.getInstance().getEventTypeColor(name);
		else
			return FramesocColorManager.getInstance().getEventProducerColor(
					name);
	}

	private void setColor(String name, FramesocColor color) {
		if (entity.equals(ModelEntity.EVENT_TYPE))
			FramesocColorManager.getInstance().setEventTypeColor(name, color);
		else
			FramesocColorManager.getInstance().setEventProducerColor(name,
					color);
	}

	private void saveColors() {
		if (entity.equals(ModelEntity.EVENT_TYPE))
			FramesocColorManager.getInstance().saveEventTypeColors();
		else
			FramesocColorManager.getInstance().saveEventProducerColors();
	}

	private void loadColors() {
		if (entity.equals(ModelEntity.EVENT_TYPE))
			FramesocColorManager.getInstance().loadEventTypeColors();
		else
			FramesocColorManager.getInstance().loadEventProducerColors();
	}

	protected Collection<String> getNames() {
		if (entity.equals(ModelEntity.EVENT_TYPE))
			return FramesocColorManager.getInstance().getEventTypeNames();
		else
			return FramesocColorManager.getInstance().getEventProducerNames();
	}

	@Override
	protected Point getInitialSize() {
		return new Point(504, 464);
	}

	public class RowLabelProvider extends OwnerDrawLabelProvider {
		@Override
		protected void paint(Event event, Object element) {
			String name = (String) element;
			Rectangle bounds = ((TableItem) event.item).getBounds(event.index);
			Image img = null;
			if (images.containsKey(name)) {
				img = images.get(name);
			} else {
				img = new Image(event.display, bounds.height / 2,
						bounds.height / 2);
				GC gc = new GC(img);
				Color border = new Color(event.display, 0, 0, 0);
				gc.setBackground(border);
				gc.fillRectangle(0, 0, bounds.height / 2, bounds.height / 2);
				gc.setBackground(getColor(name).getSwtColor());
				gc.fillRectangle(1, 1, bounds.height / 2 - 2,
						bounds.height / 2 - 2);
				gc.dispose();
				border.dispose();
				images.put(name, img);
			}

			// center image and text on y
			bounds.height = bounds.height / 2 - img.getBounds().height / 2;
			int imgy = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;
			int texty = bounds.y + 3;
			event.gc.drawText(name, bounds.x + img.getBounds().width + 5,
					texty, true);
			event.gc.drawImage(img, bounds.x, imgy);
		}

		@Override
		protected void measure(Event event, Object element) {
			// nothing to do
		}
	}

	public class RowFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			String row = (String) element;
			if (textFilter.getText().equals(""))
				return true;
			try {
				if (row.matches(".*" + textFilter.getText() + ".*")) {
					return true;
				}
			} catch (PatternSyntaxException e) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),
						"Wrong search string",
						"The expression used as search string is not valid: "
								+ textFilter.getText());
				textFilter.setText("");
			}
			return false;
		}
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

	/**
	 * Get the updated list of tools.
	 * 
	 * @return the updated list of tools
	 */
	public Map<Integer, Tool> getNewTools() {
		return toolsMap;
	}

	public Map<Integer, Tool> getOldTools() {
		return oldTools;
	}

	public void setOldTools(Map<Integer, Tool> oldTools) {
		this.oldTools = oldTools;
	}

	/************************* Database stuff ****************************/

	public String getMysqlUserName() {
		return mysqlUserName;
	}

	public void setMysqlUserName(String mysqlUserName) {
		this.mysqlUserName = mysqlUserName;
	}

	public String getMysqlPassWord() {
		return mysqlPassWord;
	}

	public void setMysqlPassWord(String mysqlPassWord) {
		this.mysqlPassWord = mysqlPassWord;
	}

	public String getMysqlURL() {
		return mysqlURL;
	}

	public void setMysqlURL(String mysqlURL) {
		this.mysqlURL = mysqlURL;
	}

	public String getSqliteDBDirectory() {
		return sqliteDBDirectory;
	}

	public void setSqliteDBDirectory(String sqliteDBDirectory) {
		this.sqliteDBDirectory = sqliteDBDirectory;
	}

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

	/**
	 * Initialize the local values for DBMS settings with the current ones
	 */
	void initLocalDBValues() {
		mysqlPassWord = config.get(SoCTraceProperty.mysql_db_password);
		mysqlURL = config.get(SoCTraceProperty.mysql_base_db_jdbc_url);
		mysqlUserName = config.get(SoCTraceProperty.mysql_db_user);

		sqliteDBDirectory = config.get(SoCTraceProperty.sqlite_db_directory);
	}

}
