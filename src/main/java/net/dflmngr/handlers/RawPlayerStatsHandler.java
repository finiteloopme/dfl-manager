package net.dflmngr.handlers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
import java.net.URL;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JsonObject;
import org.json.simple.Jsoner;

import net.dflmngr.logging.LoggingUtils;
import net.dflmngr.model.entity.AflFixture;
import net.dflmngr.model.entity.DflRoundInfo;
import net.dflmngr.model.entity.DflRoundMapping;
import net.dflmngr.model.entity.Process;
import net.dflmngr.model.service.AflFixtureService;
import net.dflmngr.model.service.DflRoundInfoService;
import net.dflmngr.model.service.GlobalsService;
import net.dflmngr.model.service.ProcessService;
//import net.dflmngr.model.service.RawPlayerStatsService;
import net.dflmngr.model.service.impl.AflFixtureServiceImpl;
import net.dflmngr.model.service.impl.DflRoundInfoServiceImpl;
import net.dflmngr.model.service.impl.GlobalsServiceImpl;
import net.dflmngr.model.service.impl.ProcessServiceImpl;
//import net.dflmngr.model.service.impl.RawPlayerStatsServiceImpl;
//import net.dflmngr.utils.DflmngrUtils;

public class RawPlayerStatsHandler {
	private LoggingUtils loggerUtils;
	
	DflRoundInfoService dflRoundInfoService;
	AflFixtureService aflFixtureService;
	GlobalsService globalsService;
	//RawPlayerStatsService rawPlayerStatsService;
	ProcessService processService;
	
	boolean isExecutable;
		
	String defaultMdcKey = "batch.name";
	String defaultLoggerName = "batch-logger";
	String defaultLogfile = "RawPlayerStatsHandler";
	
	String mdcKey;
	String loggerName;
	String logfile;
	
	public RawPlayerStatsHandler() {		
		dflRoundInfoService = new DflRoundInfoServiceImpl();
		aflFixtureService = new AflFixtureServiceImpl();
		globalsService = new GlobalsServiceImpl();
		//rawPlayerStatsService = new RawPlayerStatsServiceImpl();
		processService = new ProcessServiceImpl();
		
		isExecutable = false;
	}
	
	public void configureLogging(String mdcKey, String loggerName, String logfile) {
		//loggerUtils = new LoggingUtils(loggerName, mdcKey, logfile);
		loggerUtils = new LoggingUtils(logfile);
		this.mdcKey = mdcKey;
		this.loggerName = loggerName;
		this.logfile = logfile;
		isExecutable = true;
	}
	
	public void execute(int round, boolean onHeroku) {
				
		try {
			if(!isExecutable) {
				configureLogging(defaultMdcKey, defaultLoggerName, defaultLogfile);
				loggerUtils.log("info", "Default logging configured");
			}
			
			loggerUtils.log("info", "Downloading player stats for DFL round: {}", round);
			
			DflRoundInfo dflRoundInfo = dflRoundInfoService.get(round);
			
			List<AflFixture> fixturesToProcess = new ArrayList<>();
			//Set<String> teamsToProcess = new HashSet<>();
			
			Set<Integer> aflGames = new HashSet<>();
			
			loggerUtils.log("info", "Checking for AFL rounds to download");
			for(DflRoundMapping roundMapping : dflRoundInfo.getRoundMapping()) {
				int aflRound = roundMapping.getAflRound();
				
				loggerUtils.log("info", "DFL round includes AFL round={}", aflRound);
				if(roundMapping.getAflGame() == 0) {
					List<AflFixture> fixtures = aflFixtureService.getAflFixturesPlayedForRound(aflRound);
					fixturesToProcess.addAll(fixtures);
					//for(AflFixture fixture : fixtures) {
					//	teamsToProcess.add(fixture.getHomeTeam());
					//	teamsToProcess.add(fixture.getAwayTeam());
					//}
				} else {
					int aflGame = roundMapping.getAflGame();
					AflFixture fixture = aflFixtureService.getPlayedGame(aflRound, aflGame);
					
					if(fixture != null) {
						if(!aflGames.contains(aflGame)) {
							aflGames.add(aflGame);
							fixturesToProcess.add(fixture);
						}
						//if(roundMapping.getAflTeam() == null || roundMapping.getAflTeam().equals("")) {
						//	teamsToProcess.add(fixture.getHomeTeam());
						//	teamsToProcess.add(fixture.getAwayTeam());
						//} else {
						//	teamsToProcess.add(roundMapping.getAflTeam());
						//}
					}
				}
			}
			
			loggerUtils.log("info", "AFL games to download stats from: {}", fixturesToProcess);
			//loggerUtils.log("info", "Team to take stats from: {}", teamsToProcess);
			
			processFixtures(round, fixturesToProcess, onHeroku);
			
			dflRoundInfoService.close();
			aflFixtureService.close();
			globalsService.close();
			//rawPlayerStatsService.close();
			
			loggerUtils.log("info", "Player stats downaloded");
						
		} catch (Exception ex) {
			loggerUtils.log("error", "Error in ... ", ex);
		}
	}
		
