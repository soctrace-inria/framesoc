/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.input;

import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.framesoc.core.tools.model.EmptyInput;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.ui.dialogs.IArgumentDialog;

/**
 * Empty tool input composite.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EmptyToolInputComposite extends AbstractToolInputComposite {

	private final static IFramesocToolInput EMPTY_INPUT = new EmptyInput();
	
	public EmptyToolInputComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public IFramesocToolInput getToolInput() {
		return EMPTY_INPUT;
	}

	@Override
	public void setArgumentDialog(IArgumentDialog dialog) {
		// NOP
	}

}
