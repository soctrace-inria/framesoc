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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;

import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;

/**
 * Dialog to manage Framesoc colors.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ManageColorsComposite extends Composite {

	/**
	 * Model entity combo
	 */
	private Combo comboModelEntity;

	/**
	 * Filter text
	 */
	private Text textFilter;

	/**
	 * The viewer
	 */
	private TableViewer tableViewer;

	/**
	 * Entity managed
	 */
	private ModelEntity entity;

	/**
	 * Edit button
	 */
	private Button btnEdit;

	/**
	 * Reset button
	 */
	protected Button btnReset;

	private Dialog parentDialog;

	/**
	 * Color images
	 */
	protected Map<String, Image> images;

	/**
	 * Collection to save the temporary color changes
	 */
	protected Map<ModelEntity, Map<String, FramesocColor>> modifiedColors;

	protected class Entity {
		String name;
		ModelEntity entity;

		public Entity(String name, ModelEntity entity) {
			this.name = name;
			this.entity = entity;
		}
	}

	protected Map<Integer, Entity> entities;
	protected final static String ET_NAME = "Event Types";
	protected final static String EP_NAME = "Event Producers";

	/**
	 * Constructor
	 * 
	 * @param parentShell
	 *            shell
	 */
	public ManageColorsComposite(Composite parentComposite, Dialog parentDialog) {
		super(parentComposite, SWT.NONE);
		this.parentDialog = parentDialog;

		this.images = new HashMap<String, Image>();
		this.entities = new TreeMap<Integer, Entity>();
		this.entities.put(0, new Entity(EP_NAME, ModelEntity.EVENT_PRODUCER));
		this.entities.put(1, new Entity(ET_NAME, ModelEntity.EVENT_TYPE));
		modifiedColors = new HashMap<ModelEntity, Map<String, FramesocColor>>();
	}

	protected void createPartControl() {
		Composite all = this;

		comboModelEntity = new Combo(all, SWT.READ_ONLY);
		comboModelEntity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		Iterator<Entry<Integer, Entity>> it = entities.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Entity> e = it.next();
			comboModelEntity.add(e.getValue().name, e.getKey());
			comboModelEntity.select(e.getKey()); // select the last
			entity = e.getValue().entity;
			modifiedColors.put(entity, new HashMap<String, FramesocColor>());
		}

		comboModelEntity.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				entity = entities.get(comboModelEntity.getSelectionIndex()).entity;
				textFilter.setText("");
				tableViewer.setInput(getNames());
				tableViewer.setSelection(null);
				tableViewer.refresh(true);
			}
		});

		textFilter = new Text(all, SWT.BORDER);
		textFilter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				tableViewer.refresh();
			}
		});
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Composite composite = new Composite(all, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		Composite names = new Composite(composite, SWT.NONE);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		names.setLayout(tableColumnLayout);
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

		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		nameColumn.setLabelProvider(new RowLabelProvider());
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setSorter(new ViewerSorter());
		tableViewer.addFilter(new RowFilter());
		tableViewer.setInput(getNames());

		// Windows fix: Set column width to the full width of the table
		tableColumnLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(100, false));
		
		// buttons
		Composite compositeButtons = new Composite(composite, SWT.NONE);
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
				// Should be only one name in the selection (multiple selection
				// not allowed)
				while (it.hasNext()) {
					String name = (String) it.next();
					ColorDialog dialog = new ColorDialog(parentDialog
							.getShell());
					FramesocColor c;

					// If color was already modified
					if (modifiedColors.get(entity).containsKey(name))
						c = modifiedColors.get(entity).get(name);
					else
						c = getColor(name);

					dialog.setRGB(new RGB(c.red, c.green, c.blue));
					RGB rgb = dialog.open();
					if (rgb == null)
						continue;

					modifiedColors.get(entity).put(name,
							new FramesocColor(rgb.red, rgb.green, rgb.blue));

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
				modifiedColors.get(entity).clear();
				disposeImages();
				btnReset.setEnabled(false);
				tableViewer.refresh(true);
			}
		});
	}

	public void disposeImages() {
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

	private void setColor(String name, FramesocColor color, ModelEntity anEntity) {
		if (anEntity.equals(ModelEntity.EVENT_TYPE))
			FramesocColorManager.getInstance().setEventTypeColor(name, color);
		else
			FramesocColorManager.getInstance().setEventProducerColor(name,
					color);
	}

	/**
	 * Save the colors in the external color configuration file
	 * 
	 * @return true if there was at least one modification, false otherwise
	 */
	public boolean saveColors() {
		boolean hasChanged = saveModifiedColors();
		if (hasChanged) {
			FramesocColorManager.getInstance().saveEventTypeColors();
			FramesocColorManager.getInstance().saveEventProducerColors();
		}
		return hasChanged;
	}

	/**
	 * Save the modifications in memory
	 * 
	 * @return true if there was at least one modification, false otherwise
	 */
	private boolean saveModifiedColors() {
		boolean hasChanged = false;
		for (ModelEntity modelEntity : modifiedColors.keySet()) {
			for (String name : modifiedColors.get(modelEntity).keySet()) {
				setColor(name, modifiedColors.get(modelEntity).get(name),
						modelEntity);
				hasChanged = true;
			}
		}

		return hasChanged;
	}

	protected Collection<String> getNames() {
		if (entity.equals(ModelEntity.EVENT_TYPE))
			return FramesocColorManager.getInstance().getEventTypeNames();
		else
			return FramesocColorManager.getInstance().getEventProducerNames();
	}

	public ModelEntity getEntity() {
		return entity;
	}

	public void setEntity(ModelEntity entity) {
		this.entity = entity;
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
				// If the color was previously modified
				if (modifiedColors.get(entity).containsKey(name)) {
					// Use the modified color
					gc.setBackground(modifiedColors.get(entity).get(name)
							.getSwtColor());
				} else {
					gc.setBackground(getColor(name).getSwtColor());
				}

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
}
