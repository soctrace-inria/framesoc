package fr.inria.soctrace.tools.framesoc.exporter.input;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.ui.dialogs.IArgumentDialog;
import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputComposite;

/**
 * 
 */

/**
 * @author generoso
 *
 */
public class DBExporterInputComposite extends AbstractToolInputComposite {
	private Text text;

	
	/**
	 * @param parent
	 * @param style
	 */
	public DBExporterInputComposite(Composite parent, int style) {
		
		// XXX this in only test code
		
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		Button btnNewButton = new Button(this, SWT.NONE);
		btnNewButton.setText("New Button");
		
		Button btnCheckButton = new Button(this, SWT.CHECK);
		btnCheckButton.setText("Check Button");
		
		Spinner spinner = new Spinner(this, SWT.BORDER);
		
		Button btnRadioButton = new Button(this, SWT.RADIO);
		btnRadioButton.setText("Radio Button");
		
		Button btnCheckButton_1 = new Button(this, SWT.CHECK);
		btnCheckButton_1.setText("Check Button");
		
		Combo combo = new Combo(this, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		text = new Text(this, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Combo combo_1 = new Combo(this, SWT.NONE);
		combo_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel = new Label(this, SWT.NONE);
		lblNewLabel.setText("New Label");
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
