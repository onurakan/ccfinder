package com.oakan.ccfinder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class App {

	private final static Logger	logger						= Logger.getLogger(App.class.getPackage().getName());

	private static final int	_THREAD_COUNT_UPPER_LIMIT_8	= 8;

	public static void main(String[] args) throws InterruptedException, IOException {
		App.initLogger();
		CCFinderUtil.printCurrentDateTime("CCFINDER STARTED - ");
		Parameters parameters = new Parameters();
		CCFinderUtil.deleteFolder(parameters.outputFolder);
		CCFinderUtil.createFolder(parameters.outputFolder);
		CCFinderUtil.copyRuntimeProperties(parameters.outputFolder);
		App.seachInParallel(parameters);
		CCFinderUtil.printCurrentDateTime("CCFINDER FINISHED - ");
	}

	private static void seachInParallel(Parameters parameters) throws InterruptedException {
		int threadCount = parameters.ldapUsersParam.length > _THREAD_COUNT_UPPER_LIMIT_8 ? _THREAD_COUNT_UPPER_LIMIT_8 : parameters.ldapUsersParam.length;

		logger.log(Level.INFO, "Started searching for " + parameters.ldapUsersParam.length + " users in " + threadCount + " threads: ");

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		for (String ldapUserParam : parameters.ldapUsersParam) {
			Runnable worker = new CCExplorer(parameters, ldapUserParam);
			executor.execute(worker);
		}

		executor.shutdown();
		executor.awaitTermination(20000000, TimeUnit.HOURS);

		logger.log(Level.INFO, "Finished all threads");
	}

	public static void initLogger() throws SecurityException, IOException {
		logger.setUseParentHandlers(false);

		FileHandler logFileHandler = new FileHandler("output/app.log", true);
		logFileHandler.setFormatter(new BriefFormatter());

		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new BriefFormatter());

		logger.addHandler(logFileHandler);
		logger.addHandler(consoleHandler);
		logger.setLevel(Level.INFO);
	}

	public static class BriefFormatter extends Formatter {
		public BriefFormatter() {
			super();
		}

		@Override
		public String format(final LogRecord record) {
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

			StringBuffer sb = new StringBuffer();
//			sb.append("Thread-").append(record.getThreadID()).append("::")
			sb.append(dateFormat.format(new Date(record.getMillis()))).append("::")
//				.append(record.getSourceClassName()).append("::")
//				.append(record.getSourceMethodName()).append("::")
				.append(record.getMessage() + "\n");

			return sb.toString();
		}
	}
}
