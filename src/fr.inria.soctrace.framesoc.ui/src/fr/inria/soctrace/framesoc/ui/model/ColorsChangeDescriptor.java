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
/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.model;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;

/**
 * Descriptor for a color change event, sent on the {@link FramesocBus}.
 * This descriptor is associated with the topic
 * {@link fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic#TOPIC_UI_COLORS_CHANGED}.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ColorsChangeDescriptor {

	/**
	 * Model entity
	 */
	private ModelEntity entity;

	/**
	 * @return the entity
	 */
	public ModelEntity getEntity() {
		return entity;
	}

	/**
	 * @param entity the entity to set
	 */
	public void setEntity(ModelEntity entity) {
		this.entity = entity;
	}

	@Override
	public String toString() {
		return "ColorChangeDescriptor [entity=" + entity + "]";
	}
	
}
