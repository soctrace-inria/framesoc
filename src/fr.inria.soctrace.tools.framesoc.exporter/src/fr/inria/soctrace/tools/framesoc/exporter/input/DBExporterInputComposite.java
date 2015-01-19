package fr.inria.soctrace.tools.framesoc.exporter.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputComposite;
import fr.inria.soctrace.tools.framesoc.exporter.dbexporter.TraceComboManager;

/**
 * DB exporter input composite
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DBExporterInputComposite extends AbstractToolInputComposite {

	private Text textDirectory;
	private TraceComboManager traceComboManager;
	protected ExporterInput input = new ExporterInput();

	public DBExporterInputComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		Group grpExportSettings = new Group(parent, SWT.NONE);
		grpExportSettings.setLayout(new GridLayout(1, false));
		grpExportSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpExportSettings.setText("Export Settings");

		// Trace
		Composite compositeTrace = new Composite(grpExportSettings, SWT.NONE);
		compositeTrace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeTrace.setSize(584, 41);
		compositeTrace.setLayout(new GridLayout(2, false));

		Label lblTrace = new Label(compositeTrace, SWT.NONE);
		lblTrace.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTrace.setText("Trace");

		Combo comboTraces = new Combo(compositeTrace, SWT.READ_ONLY);
		comboTraces.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		traceComboManager = new TraceComboManager(comboTraces, true);
		traceComboManager.loadAll();
		comboTraces.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateExporterInput();
			}
		});

		// Directory
		Composite compositeDirectory = new Composite(grpExportSettings, SWT.NONE);
		compositeDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		compositeDirectory.setLayout(new GridLayout(3, false));

		Label lblDirectory = new Label(compositeDirectory, SWT.NONE);
		lblDirectory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDirectory.setText("Export Directory");

		textDirectory = new Text(compositeDirectory, SWT.BORDER);
		textDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textDirectory.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateExporterInput();
			}
		});

		Button btnBrowse = new Button(compositeDirectory, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				textDirectory.setText("");
				DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.SINGLE);
				textDirectory.setText(dlg.open());
			}
		});
		btnBrowse.setText("Browse");
		
		input.trace = traceComboManager.getSelectedTrace();
		input.directory = "";

	}

	@Override
	public IFramesocToolInput getToolInput() {
		return input;
	}

	private void updateExporterInput() {
		// trace
		input.trace = traceComboManager.getSelectedTrace();
		// Directory
		input.directory = textDirectory.getText();
		// update ok in argument dialog
		dialog.updateOk();
	}

}
