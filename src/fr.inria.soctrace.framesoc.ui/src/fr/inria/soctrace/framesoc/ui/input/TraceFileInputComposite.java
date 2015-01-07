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
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceFileInputComposite extends Composite {
	
	private Text traceFileText;

	public TraceFileInputComposite(final Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(3, false));
		
		Label lblTraceFiles = new Label(this, SWT.NONE);
		lblTraceFiles.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTraceFiles.setText("Trace files");
		
		traceFileText = new Text(this, SWT.BORDER);
		traceFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnBrowse = new Button(this, SWT.NONE);
		btnBrowse.setText("Browse");
		btnBrowse.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {
        		traceFileText.setText("");
        		FileDialog dlg = new FileDialog(parent.getShell(), SWT.MULTI);
        		String fn = dlg.open();
        		if (fn != null) {
        			String[] names = dlg.getFileNames();
        			String filter = dlg.getFilterPath();
        			for (int i=0; i<names.length; ++i) {
        				String name = Portability.normalize(filter + "/" + names[i]);
        				traceFileText.append("\"" + name + "\" ");
        			}
        		}
        	}
        });
	}
	
	public void addModifyListener (ModifyListener listener) {
        traceFileText.addModifyListener(listener);
	}

}
