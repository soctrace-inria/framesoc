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
package fr.inria.soctrace.framesoc.ui.listeners;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import fr.inria.soctrace.framesoc.ui.dialogs.IArgumentDialog;

/**
 * Text field listener to be used in argument dialogs.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class LaunchTextListener extends StringListener implements ModifyListener {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	private IArgumentDialog dialog;

	public LaunchTextListener(String initialValue, IArgumentDialog dialog) {
		super(initialValue);
		this.dialog = dialog;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		text = ((Text) e.getSource()).getText();
		dialog.updateOk();
	}

	/**
	 * Get the tokens contained in the text.
	 * 
	 * A token is a single string or a list of string between "".
	 * 
	 * <pre>
	 * For example, if the text is:
	 *    token1  "a b c" "this is a token" token4
	 * the returned array will contain the following values:
	 *    {token1,  "a b c", "this is a token", token4}
	 * </pre>
	 * 
	 * @param text
	 * @return
	 */
	public static String[] getTokens(String text) {
		try {
			if (text.matches("\\s*"))
				return EMPTY_STRING_ARRAY;
			String[] tokens = text.split("\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
			Pattern pattern = Pattern.compile("^\"(.*)\"$");
			for (int i = 0; i < tokens.length; i++) {
				Matcher matcher = pattern.matcher(tokens[i]);
				if (matcher.find()) {
					tokens[i] = matcher.group(1);
				}
			}
			return tokens;
		} catch (IllegalArgumentException e) {
			// TODO
			System.out.println("TODO check this");
		}
		return new String[0];
	}

}
