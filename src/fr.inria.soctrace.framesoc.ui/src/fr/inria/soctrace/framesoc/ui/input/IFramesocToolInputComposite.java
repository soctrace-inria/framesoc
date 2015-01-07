/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.input;

import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.ui.dialogs.IArgumentDialog;

/**
 * Interface for tool custom input forms.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IFramesocToolInputComposite {

	/**
	 * Get the tool input set via the UI.
	 * 
	 * @return the tool input
	 */
	IFramesocToolInput getToolInput();

	/**
	 * Set the argument dialog where this composite is used. The dialog reference must be used to
	 * call the {@link IArgumentDialog#updateOk()} method when a parameter is changed in the
	 * composite UI.
	 * 
	 * @param dialog
	 *            dialog using this composite
	 */
	void setArgumentDialog(IArgumentDialog dialog);
}
