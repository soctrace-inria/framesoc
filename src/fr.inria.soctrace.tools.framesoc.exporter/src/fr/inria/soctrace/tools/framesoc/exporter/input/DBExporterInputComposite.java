package fr.inria.soctrace.tools.framesoc.exporter.input;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.ui.dialogs.IArgumentDialog;
import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputComposite;

/**
 * DB exporter input composite
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DBExporterInputComposite extends AbstractToolInputComposite {

	/**
	 * 
	 * @param parent
	 * @param style
	 */
	public DBExporterInputComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		// TODO
	}

	@Override
	public IFramesocToolInput getToolInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setArgumentDialog(IArgumentDialog dialog) {
		// TODO Auto-generated method stub
	}

}
