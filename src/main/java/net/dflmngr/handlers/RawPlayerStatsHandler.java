package net.dflmngr.handlers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JsonObject;
import org.json.simple.Jsoner;

import net.dflmngr.logging.LoggingUtils;
import net.dflmngr.model.entity.AflFixture;
import net.dflmngr.model.entity.DflRoundInfo;
import net.dflmngr.model.entity.DflRoundMapping;
import net.dflmngr.model.service.AflFixtureService;
import net.dflmngr.model.service.DflRoundInfoService;
import net.dflmngr.model.service.GlobalsService;
import net.dflmngr.model.service.RawPlayerStatsService;
import net.dflmngr.model.service.impl.AflFixtureServiceImpl;
import net.dflmngr.model.service.impl.DflRoundInfoServiceImpl;
import net.dflmngr.model.service.impl.GlobalsServiceImpl;
import net.dflmngr.model.service.impl.RawPlayerStatsServiceImpl;

public class RawPlayerStatsHandler {
	private LoggingUtils loggerUtils;
	
	DflRoundInfoService dflRoundInfoService;
	AflFixtureService aflFixtureService;
	GlobalsService globalsService;
	RawPlayerStatsService rawPlayerStatsService;
	
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
		rawPlayerStatsService = new RawPlayerStatsServiceImpl();
		
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
			
			loggerUtils.log("info", "Downloading player stats for DFL round: ", round);
			
			DflRoundInfo dflRoundInfo = dflRoundInfoService.get(round);
			
			List<AflFixture> fixturesToProcess = new ArrayList<>();
			Set<String> teamsToProcess = new HashSet<>();
			
			loggerUtils.log("info", "Checking for AFL rounds to download");
			for(DflRoundMapping roundMapping : dflRoundInfo.getRoundMapping()) {
				int aflRound = roundMapping.getAflRound();
				
				loggerUtils.log("info", "DFL round includes AFL round={}", aflRound);
				if(roundMapping.getAflGame() == 0) {
					List<AflFixture> fixtures = aflFixtureService.getAflFixturesPlayedForRound(aflRound);
					fixturesToProcess.addAll(fixtures);
					for(AflFixture fixture : fixtures) {
						teamsToProcess.add(fixture.getHomeTeam());
						teamsToProcess.add(fixture.getAwayTeam());
					}
				} else {
					int aflGame = roundMapping.getAflGame();
					AflFixture fixture = aflFixtureService.getPlayedGame(aflRound, aflGame);
					
					if(fixture != null) {
						if(roundMapping.getAflTeam() == null || roundMapping.getAflTeam().equals("")) {
							teamsToProcess.add(fixture.getHomeTeam());
							teamsToProcess.add(fixture.getAwayTeam());
						} else {
							teamsToProcess.add(roundMapping.getAflTeam());
						}
					}
				}
			}
			
			loggerUtils.log("info", "AFL games to download stats from: {}", fixturesToProcess);
			loggerUtils.log("info", "Team to take stats from: {}", teamsToProcess);
			
			processFixtures(round, fixturesToProcess, teamsToProcess, onHeroku);
			
			dflRoundInfoService.close();
			aflFixtureService.close();
			globalsService.close();
			rawPlayerStatsService.close();
			
			loggerUtils.log("info", "Player stats downaloded");
						
		} catch (Exception ex) {
			loggerUtils.log("error", "Error in ... ", ex);
		}
	}
		
	private void processFixtures(int round, List<AflFixture> fixturesToProcess, Set<String> teamsToProcess, boolean onHeroku) throws Exception {

		String year = globalsService.getCurrentYear();
		String statsUrl = globalsService.getAflStatsUrl();
		
		List<String> dynoNames = new ArrayList<>();

		for (AflFixture fixture : fixturesToProcess) {
			String homeTeam = fixture.getHomeTeam();
			String awayTeam = fixture.getAwayTeam();

			String fullStatsUrl = statsUrl + "/" + year + "/" + round + "/" + homeTeam.toLowerCase() + "-v-"
					+ awayTeam.toLowerCase();
			loggerUtils.log("info", "AFL stats URL: {}", fullStatsUrl);

			String herokuApiEndpoint = "https://api.heroku.com/apps/dfl-manager-dev/dynos";
			URL obj = new URL(herokuApiEndpoint);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/vnd.heroku+json; version=3");

			JsonObject postData = new JsonObject();
			postData.put("attach", "false");
			postData.put("command", "bin/run_raw_stats_downloader.sh " + round + " " + " " + homeTeam + " " + awayTeam + fullStatsUrl);
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
			List<String> completedDynos = new ArrayList<>();
			for(String dynoName: dynoNames) {
				String herokuApiEndpoint = "https://api.heroku.com/apps/dfl-manager-dev/dynos/" + dynoName;
				URL obj = new URL(herokuApiEndpoint);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				con.setRequestMethod("GET");
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
				
				String state = responseData.getString("name");
				
				loggerUtils.log("info", "Dyno State: {}", state);
				
				if(state == null || !state.equalsIgnoreCase("up")) {
					completedDynos.add(dynoName);
				}
			}
			dynoNames.removeAll(completedDynos);
		}
			
	}
	
	// For internal testing
	public static void main(String[] args) {
		RawPlayerStatsHandler testing = new RawPlayerStatsHandler();
		testing.configureLogging("batch.name", "batch-logger", "RawPlayerStatsHandlerTesting");
		testing.execute(1, true);
	}
}
