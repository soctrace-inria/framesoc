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
package fr.inria.soctrace.framesoc.ui.dialogs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.lib.utils.IdManager.Direction;

/**
 * Dialog to manage Framesoc tools:
 * <ul>
 * <li>Browse, Add, Edit, Remove non plugin tool.
 * <li>Browse plugin tools.
 * </ul>
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ManageToolsDialog extends Dialog {

	/**
	 * Tools map, always synchronized with the viewer.
	 */
	private Map<Integer, Tool> toolsMap;
	
	/**
	 * Installed tool names. Names are unique for tools.
	 */
	private Set<String> oldToolNames;
	
	/**
	 * The viewer
	 */
	private ListViewer listViewer;
	
	/**
	 * For added tools we use temporary negative IDs.
	 * Actual ID are assigned by the Dialog user.
	 */
	private IdManager newToolIdManager;
	private final int TMP_START_ID = -1000;

	/**
	 * Edit button. Disabled when plugin tools are selected.
	 */
	private Button btnEdit;

	private Button btnRemove;
	
	/**
	 * Constructor
	 * @param parentShell shell
	 * @param tools tools map: it will be copied
	 */
	public ManageToolsDialog(Shell parentShell, Map<Integer, Tool> tools) {
		super(parentShell);
		oldToolNames = new HashSet<String>();
		toolsMap = new HashMap<Integer, Tool>();
		Iterator<Entry<Integer, Tool>> iterator = tools.entrySet().iterator();
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
        GridLayout gridLayout = (GridLayout) composite.getLayout();
        gridLayout.numColumns = 2;
        
        // list
        listViewer = new ListViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
        listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        	public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)listViewer.getSelection();
				if (selection.size()>1) {
					btnEdit.setEnabled(false);
					btnRemove.setEnabled(false);
					return;
				}
				Tool tool = (Tool)selection.getFirstElement();
				if (tool==null)
					return;
				if (tool.isPlugin()) {
					btnEdit.setEnabled(false);
					btnRemove.setEnabled(false);
				} else {
					btnEdit.setEnabled(true);
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
        		return (oldToolNames.contains(t.getName())?"":"*") + t.getName();
        	}
        	// TODO: different icon for plugins and bin
        });
        listViewer.setSorter(new ViewerSorter());
        
        // buttons
        Composite composite_1 = new Composite(composite, SWT.NONE);
        composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
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
        
        btnEdit = new Button(composite_1, SWT.NONE);
        btnEdit.setBounds(0, 50, 92, 33);
        btnEdit.setText("Edit");
        btnEdit.setToolTipText("Edit external tool details");
        btnEdit.setEnabled(false);
        btnEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)listViewer.getSelection();
				Tool tool = (Tool)selection.getFirstElement();
				// show a dialog to edit the tool
				EditToolDialog dialog = new EditToolDialog(getShell(), tool.getName(), tool.getCommand(), tool.getType(), tool.getDoc());
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
				IStructuredSelection selection = (IStructuredSelection)listViewer.getSelection();
				Iterator<?> it = selection.iterator();
				while (it.hasNext()) {
					toolsMap.remove(((Tool)it.next()).getId());					
				}
				listViewer.refresh(false);
			}
		});

        return composite;
    }	
    
    /**
     * Get the updated list of tools.
     * @return the updated list of tools
     */
    public Map<Integer, Tool> getNewTools() {
    	return toolsMap;
    }
   
    @Override
	protected Point getInitialSize() {
		return new Point(620, 600);
	}
} 
