package net.dflmngr.scheduler.generators;

//import java.text.SimpleDateFormat;
//import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import net.dflmngr.jndi.JndiProvider;
import net.dflmngr.logging.LoggingUtils;
import net.dflmngr.model.entity.DflRoundInfo;
import net.dflmngr.model.service.DflRoundInfoService;
import net.dflmngr.model.service.GlobalsService;
import net.dflmngr.model.service.impl.DflRoundInfoServiceImpl;
import net.dflmngr.model.service.impl.GlobalsServiceImpl;
import net.dflmngr.scheduler.JobScheduler;
import net.dflmngr.utils.CronExpressionCreator;
//import net.dflmngr.webservice.CallDflmngrWebservices;

public class StartRoundJobGenerator {
	private LoggingUtils loggerUtils;
	
	DflRoundInfoService dflRoundInfoService;
	GlobalsService globalsService;
	
	private static String jobName = "StartRoundJob";
	private static String jobGroup = "Ongoing";
	private static String jobClass = "net.dflmngr.scheduler.jobs.StartRoundJob";
	
	//private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
	//private static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
	
	public StartRoundJobGenerator() {
		
		//loggerUtils = new LoggingUtils("batch-logger", "batch.name", "StartRoundJobGenerator");
		loggerUtils = new LoggingUtils("StartRoundJobGenerator");
		
		try {
			//JndiProvider.bind();		
			dflRoundInfoService = new DflRoundInfoServiceImpl();
			globalsService = new GlobalsServiceImpl();
		} catch (Exception ex) {
			loggerUtils.log("error", "Error in ... ", ex);
		}
	}
	
	public void execute() {
		try {
			loggerUtils.log("infp", "Executing StartRoundJobGenerator ....");
			
			List<DflRoundInfo> dflRounds = dflRoundInfoService.findAll();
			
			for(DflRoundInfo dflRound : dflRounds) {
				loggerUtils.log("info", "Creating job entry for round={}, lockout={}", dflRound.getRound(), dflRound.getHardLockoutTime());
				createReportJobEntry(dflRound.getRound(), dflRound.getHardLockoutTime());
			}
			
			dflRoundInfoService.close();
			globalsService.close();
			
			loggerUtils.log("info", "StartRoundJobGenerator completed");
		} catch (Exception ex) {
			loggerUtils.log("error", "Error in ... ", ex);
		}
	}
	
	private void createReportJobEntry(int round, ZonedDateTime time) throws Exception {
		
		CronExpressionCreator cronExpression = new CronExpressionCreator();
		
		//Calendar timeCal = Calendar.getInstance();
		//timeCal.setTime(time);
		//Calendar timeCal = GregorianCalendar.from(time);
		//ZonedDateTime runTime = time.withZoneSameInstant(ZoneId.of("UTC")).plusMinutes(10);
		
		
		/*
		//int mins = timeCal.get(Calendar.MINUTE) + 10;
		int mins = time.getMinute() + 10;
		
		if(mins >= 60) {
			//timeCal.set(Calendar.MINUTE, (mins - 60));
			//timeCal.set(Calendar.HOUR, (timeCal.get(Calendar.HOUR) + 1));
			runTime = runTime.withMinute(mins = 60);
			runTime = 
		} else {
			timeCal.set(Calendar.MINUTE, mins);
		}
		*/
		
		//cronExpression.setTime(timeFormat.format(timeCal.getTime()));
		//cronExpression.setStartDate(dateFormat.format(timeCal.getTime()));
		//cronExpression.setTime(runTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
		//cronExpression.setStartDate(runTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		cronExpression.setTime(time.format(DateTimeFormatter.ofPattern("hh:mm a")));
		cronExpression.setStartDate(time.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        
		Map<String, Object> jobParams = new HashMap<>();
		jobParams.put("ROUND", round);
		
		//CallDflmngrWebservices.scheduleJob(jobName, jobGroup, jobClass, jobParams, cronExpression.getCronExpression(), false, loggerUtils);
		JobScheduler.schedule(jobName, jobGroup, jobClass, jobParams, cronExpression.getCronExpression(), false);
	}
	
	public static void main(String[] args) {		
		StartRoundJobGenerator testing = new StartRoundJobGenerator();
		testing.execute();
	}
}
