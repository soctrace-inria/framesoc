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
package fr.inria.soctrace.framesoc.ui.toolbar;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import fr.inria.soctrace.framesoc.core.FramesocConstants.FramesocToolType;


/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class LaunchToolMenuContribution extends AbstractMenuContribution {
	
	public static final String LAUNCH_MENU_NAME = "fr.inria.soctrace.framesoc.ui.commands.LaunchTool";

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		createToolsItems(serviceLocator, additions, FramesocToolType.ANALYSIS, LAUNCH_MENU_NAME);
	}
	
}
