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
 * Base wrapper for external program execution in Eclipse.
 * 
 * <pre>
 * The class provides two execute() methods for executing a program:
 * - a "push style" method, which pushes each line of output to a line processor 
 *   (data pushed to the client code)
 * - a "pop style" method, which provides the client code with a BufferedReader
 *   (data popped by the client code)
 * </pre>
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
	 * The external program is destroyed if the progress monitor is stopped.
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
			p.destroy();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}

	/**
	 * Execute the external program, immediately returning a buffered reader to
	 * get the command stdout and stderr.
	 * 
	 * The program is destroyed if the progress monitor is cancelled.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @return the buffered reader to read program stdout and stderr
	 */
	public BufferedReader execute(final IProgressMonitor monitor) {
		logger.debug("Executing: {}", fCommand);

		try {
			ProcessBuilder pb = new ProcessBuilder(fCommand);
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.PIPE);
			final Process p = pb.start();

			Thread watchDog = new Thread() {
				@Override
				public void run() {
					boolean exited = false;
					while (!monitor.isCanceled() && !exited) {
						try {
							p.exitValue();
							exited = true;
						} catch (IllegalThreadStateException e) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
					if (!exited) {
						p.destroy();
					}
				}
			};

			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			watchDog.start();

			return br;

		} catch (IOException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}
}
