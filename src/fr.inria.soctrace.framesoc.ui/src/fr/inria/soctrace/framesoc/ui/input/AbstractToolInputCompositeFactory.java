package fr.inria.soctrace.framesoc.ui.input;

import org.eclipse.swt.widgets.Composite;

/**
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class AbstractToolInputCompositeFactory {

	/**
	 * 
	 * @param parent composite parent
	 * @param style composite style
	 * @return the Framesoc tool input composite
	 */
	public abstract AbstractToolInputComposite getComposite(Composite parent,
			int style);

}
