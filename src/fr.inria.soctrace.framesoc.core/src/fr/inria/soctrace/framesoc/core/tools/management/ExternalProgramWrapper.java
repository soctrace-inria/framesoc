package fr.inria.soctrace.framesoc.core.tools.management;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ExternalProgramWrapper {

	private final static Logger logger = LoggerFactory.getLogger(ExternalProgramWrapper.class);

	/**
	 * Program command with arguments
	 */
	protected List<String> fCommand;

	/**
	 * Constructor
	 * 
	 * @param command
	 *            program command
	 * @param arguments
	 *            command arguments
	 */
	public ExternalProgramWrapper(String command, List<String> arguments) {
		super();
		if (command == null)
			throw new NullPointerException();
		if (arguments == null)
			throw new NullPointerException();
		fCommand = new ArrayList<>(arguments.size() + 1);
		fCommand.add(command);
		fCommand.addAll(arguments);
	}

	/**
	 * Execute the external program, passing the command stdout and stderr to
	 * the given processor.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @param processor
	 *            line processor
	 * @return the execution status
	 */
	public IStatus execute(IProgressMonitor monitor, ILineProcessor processor) {
		logger.debug("Executing: {}", fCommand);

		try {
			ProcessBuilder pb = new ProcessBuilder(fCommand);
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.PIPE);
			Process p = pb.start();
			
			BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = bri.readLine()) != null && !monitor.isCanceled()) {
				processor.process(line);
			}
			bri.close();

		} catch (IOException e) {
			System.err.println(e.getMessage());
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}
}
