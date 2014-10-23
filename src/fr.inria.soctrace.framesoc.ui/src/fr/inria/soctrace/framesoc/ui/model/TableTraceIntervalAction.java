/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.model;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.wb.swt.ResourceManager;

import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class TableTraceIntervalAction extends TraceIntervalAction {

	public static void add(IToolBarManager toolbarManager, TraceIntervalAction action) {
		if (FramesocPartManager.getInstance().isFramesocPartExisting(
				FramesocViews.EVENT_TABLE_VIEW_ID)) {
			toolbarManager.add(action);
		}
	}
	
	public TableTraceIntervalAction() {
		ImageDescriptor img = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
				"icons/table.png");
		this.setImageDescriptor(img);
		this.setText("Show in Table");
	}
	
	@Override
	protected FramesocBusTopic getTopic() {
		return FramesocBusTopic.TOPIC_UI_TABLE_DISPLAY_TIME_INTERVAL;
	}

}
