/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for adding a new parameter.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class NewParamDialog extends Dialog {

	private static final String MESSAGE = "New custom trace parameter";
	private TextListener fName;
	private TextListener fType;
	private TextListener fValue;

	public NewParamDialog(Shell parentShell) {
		super(parentShell);
		fName = new CheckStringListener("");
		fType = new CheckStringListener("");
		fValue = new CheckStringListener("");
	}

	private class CheckStringListener extends TextListener {
		
		public CheckStringListener(String initialValue) {
			super(initialValue);
		}

		@Override
		public void modifyText(ModifyEvent e) {
			super.modifyText(e);
			getButton(IDialogConstants.OK_ID).setEnabled(okEnabled());
		}

		private boolean okEnabled() {
			if (fName.getText().trim().isEmpty() || fType.getText().trim().isEmpty()
					|| fValue.getText().trim().isEmpty())
				return false;
			return true;
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite c = (Composite) super.createDialogArea(parent);
		c.setLayout(new GridLayout(1, false));

		Composite labelComposite = new Composite(c, SWT.NONE);
		labelComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		labelComposite.setLayout(new GridLayout(1, false));
		Label message = new Label(labelComposite, SWT.NONE);
		message.setText(MESSAGE);

		Composite composite = new Composite(c, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(2, false));

		Label lblName = new Label(composite, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name");

		Text name = new Text(composite, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		name.addModifyListener(fName);

		Label lblType = new Label(composite, SWT.NONE);
		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblType.setText("Type");

		Text type = new Text(composite, SWT.BORDER);
		type.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		type.addModifyListener(fType);

		Label lblValue = new Label(composite, SWT.NONE);
		lblValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblValue.setText("Value");

		Text value = new Text(composite, SWT.BORDER);
		value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		value.addModifyListener(fValue);
		
		return composite;

	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	public String getName() {
		return fName.getText();
	}

	public String getType() {
		return fType.getText();
	}

	public String getValue() {
		return fValue.getText();
	}

}
