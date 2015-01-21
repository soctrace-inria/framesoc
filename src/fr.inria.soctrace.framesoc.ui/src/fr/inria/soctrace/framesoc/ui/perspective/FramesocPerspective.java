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
package fr.inria.soctrace.framesoc.ui.perspective;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartContributionManager.PartContributionDescriptor;

/**
 * Framesoc default perspective factory.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocPerspective implements IPerspectiveFactory {

	public static final boolean DEBUG = false;

	/**
	 * Perspective ID as specified by the extension.
	 */
	public final static String ID = "fr.inria.soctrace.framesoc.ui.perspective.FramesocPerspective"; //$NON-NLS-1$ 

	// Marker for saying "any id following the primary id" (secondary id) 
	private final static String MULTI_VIEW = ":*"; //$NON-NLS-1$ 

	// Folder constants 
	public static final String TOP_LEFT = "TOP_LEFT"; //$NON-NLS-1$
	public static final String BOTTOM_LEFT = "BOTTOM_LEFT"; //$NON-NLS-1$
	public static final String TOP_RIGHT = "TOP_RIGHT"; //$NON-NLS-1$
	public static final String BOTTOM_RIGHT = "BOTTOM_RIGHT"; //$NON-NLS-1$

	@Override
	public void createInitialLayout(IPageLayout layout) {

		IViewLayout viewLayout = null;
		layout.setEditorAreaVisible(false);
		layout.addPerspectiveShortcut(FramesocPerspective.ID);

		// Top left folder
		IFolderLayout topLeftFolder = layout.createFolder(TOP_LEFT, IPageLayout.LEFT, 0.25f, IPageLayout.ID_EDITOR_AREA); 
		topLeftFolder.addView(FramesocViews.TRACE_TREE_VIEW_ID);
		viewLayout = layout.getViewLayout(FramesocViews.TRACE_TREE_VIEW_ID);
		viewLayout.setCloseable(false);

		// Bottom left folder
		IFolderLayout bottomLeftFolder = layout.createFolder(BOTTOM_LEFT, IPageLayout.BOTTOM, 0.45f, TOP_LEFT); 
		bottomLeftFolder.addView(FramesocViews.TRACE_DETAILS_VIEW_ID);
		viewLayout = layout.getViewLayout(FramesocViews.TRACE_DETAILS_VIEW_ID);
		viewLayout.setCloseable(false);

		// XXX DEBUG VIEW
		if (DEBUG) {
			debug(layout);
			return;
		}

		// Create a map of map where views ID are sorted by priority, within a given position
		final Map<String, Map<Integer, String>> sortedIds = new HashMap<String, Map<Integer, String>>();
		List<PartContributionDescriptor> parts = FramesocPartContributionManager.getInstance().getPartContributionDescriptors();
		for (PartContributionDescriptor des: parts) {
			if (!sortedIds.containsKey(des.position)) {
				sortedIds.put(des.position, new TreeMap<Integer, String>());
			}
			sortedIds.get(des.position).put(des.priority, des.id);
		}

		// Top right folder
		IFolderLayout topRightFolder = layout.createFolder(TOP_RIGHT, IPageLayout.TOP, 0.75f, IPageLayout.ID_EDITOR_AREA);
		if (sortedIds.containsKey(TOP_RIGHT)) {
			for (String id: sortedIds.get(TOP_RIGHT).values()) {
				topRightFolder.addPlaceholder(id + MULTI_VIEW);	
			}
		}

		// Bottom right folder
		IFolderLayout middleRightFolder = layout.createFolder(BOTTOM_RIGHT, IPageLayout.BOTTOM, 0.45f, TOP_RIGHT); 
		if (sortedIds.containsKey(BOTTOM_RIGHT)) {
			for (String id: sortedIds.get(BOTTOM_RIGHT).values()) {
				middleRightFolder.addPlaceholder(id + MULTI_VIEW);	
			}
		}

		// Create all Framesoc parts asynchronously as they need to be created after the folders 
		// have been laid-out. This ensures they have a secondary id according to our standards. 
		Display.getDefault().asyncExec(new Runnable() { 
			@Override 
			public void run() { 
				Iterator<Entry<String, Map<Integer, String>>> it = sortedIds.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Map<Integer, String>> entry = it.next();
					Map<Integer, String> pmap = entry.getValue();
					for (String id: pmap.values()) {
						FramesocPartManager.getInstance().getPartInstance(id, null);	
					}				
				}				
			} 
		}); 

	}

	/**
	 * Debug method to create only the debug view in the Framesoc perspective.
	 * @param layout page layout
	 */
	private void debug(IPageLayout layout) {
		IFolderLayout topRightFolder = layout.createFolder(TOP_RIGHT, IPageLayout.TOP, 0.75f, IPageLayout.ID_EDITOR_AREA);
		topRightFolder.addPlaceholder(FramesocViews.DEBUG_VIEW_ID + ":*");
		// create all views asynchronously as they need to be created after the folders etc 
		// have been laid-out. This ensures they have a secondary id according to our standards. 
		Display.getDefault().asyncExec(new Runnable() { 
			@Override 
			public void run() { 
				FramesocPartManager.getInstance().getPartInstance(FramesocViews.DEBUG_VIEW_ID, null);
			} 
		}); 
	}

}
