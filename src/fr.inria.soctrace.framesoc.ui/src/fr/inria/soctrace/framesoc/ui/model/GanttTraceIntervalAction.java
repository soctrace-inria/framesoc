/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.model;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.wb.swt.ResourceManager;

import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class GanttTraceIntervalAction extends TraceIntervalAction {

	public static void add(IToolBarManager toolbarManager, TraceIntervalAction action) {
		if (FramesocPartManager.getInstance().isFramesocPartExisting(
				FramesocViews.GANTT_CHART_VIEW_ID)) {
			toolbarManager.add(action);
		}
	}
	
	public GanttTraceIntervalAction(FramesocPart part) {
		super(part);
		ImageDescriptor img = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
				"icons/gantt.png");
		this.setImageDescriptor(img);
		this.setText("Show in Gantt Chart");
	}
	
	@Override
	protected FramesocBusTopic getTopic() {
		return FramesocBusTopic.TOPIC_UI_GANTT_DISPLAY_TIME_INTERVAL;
	}

}
