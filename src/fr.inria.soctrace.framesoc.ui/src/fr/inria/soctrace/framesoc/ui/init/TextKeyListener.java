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
package fr.inria.soctrace.framesoc.ui.init;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Text;

/**
 * Key listener for text fields, simply checking
 * whether the text is empty or not at key released.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TextKeyListener implements KeyListener {

	Text text;
	WizardPage page;
	
	public TextKeyListener(Text text, WizardPage page) {
		this.text = text;
		this.page = page;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {}
	
	@Override
	public void keyReleased(KeyEvent e) {
		if (!text.getText().isEmpty()) {
			page.setPageComplete(true);
		}
	}


}
