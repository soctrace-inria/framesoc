package fr.inria.soctrace.framesoc.ui.input;

import org.eclipse.swt.widgets.Composite;

/**
 * Base abstract class for tool input composite factories.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class AbstractToolInputCompositeFactory {

	/**
	 * Create a composite.
	 * 
	 * @param parent composite parent
	 * @param style composite style
	 * @return the Framesoc tool input composite
	 */
	public abstract AbstractToolInputComposite getComposite(Composite parent,
			int style);

}
