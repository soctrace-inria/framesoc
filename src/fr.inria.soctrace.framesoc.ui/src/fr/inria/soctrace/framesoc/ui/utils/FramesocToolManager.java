package fr.inria.soctrace.framesoc.ui.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.ui.model.ToolDescriptor;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.framesoc.core.tools.model.EmptyInput;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

public class FramesocToolManager implements IFramesocBusListener {

	/**
	 * Single instance of the manager
	 */
	private static FramesocToolManager instance = null;

	/**
	 * Logger
	 */
	private static Logger logger = LoggerFactory
			.getLogger(FramesocToolManager.class);

	private FramesocBusTopicList topics;

	/**
	 * Instance getter
	 * 
	 * @return the manager instance
	 */
	public static FramesocToolManager getInstance() {
		if (instance == null)
			instance = new FramesocToolManager();
		return instance;
	}

	@Override
	public void handle(FramesocBusTopic topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_LAUNCH_TOOL)) {
			try {
				if (data instanceof ToolDescriptor) {
					ToolDescriptor tDes = (ToolDescriptor) data;

					IFramesocToolInput toolInput = tDes.getToolInput();

					// If no input is provided
					if (toolInput == null) {
						// Launch tool with empty input
						logger.debug("Using EmptyInput as default");
						toolInput = new EmptyInput();
					}

					if (tDes.getTool() != null) {
						logger.debug("Launching tool "
								+ tDes.getTool().getName());
						FramesocManager.getInstance().launchTool(
								tDes.getTool(), toolInput);
					} else if (!tDes.getToolName().isEmpty()) {
						Tool tool = FramesocManager.getInstance().getTool(
								tDes.getToolName());
						if (tool != null) {
							logger.debug("Launching tool " + tool.getName());
							FramesocManager.getInstance().launchTool(
									tDes.getTool(), toolInput);
						} else {
							throw new SoCTraceException(
									"Invalid tool name provided: "
											+ tDes.getToolName());
						}
					} else {
						throw new SoCTraceException("No valid tool provided.");
					}
				} else {
					throw new SoCTraceException(
							"Invalid data provided: was expecting data of type ToolDescriptor got "
									+ data.getClass().getName() + " instead.");
				}
			} catch (SoCTraceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * Private methods
	 */

	/**
	 * Private constructor. Prevents instantiation.
	 */
	private FramesocToolManager() {
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_LAUNCH_TOOL);
		topics.registerAll();
	}

}
