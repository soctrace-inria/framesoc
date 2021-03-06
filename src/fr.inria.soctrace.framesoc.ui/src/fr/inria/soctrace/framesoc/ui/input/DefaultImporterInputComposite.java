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
import fr.inria.soctrace.framesoc.core.tools.model.FileInput;
import fr.inria.soctrace.framesoc.ui.dialogs.IArgumentDialog;
import fr.inria.soctrace.framesoc.ui.listeners.LaunchTextListener;

/**
 * Default tool input composite for importers.
 * 
 * It provides a text field for trace files, with a browse button beside.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DefaultImporterInputComposite extends AbstractToolInputComposite {

	protected FileInputComposite traceFiles;
	protected LaunchTextListener traceFileListener;

	public DefaultImporterInputComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		traceFiles = new FileInputComposite(parent, SWT.NONE);
		traceFiles.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	@Override
	public void setArgumentDialog(IArgumentDialog dialog) {
		super.setArgumentDialog(dialog);
		traceFileListener = new LaunchTextListener("", dialog);
		traceFiles.addModifyListener(traceFileListener);
	}

	@Override
	public IFramesocToolInput getToolInput() {
		FileInput input = new FileInput();
		input.setFiles(Arrays.asList(LaunchTextListener.getTokens(traceFileListener.getText())));
		return input;
	}

}
