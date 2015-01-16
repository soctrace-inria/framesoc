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
