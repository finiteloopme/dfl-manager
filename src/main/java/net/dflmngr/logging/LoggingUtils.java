package net.dflmngr.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LoggingUtils {
	
	private Logger logger;
	
	//private String loggerName;
	private String loggerKey;
	private String logFileBase;

	private String process;
	private boolean stdoutLogging;

	/*
	public LoggingUtils(String loggerName, String loggerKey, String logFileBase) {
		this.loggerName = loggerName;
		this.loggerKey = loggerKey;
		this.logFileBase = logFileBase;

		this.stdoutLogging = false;
		
		logger = LoggerFactory.getLogger(this.loggerName);
	}
	*/

	public LoggingUtils(String process) {
		this.process = process;
		this.stdoutLogging = true;
		logger = LoggerFactory.getLogger("stdout-logger");
	}
	
	public void log(String level, String msg, Object...arguments) {

		String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
		String callingMethod = Thread.currentThread().getStackTrace()[2].getMethodName();
		int lineNo = Thread.currentThread().getStackTrace()[2].getLineNumber();
		
		String loggerMsg = "[" + callingClass + "." +  callingMethod + "(Line:" + lineNo +")] - " + msg;

		if(stdoutLogging) {
			loggerMsg = "[" + process + "]" + loggerMsg;
		} else {
			MDC.put(loggerKey, logFileBase);
		}
		
		try {
			switch (level) {
				case "info" : logger.info(loggerMsg, arguments); break;
				case "error" : logger.error(loggerMsg, arguments);
			}
		} catch (Exception ex) {
			logger.error("Error in ... ", ex);
		} finally {
			if(!stdoutLogging)  {
				MDC.remove(loggerKey);	
			}
		}
	}
}