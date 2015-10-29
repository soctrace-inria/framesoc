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
public abstract class PieTraceIntervalAction extends TraceIntervalAction {

	public static void add(IToolBarManager toolbarManager, TraceIntervalAction action) {
		if (FramesocPartManager.getInstance().isFramesocPartExisting(
				FramesocViews.STATISTICS_PIE_CHART_VIEW_ID)) {
			toolbarManager.add(action);
		}
	}

	public PieTraceIntervalAction(FramesocPart part) {
		super(part);
		ImageDescriptor img = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
				"icons/piechart.png");
		this.setImageDescriptor(img);
		this.setText("Show in Pie Chart");
	}
	
	@Override
	protected FramesocBusTopic getTopic() {
		return FramesocBusTopic.TOPIC_UI_PIE_DISPLAY_TIME_INTERVAL;
	}

}
