/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class CommandLineArgsInputComposite extends Composite {

	private Text argsText;
	private Text docText;

	public CommandLineArgsInputComposite(Composite parent, int style, boolean hasDoc) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		// arguments label
		Label argsLabel = new Label(this, SWT.NONE);
		argsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		argsLabel.setText("Arguments");

		// arguments text
		argsText = new Text(this, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		argsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		argsText.setEditable(true);

		if (hasDoc) {
			// doc label
			Label docLabel = new Label(this, SWT.NONE);
			docLabel.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.ITALIC));
			docLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
			docLabel.setText("Doc");

			// doc text
			docText = new Text(this, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL
					| SWT.MULTI);
			docText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		}
	}

	public void addArgsModifyListener(ModifyListener listener) {
		argsText.addModifyListener(listener);
	}

	public void addDocModifyListener(ModifyListener listener) {
		if (docText != null) {
			docText.addModifyListener(listener);
		}
	}

	public void setDocText(String text) {
		if (docText != null) {
			docText.setText(text);
		}
	}

}
