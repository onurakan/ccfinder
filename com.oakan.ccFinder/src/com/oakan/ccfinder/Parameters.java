package com.oakan.ccfinder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parameters {

	private final static Logger	logger	= Logger.getLogger(Parameters.class.getPackage().getName());
	
	String		ccSnapshotPath;
	String[]	ldapUsersParam;
	String		ccPathParam;
	String		createdSinceDate;
	String		command;
	Set<String>	ccFolders2IgnoreSet	= new HashSet<String>();
	String		outputFolder;

	public Parameters() {
		readParameters();
	}

	private void readParameters() {
		ccSnapshotPath = CCFinderUtil.getResourceParameter("ccSnapshotPath", false);
		ldapUsersParam = CCFinderUtil.getResourceParameter("ldapUsers", false).split(",");
		ccPathParam = CCFinderUtil.getResourceParameter("ccPath", false);
		createdSinceDate = CCFinderUtil.getResourceParameter("created_since", false);
		command = CCFinderUtil.getResourceParameter("findByCreator", false);
		outputFolder = "output/" + new File(ccPathParam).getName();

		String ccFolders2Ignore = CCFinderUtil.getResourceParameter("ccFolders2Ignore", false);
		for (String folder : ccFolders2Ignore.split(",")) {
			ccFolders2IgnoreSet.add(folder);
		}

		logger.log(Level.INFO, "Seaching in : " + ccPathParam);
		logger.log(Level.INFO, "These folders will be ignored in seach: " + ccFolders2Ignore);
		logger.log(Level.INFO, "Created after this date : " + createdSinceDate);

		StringBuffer users = new StringBuffer();
		for (String ldapUser : ldapUsersParam) {
			users.append(ldapUser).append(",");
		}
		
		logger.log(Level.INFO, "Users:" + users.toString().substring(0, users.length() - 1));
	}

}
