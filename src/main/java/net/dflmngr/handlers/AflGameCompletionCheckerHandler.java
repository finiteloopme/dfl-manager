package net.dflmngr.handlers;

//import net.dflmngr.logging.LoggingUtils;

public class AflGameCompletionCheckerHandler {
	//private LoggingUtils loggerUtils;
	
	boolean isExecutable;
	
	String defaultMdcKey = "batch.name";
	String defaultLoggerName = "batch-logger";
	String defaultLogfile = "AflGameCompletionChecker";
	
	String mdcKey;
	String loggerName;
	String logfile;
	
	public AflGameCompletionCheckerHandler() {		
		isExecutable = false;
	}
	
	public void configureLogging(String mdcKey, String loggerName, String logfile) {
		//loggerUtils = new LoggingUtils(logfile);
		this.mdcKey = mdcKey;
		this.loggerName = loggerName;
		this.logfile = logfile;
		isExecutable = true;
	}
	
	
	

}
