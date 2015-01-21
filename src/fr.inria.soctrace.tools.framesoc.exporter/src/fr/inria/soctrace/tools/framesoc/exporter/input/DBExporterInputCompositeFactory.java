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
package fr.inria.soctrace.tools.framesoc.exporter.input;

import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputComposite;
import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputCompositeFactory;

/**
 * Factory for DB exporter input composite.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DBExporterInputCompositeFactory extends AbstractToolInputCompositeFactory {

	@Override
	public AbstractToolInputComposite getComposite(Composite parent, int style) {
		return new DBExporterInputComposite(parent, style);
	}

}
