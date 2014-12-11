/*******************************************************************************
 * Copyright (c) 2012-2014 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.core.tools.management.ArgumentsManager;
import fr.inria.soctrace.framesoc.core.tools.management.ToolContributionManager;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocTool.ParameterCheckStatus;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.Portability;

/**
 * Eclipse Dialog to import a trace into the infrastructure.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ImportTraceDialog extends Dialog implements IArgumentDialog {

	private static final String IMPORT_TRACE_DIALOG_TITLE = "Import a new trace";
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	Map<String, Tool> toolsMap;
	Map<String, IFramesocTool> fsToolsMap; // the tool is null for non plugin tools
	
	// Data Listener
	private ComboListener importerNameListener;
	private LaunchTextListener otherArgsListener;
	private LaunchTextListener traceFileListener;
	
	// tool name
    private Label importerNameLabel;
    private Combo importerNameCombo;
    
    // file names
    private Label traceFileLabel;
    private Text traceFileText; 
    
    // other arguments
    private Label otherArgsLabel;
    private Text otherArgsText;
	private Label docLabel;
	private Text docText;
	private Group groupMessage;
	private Label message;

	public ImportTraceDialog(Shell parentShell, List<Tool> tools) throws SoCTraceException {
		super(parentShell);
		toolsMap = new HashMap<String, Tool>();
		fsToolsMap = new HashMap<String, IFramesocTool>();
		for(Tool t: tools) {
			toolsMap.put(t.getName(), t);
			fsToolsMap.put(t.getName(), ToolContributionManager.getToolLauncher(t));
		}
		importerNameListener = new ComboListener(toolsMap.keySet().iterator().next());
		otherArgsListener = new LaunchTextListener("", this);
		traceFileListener = new LaunchTextListener("", this);
	}
	
    @Override
    protected Control createDialogArea(Composite parent) {
        
    	this.getShell().setText(IMPORT_TRACE_DIALOG_TITLE);
        Composite composite = (Composite) super.createDialogArea(parent);
       
        GridData data;
      
        Group c = new Group(composite, SWT.NONE);
        c.setText("Import new trace");
        GridLayout layout = new GridLayout(2,false);
        c.setLayout(layout);
        data = new GridData(GridData.FILL_BOTH);
        c.setLayoutData(data);

        importerNameLabel = new Label(c, SWT.NONE);
        importerNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        importerNameLabel.setText("Importer");
        importerNameCombo = new Combo(c, SWT.BORDER | SWT.READ_ONLY);
        importerNameCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        for (String s: toolsMap.keySet()) {
        	importerNameCombo.add(s);
        }
        importerNameCombo.select(0);
        importerNameCombo.addSelectionListener(importerNameListener);
        importerNameCombo.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		docText.setText(toolsMap.get(importerNameCombo.getText()).getDoc());
        		updateOk();
        	}
		});

        // browse
        traceFileLabel = new Label(c, SWT.NONE);
        traceFileLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        traceFileLabel.setText("Trace files");

        final Composite br = new Composite(c, SWT.NONE);
        br.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        GridLayout brlayout = new GridLayout(2,false);
        brlayout.marginWidth = 0;
        brlayout.marginHeight = 0;
        br.setLayout(brlayout);
        traceFileText = new Text(br, SWT.BORDER);
        data = new GridData(GridData.FILL_BOTH);
        data.minimumWidth = 400;
        traceFileText.setLayoutData(data);
        traceFileText.addModifyListener(traceFileListener);

        Button multi = new Button(br, SWT.PUSH);
        multi.setText("Browse");
        multi.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {
        		traceFileText.setText("");
        		FileDialog dlg = new FileDialog(br.getShell(), SWT.MULTI);
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

        // other arguments label
        otherArgsLabel = new Label(c, SWT.NONE);
        otherArgsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
        otherArgsLabel.setText("Other arguments");

        // other arguments text
        otherArgsText = new Text(c, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
        otherArgsText.setTouchEnabled(true);
        data = new GridData(GridData.FILL_BOTH);
        data.minimumWidth = 400;
        data.minimumHeight = 50;
        otherArgsText.addModifyListener(otherArgsListener);
        otherArgsText.setLayoutData(data);

        // doc label
        docLabel = new Label(c, SWT.NONE);
        docLabel.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.ITALIC));
        docLabel.setAlignment(SWT.CENTER);
        docLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
        docLabel.setText("Doc");
        
        // doc text
        docText = new Text(c, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
        data = new GridData(GridData.FILL_BOTH);
        docText.setLayoutData(data);
        docText.setText(toolsMap.get(importerNameCombo.getText()).getDoc());
        
        groupMessage = new Group(composite, SWT.V_SCROLL);
        groupMessage.setText("Error message");
        groupMessage.setLayout(new GridLayout(1, false));
        groupMessage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        message = new Label(groupMessage, SWT.WRAP);
        message.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        return composite;
    }	
    
    @Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		// OK enabled only if the Framesoc tool says so
        updateOk();
	}
	
    private ParameterCheckStatus canLaunch() {
    	ParameterCheckStatus status = new ParameterCheckStatus(false, "");
    	Tool t = getTool();
    	if (t == null) {
    		status.message = "Importer not existing";
    		return status;
    	}
    	IFramesocTool tool = getToolLauncher();
    	if (tool == null) {
    		status.message = "Importer not existing";
    		return status;
    	}
    	return tool.canLaunch(getArgs()); 
    }
    	
	private IFramesocTool getToolLauncher() {
		return fsToolsMap.get(importerNameListener.getText());
	}
	
	private String[] getTraceFiles() {
		if (traceFileListener.getText().matches("\\s*"))
			return EMPTY_STRING_ARRAY;
		String[] tokens = traceFileListener.getText().split("\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
		Pattern pattern = Pattern.compile("^\"(.*)\"$");
		for (int i=0; i<tokens.length; i++) {
			Matcher matcher = pattern.matcher(tokens[i]);
			if (matcher.find()) {
			    tokens[i] = matcher.group(1);
			}
		}
		return tokens;
	}

	private String[] getOtherArgs() {
		if (otherArgsListener.getText().matches("\\s*"))
			return EMPTY_STRING_ARRAY;
		return ArgumentsManager.tokenize(otherArgsListener.getText()); 
	}
	
	protected Point getInitialSize() {
		return new Point(625, 450);
	}

	@Override
	public void updateOk() {
		Button ok = getButton(IDialogConstants.OK_ID);
		if (ok == null)
			return;
		ParameterCheckStatus status = canLaunch();
		message.setText(status.message);
		message.setToolTipText(status.message);
		ok.setEnabled(status.valid);
	}

	public Tool getTool() {
		return toolsMap.get(importerNameListener.getText());
	}

	public String[] getArgs() {
		// argument list
		String[] files = getTraceFiles();
		String[] others = getOtherArgs();
		String[] args = new String[files.length+others.length];
		
		// build the argument list
		for (int i=0; i<others.length; ++i) {
			args[i] = others[i];
		}
		for (int i=others.length; i<files.length + others.length; ++i) {
			args[i] = files[i - others.length];
		}
		
		return args;
	}
}