	private void processFixtures(int round, List<AflFixture> fixturesToProcess, boolean onHeroku) throws Exception {
		
		String year = globalsService.getCurrentYear();
		String statsUrl = globalsService.getAflStatsUrl();
		

		//List<String> dynoNames = new ArrayList<>();
		
		//ZonedDateTime now = ZonedDateTime.now(ZoneId.of(DflmngrUtils.defaultTimezone));

		for (AflFixture fixture : fixturesToProcess) {
			String homeTeam = fixture.getHomeTeam();
			String awayTeam = fixture.getAwayTeam();
			String aflRound = Integer.toString(fixture.getRound());

			String fullStatsUrl = statsUrl + "/" + year + "/" + aflRound + "/" + homeTeam.toLowerCase() + "-v-" + awayTeam.toLowerCase();
			loggerUtils.log("info", "AFL stats URL: {}", fullStatsUrl);

			if(onHeroku) {
				String command = "bin/run_raw_stats_downloader.sh " + round + " " + " " + homeTeam + " " + awayTeam + " " + fullStatsUrl;
				
				int tries = 1;
				while(!spawnDyno(command)) {
					if(tries == 3) {
						break;
					}
					tries++;
				}
			} else {
				loggerUtils.log("info", "Running locally ... ");
				RawStatsDownloaderHandler handler = new RawStatsDownloaderHandler();
				handler.configureLogging("RawPlayerDownloader");
				handler.execute(round, homeTeam, awayTeam, fullStatsUrl);
			}
		}	
			/*
			String herokuApiEndpoint = "https://api.heroku.com/apps/" + System.getenv("APP_NAME") + "/dynos";
			String apiToken = System.getenv("HEROKU_API_TOKEN");
			URL obj = new URL(herokuApiEndpoint);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization", "Bearer " + apiToken);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/vnd.heroku+json; version=3");

			JsonObject postData = new JsonObject();
			postData.put("attach", "false");
			postData.put("command", "bin/run_raw_stats_downloader.sh " + round + " " + " " + homeTeam + " " + awayTeam + " " + fullStatsUrl);
			postData.put("size", "hobby");
			postData.put("type", "run");

			con.setDoOutput(true);

			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes(postData.toJson());
			out.flush();
			out.close();

			int responseCode = con.getResponseCode();

			loggerUtils.log("info", "Spawning Dyno to process fixture: {}", postData.toJson());
			loggerUtils.log("info", "Response Code: {}", responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String output;
			StringBuffer response = new StringBuffer();

			while ((output = in.readLine()) != null) {
				response.append(output);
			}
			in.close();
			
			loggerUtils.log("info", "Response data: {}", response.toString());
			
			JsonObject responseData = Jsoner.deserialize(response.toString(), new JsonObject());
			
			String dynoName = responseData.getString("name");
			
			loggerUtils.log("info", "Dyno: {}", dynoName);
			
			dynoNames.add(dynoName);
		}
		
		while(dynoNames.size() > 0) {
			loggerUtils.log("info", "Wait for stats to download....");
			Thread.sleep(30000);
			List<String> completedDynos = new ArrayList<>();
			for(String dynoName: dynoNames) {
				/*
				String herokuApiEndpoint = "https://api.heroku.com/apps/dfl-manager-dev/dynos/" + dynoName;
				String apiToken = System.getenv("HEROKU_API_TOKEN");
				URL obj = new URL(herokuApiEndpoint);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				con.setRequestMethrasingod("GET");
				con.setRequestProperty("Authorization", "Bearer " + apiToken);
				con.setRequestProperty("Content-Type", "application/json");
				con.setRequestProperty("Accept", "application/vnd.heroku+json; version=3");

				int responseCode = con.getResponseCode();

				loggerUtils.log("info", "Checking Dyno: {}", dynoName);
				loggerUtils.log("info", "Response Code: {}", responseCode);

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String output;
				StringBuffer response = new StringBuffer();

				while ((output = in.readLine()) != null) {
					response.append(output);
				}
				in.close();
				
				loggerUtils.log("info", "Response data: {}", response.toString());
				
				JsonObject responseData = Jsoner.deserialize(response.toString(), new JsonObject());
				
				String state = responseData.getString("state");
				
				loggerUtils.log("info", "Dyno State: {}", state);
				
				if(state == null || !(state.equalsIgnoreCase("up") || state.equalsIgnoreCase("starting"))) {
					completedDynos.add(dynoName);
				}
				*/

				/*
				//Process process = processService.getProcess(dynoName, now).get(0);
				List<Process> processes = processService.getProcessById(dynoName);
				String status = "";
				String params = "";
				for(Process process : processes) {
					status = process.getStatus();
					loggerUtils.log("info", "Dyno stauts: {} is {}", dynoName, status);
					if(now.isBefore(process.getStartTime())) {
						status = process.getStatus();
						params = process.getParams();
						break;
					}
				}
				//if(!process.getStatus().equals("Running")) {
				if(status.equals("Completed") || status.equals("Failed")) {
					loggerUtils.log("info", "Completed: {} {}", dynoName, params);
					completedDynos.add(dynoName);
				}
			}
			dynoNames.removeAll(completedDynos);
		}
		*/
			
	}
	
