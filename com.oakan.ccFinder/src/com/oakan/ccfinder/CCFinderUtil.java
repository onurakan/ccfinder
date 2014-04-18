package com.oakan.ccfinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CCFinderUtil {

	private final static Logger	logger	= Logger.getLogger(CCFinderUtil.class.getPackage().getName());

	private static ResourceBundle	resourceBundle	= null;
	private static final String	RUNTIME_PROPS_FILE_NAME__CCFINDER	= "ccfinder";

	public static String getResourceParameter(String key, boolean checkContainsKey) {
		if (resourceBundle == null) {
			resourceBundle = ResourceBundle.getBundle(RUNTIME_PROPS_FILE_NAME__CCFINDER);
		}
		if (checkContainsKey && !resourceBundle.containsKey(key)) {
			return null;
		}
		return resourceBundle.getString(key);
	}

	public static void printCurrentDateTime(String printPrefix) {
		TimeZone.setDefault(TimeZone.getTimeZone("Turkey"));
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		logger.log(Level.INFO, printPrefix + " time = " + dateFormat.format(cal.getTime()));
	}

	public static void createFolder(String folder) {
		new File(folder).mkdir();
		logger.log(Level.INFO, "created folder : " + folder);
	}

	public static void deleteFolder(String folder) {
		File directory = new File(folder);

		String[] entries = directory.list();

		if (null != entries) {
			for (String s : entries) {
				File currentFile = new File(directory.getPath(), s);
				currentFile.delete();
			}
		}
		logger.log(Level.INFO, "deleted folder : " + folder);
	}
	
	public static void copyRuntimeProperties(String outputFolder) throws IOException {
		String sourcePath = new App().getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		String destPath = System.getProperty("user.dir") + "/" + outputFolder + "/ccfinderRuntime.properties";
		CCFinderUtil.copyFile(new File(sourcePath + RUNTIME_PROPS_FILE_NAME__CCFINDER + ".properties"), new File(destPath));
	}

	private static void copyFile(File source, File dest) throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
			try {
				if (output != null) {
					output.close();
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("Turkey"));
		
		App.initLogger();
		logger.log(Level.ALL,"deneme");
		logger.log(Level.ALL,"deneme");
		System.out.println(System.getProperty("user.timezone"));
		System.out.println(System.getProperty("user.country"));
		System.out.println(System.getProperty("java.home"));
		TimeZone.setDefault(TimeZone.getTimeZone("Turkey"));
		TimeZone zone = TimeZone.getDefault();
		System.out.println(zone.getDisplayName());
		System.out.println(zone.getID());
		String workingDir = System.getProperty("user.dir");
		System.out.println("user.dir : " + workingDir);
//		copyRuntimeProperties("output/TEST");
		System.out.println("This is line seperator: \"" + System.getProperty("line.separator") +"\"");
		printCurrentDateTime("Finish");
	}
}
