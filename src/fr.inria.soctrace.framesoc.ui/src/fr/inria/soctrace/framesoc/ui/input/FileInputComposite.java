/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import fr.inria.soctrace.lib.utils.Portability;

/**
 * Composite providing a text field for reading file paths, with a browse button beside.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FileInputComposite extends Composite {

	private final static String FILES = "Trace files";
	private final static String BROWSE = "Browse";
	private final static int FILE_DIALOG_STYLE = SWT.MULTI;

	private Text traceFileText;

	private int fileDialogStyle = FILE_DIALOG_STYLE;
	private String fileLabel = FILES;
	private String browseLabel = BROWSE;

	public FileInputComposite(final Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(3, false));

		Label lblTraceFiles = new Label(this, SWT.NONE);
		lblTraceFiles.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTraceFiles.setText(fileLabel);

		traceFileText = new Text(this, SWT.BORDER);
		traceFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnBrowse = new Button(this, SWT.NONE);
		btnBrowse.setText(browseLabel);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				traceFileText.setText("");
				FileDialog dlg = new FileDialog(parent.getShell(), fileDialogStyle);
				String fn = dlg.open();
				if (fn != null) {
					String[] names = dlg.getFileNames();
					String filter = dlg.getFilterPath();
					for (int i = 0; i < names.length; ++i) {
						String name = Portability.normalize(filter + "/" + names[i]);
						traceFileText.append("\"" + name + "\" ");
					}
				}
			}
		});
	}

	public int getFileDialogStyle() {
		return fileDialogStyle;
	}

	public void setFileDialogStyle(int fileDialogStyle) {
		this.fileDialogStyle = fileDialogStyle;
	}

	public String getFileLabel() {
		return fileLabel;
	}

	public void setFileLabel(String fileLabel) {
		this.fileLabel = fileLabel;
	}

	public String getBrowseLabel() {
		return browseLabel;
	}

	public void setBrowseLabel(String browseLabel) {
		this.browseLabel = browseLabel;
	}

	public void addModifyListener(ModifyListener listener) {
		traceFileText.addModifyListener(listener);
	}

}
