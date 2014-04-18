package com.oakan.ccfinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CCExplorer implements Runnable {

	private final static Logger	logger	= Logger.getLogger(CCExplorer.class.getPackage().getName());

	private static final int	_CHECKIN_COUNTER_PRINT_PERIOD_10	= 10;
	private String		ldapUserParam;
	private Parameters	parameters;
	private int			checkinCounter;
	private int			commandCounter;

	public CCExplorer(Parameters parameters, String ldapUserParam) {
		this.parameters = parameters;
		this.ldapUserParam = ldapUserParam;
		this.checkinCounter = 0;
		this.commandCounter = 0;
	}

	@Override
	public void run() {
		this.executeClearcaseCommandByLdapUser();
	}

	private void executeClearcaseCommandByLdapUser() {
		logger.log(Level.INFO, "Started executing for:" + ldapUserParam);

		try {
			PrintWriter writer = new PrintWriter(parameters.outputFolder + "/" + ldapUserParam + ".txt", "UTF-8");

			try {
				executeCommandForEachFile(writer, parameters.ccPathParam);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				writer.close();
			}

		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		logger.log(Level.INFO, "Finished executing for:" + ldapUserParam);
	}

	private void executeCommandForEachFile(PrintWriter writer, String path) throws IOException {
		File file = new File(path);

		if (file.isDirectory() && !parameters.ccFolders2IgnoreSet.contains(file.getName())) {
			// writer.println(ldapUserParam + "--Started directory : "+file);
			// System.out.println(ldapUserParam + "--Started directory : "+file);
			for (String subFile : file.list()) {
				String currentFile = path + "\\" + subFile;
				executeCommandForEachFile(writer, currentFile);
				// System.out.println("Finished file : "+ currentFile);
			}
			// writer.println(ldapUserParam + "--Finished directory : "+file);
			// System.out.println(ldapUserParam + "--Finished directory : "+file);
			if (file.getParent().equals(parameters.ccPathParam)) {
				logger.log(Level.INFO, ldapUserParam + "--Finished directory : " + file);
			}
		} else if (file.isFile()) {
			String commandFindByCreator = MessageFormat.format(parameters.command, file.getParent(), file.getName(), parameters.createdSinceDate, ldapUserParam);
//			 System.out.println(commandFindByCreator);
			// writer.println(commandFindByCreator);
			// executeCommand(writer, commandFindByCreator);
			executeCommand(writer, commandFindByCreator);
		}
		writer.flush();
	}

	private static final int	_COMMAND_COUNTER_PRINT_PERIOD_100	= 100;

	private Process executeCommand(final PrintWriter writer, final String command) throws IOException {
		Process process = Runtime.getRuntime().exec(command);
		BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

		boolean isCCError = false;
		String line = null;
		
		while ((line = in.readLine()) != null) {
			
			if (line.startsWith(parameters.ccSnapshotPath)) {
				writer.println(line.substring(parameters.ccSnapshotPath.length()));
				if (++checkinCounter % _CHECKIN_COUNTER_PRINT_PERIOD_10 == 0) {
					logger.log(Level.INFO, ldapUserParam + "-Found " + checkinCounter + " files for this user.");
				}
				break;// sadece bir kere yazilsin dosyaya;
			} else {
				//hata almýþ bunu yazdýr.
				isCCError = true;
			}
			
			if (isCCError) {
				writer.println("<ERROR-IN-CLEARCASE>");
				writer.println(command);
				writer.println(line);
				writer.println("</ERROR-IN-CLEARCASE>");
				isCCError = false;
			}
		}

		if (++commandCounter % _COMMAND_COUNTER_PRINT_PERIOD_100 == 0) {
			logger.log(Level.INFO, ldapUserParam + "-" + commandCounter + " files checked for checkins.");
		}

		return process;
	}

	private static final long	_TIMEOUT_60							= 60L;

	@SuppressWarnings("unused")
	private void timedExecuteCommand(final PrintWriter writer, final String command) throws IOException {
		try {
			timedCall(new Callable<Integer>() {
				public Integer call() throws Exception {
					return executeCommand(writer, command).waitFor();
				}
			}, _TIMEOUT_60, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			String errorPrefix = "TimeoutException for command : ";
			logger.log(Level.SEVERE, ldapUserParam + "-" + errorPrefix + command);
			writer.println(errorPrefix);
			writer.println(command);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} catch (ExecutionException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

//	private static final ExecutorService	THREAD_POOL	= Executors.newCachedThreadPool();
	private static final ExecutorService	THREAD_POOL	= Executors.newSingleThreadExecutor();

	private static <T> T timedCall(Callable<T> c, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
		FutureTask<T> task = new FutureTask<T>(c);
		THREAD_POOL.execute(task);
		return task.get(timeout, timeUnit);
	}

}
