/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.input;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.core.tools.model.TraceFileInput;
import fr.inria.soctrace.framesoc.ui.dialogs.IArgumentDialog;
import fr.inria.soctrace.framesoc.ui.listeners.LaunchTextListener;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DefaultImporterInputComposite extends AbstractToolInputComposite {

	private TraceFileInputComposite traceFiles;
	private LaunchTextListener traceFileListener;

	public DefaultImporterInputComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		traceFiles = new TraceFileInputComposite(parent, SWT.NONE);
		traceFiles.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	@Override
	public void setArgumentDialog(IArgumentDialog dialog) {
		traceFileListener = new LaunchTextListener("", dialog);
		traceFiles.addModifyListener(traceFileListener);
	}

	@Override
	public IFramesocToolInput getToolInput() {
		TraceFileInput input = new TraceFileInput();
		input.setTraceFiles(Arrays.asList(LaunchTextListener.getTokens(traceFileListener.getText())));
		return input;
	}

}
