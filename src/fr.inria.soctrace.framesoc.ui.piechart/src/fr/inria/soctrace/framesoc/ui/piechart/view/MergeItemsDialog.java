/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.view;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.listeners.TextListener;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class MergeItemsDialog extends Dialog {

	private TextListener label = new TextListener("");
	private FramesocColor color = FramesocColor.BLACK;
	private Text labelText;

	protected MergeItemsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite all = (Composite) super.createDialogArea(parent);
		all.setLayout(new GridLayout(1, false));

		Group grpMergedItem = new Group(all, SWT.NONE);
		grpMergedItem.setText("Merged Item");
		grpMergedItem.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpMergedItem.setLayout(new GridLayout(2, false));

		Label labelLabel = new Label(grpMergedItem, SWT.NONE);
		labelLabel.setText("Label:");

		labelText = new Text(grpMergedItem, SWT.BORDER);
		labelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		labelText.addModifyListener(label);

		Label labelColor = new Label(grpMergedItem, SWT.NONE);
		labelColor.setText("Color:");

		Composite composite = new Composite(grpMergedItem, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		gl_composite.horizontalSpacing = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Composite colorComposite = new Composite(composite, SWT.BORDER);
		GridData gd_colorComposite = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
		gd_colorComposite.heightHint = 27;
		gd_colorComposite.widthHint = 40;
		colorComposite.setLayoutData(gd_colorComposite);
		colorComposite.setBackground(color.getSwtColor());
		colorComposite.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				changeColor(colorComposite);
			}
		});

		Button buttonColor = new Button(composite, SWT.NONE);
		buttonColor.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		buttonColor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				changeColor(colorComposite);
			}
		});
		buttonColor.setText("Change Color");

		all.pack();

		return all;
	}

	private void changeColor(Composite colorComposite) {
		ColorDialog dlg = new ColorDialog(getShell());
		RGB rgb = dlg.open();
		if (rgb != null) {
			if (color != null) {
				color.dispose();
			}
			color = new FramesocColor(rgb.red, rgb.green, rgb.blue);
		}
		colorComposite.setBackground(color.getSwtColor());		
	}
	public String getLabel() {
		return label.getText();
	}

	public FramesocColor getColor() {
		return color;
	}
}
