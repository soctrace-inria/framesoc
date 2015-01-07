/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.input;

import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.framesoc.ui.dialogs.IArgumentDialog;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class AbstractToolInputComposite extends Composite implements IFramesocToolInputComposite {

	protected IArgumentDialog dialog;
	
	public AbstractToolInputComposite(Composite parent, int style) {
		super(parent, style);
	}

}