	private boolean spawnDyno(String command) throws Exception {
		loggerUtils.log("info", "Spawning dyno with command={}", command);
		
		boolean success = false;
		
		String herokuApiEndpoint = "https://api.heroku.com/apps/" + System.getenv("APP_NAME") + "/dynos";
		String apiToken = System.getenv("HEROKU_API_TOKEN");
		URL obj = new URL(herokuApiEndpoint);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", "Bearer " + apiToken);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/vnd.heroku+json; version=3");

		JsonObject postData = new JsonObject();
		postData.put("attach", "false");
		postData.put("command", command);
		postData.put("size", "hobby");
		postData.put("type", "run");

		con.setDoOutput(true);

		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.writeBytes(postData.toJson());
		out.flush();
		out.close();

		int responseCode = con.getResponseCode();
		
		loggerUtils.log("info", "API JSON data={}", postData.toJson());
		loggerUtils.log("info", "Response Code: {}", responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String output;
		StringBuffer response = new StringBuffer();

		while ((output = in.readLine()) != null) {
			response.append(output);
		}
		in.close();
		
		loggerUtils.log("info", "Response data: {}", response.toString());
		
		JsonObject responseData = Jsoner.deserialize(response.toString(), new JsonObject());
		
		String dynoId = responseData.getString("id");
		
		loggerUtils.log("info", "Dyno: {}", dynoId);
		
		int countDown = 20;
		
		Process process = null;
		
		while(countDown != 0) {
			loggerUtils.log("info", "Wait for stats to download....");
			Thread.sleep(30000);
			
			if(process == null) {
				process = processService.get(dynoId);
			} else {
				processService.refresh(process);
			}
			
			if(process != null) {
				String status = process.getStatus();
				String params = process.getParams();
				loggerUtils.log("info", "Dyno stauts: {} is {}", dynoId, status);
				
				if(status.equals("Completed")) {
					loggerUtils.log("info", "Completed: {} {}", dynoId, params);
					success = true;
					break;
				} else if(status.equals("Failed")) {
					loggerUtils.log("info", "Completed: {} {}", dynoId, params);
					success = false;
					break;
				}
			}
			
			countDown--;
		}
				
		return success;
	}
	
	// For internal testing
	public static void main(String[] args) {
		Options options = new Options();
		
		Option roundOpt  = Option.builder("r").argName("round").hasArg().desc("round to run on").type(Number.class).required().build();
		Option onHerokuOpt = new Option("h", "use Heroku one off dyno");
		
		options.addOption(roundOpt);
		options.addOption(onHerokuOpt);
		
		try {
			int round = 0;
			boolean onHeroku = false;
						
			CommandLineParser parser = new DefaultParser();
			CommandLine cli = parser.parse(options, args);
			
			round = ((Number)cli.getParsedOptionValue("r")).intValue();
			
			if(cli.hasOption("h")) {
				onHeroku = true;
			}
		
			RawPlayerStatsHandler testing = new RawPlayerStatsHandler();
			testing.configureLogging("batch.name", "batch-logger", "RawPlayerStatsHandlerTesting");
			testing.execute(round, onHeroku);
			System.exit(0);
			
		} catch (ParseException ex) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "RawStatsHandler", options );
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
