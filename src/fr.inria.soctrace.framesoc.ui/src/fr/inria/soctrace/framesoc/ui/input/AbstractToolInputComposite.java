/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.input;

import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.framesoc.ui.dialogs.IArgumentDialog;

/**
 * Base abstract composite for all Framesoc custom tool input composites.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class AbstractToolInputComposite extends Composite implements
		IFramesocToolInputComposite {

	/**
	 * Reference to the argument dialog, which should be used to call the
	 * {@link IArgumentDialog#updateOk()} method when a parameter is modified in the UI.
	 */
	protected IArgumentDialog dialog;

	public AbstractToolInputComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void setArgumentDialog(IArgumentDialog dialog) {
		this.dialog = dialog;
	}

}
