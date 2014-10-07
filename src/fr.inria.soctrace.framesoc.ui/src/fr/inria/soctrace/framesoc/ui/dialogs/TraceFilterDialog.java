/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.dialogs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import fr.inria.linuxtools.tmf.ui.widgets.virtualtable.TmfVirtualTable;
import fr.inria.soctrace.lib.model.Trace;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceFilterDialog extends Dialog {

	private static final String TRACE_FILTER_DIALOG_TITLE = "Trace Search";

	private static final int DEFAULT_WIDTH = 60;
	private static final int DEFAULT_HEIGHT = 18;
	private TmfVirtualTable fTable;
	private List<Trace> fTraces;
	private Set<Trace> fChecked;

	public TraceFilterDialog(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	public void setChecked(List<Trace> traces, List<Trace> checked) {
		if (traces == null || checked == null)
			throw new NullPointerException();
		fTraces = traces;
		fChecked = new HashSet<>();
		for (Trace t : checked) {
			fChecked.add(t);
		}
	}

	public Set<Trace> getChecked() {
		return fChecked;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(TRACE_FILTER_DIALOG_TITLE);
		Composite composite = (Composite) super.createDialogArea(parent);

		Label messageLabel = createMessage(composite);
		Table table = createTable(composite);
		Control buttonComposite = createButtons(composite);

		if (fTraces.size() == 0) {
			messageLabel.setEnabled(false);
			table.setEnabled(false);
			buttonComposite.setEnabled(false);
		}

		return composite;

	}

	private Control createButtons(Composite composite) {
		// TODO Auto-generated method stub
		return null;
	}

	private Table createTable(Composite composite) {
		// TODO Auto-generated method stub
		return null;
	}

	private Label createMessage(Composite composite) {
		// TODO Auto-generated method stub
		return null;
	}

}
