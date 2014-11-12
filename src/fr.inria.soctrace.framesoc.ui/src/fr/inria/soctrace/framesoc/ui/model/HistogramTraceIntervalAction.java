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
public abstract class HistogramTraceIntervalAction extends TraceIntervalAction {

	public static void add(IToolBarManager toolbarManager, TraceIntervalAction action) {
		if (FramesocPartManager.getInstance().isFramesocPartExisting(
				FramesocViews.HISTOGRAM_VIEW_ID)) {
			toolbarManager.add(action);
		}
	}
	
	public HistogramTraceIntervalAction() {
		ImageDescriptor img = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
				"icons/histogram.png");
		this.setImageDescriptor(img);
		this.setText("Show in Event Density Chart");
	}
	
	@Override
	protected FramesocBusTopic getTopic() {
		return FramesocBusTopic.TOPIC_UI_HISTOGRAM_DISPLAY_TIME_INTERVAL;
	}

}
