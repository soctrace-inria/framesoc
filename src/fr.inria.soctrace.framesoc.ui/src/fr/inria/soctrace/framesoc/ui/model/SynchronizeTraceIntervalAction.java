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

/**
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public abstract class SynchronizeTraceIntervalAction extends TraceIntervalAction {

	public static void add(IToolBarManager toolbarManager,
			TraceIntervalAction action) {
		toolbarManager.add(action);
	}
	
	public SynchronizeTraceIntervalAction(FramesocPart part) {
		super(part);
		ImageDescriptor img = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
				"icons/synchronize.png");
		this.setImageDescriptor(img);
		this.setText("Synchronize all views of the same group");
	}
	
	@Override
	protected FramesocBusTopic getTopic() {
		return FramesocBusTopic.TOPIC_UI_SYNCHRONIZE_TIME_AND_FILTER;
	}

}
