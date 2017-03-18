package net.dflmngr.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import net.dflmngr.logging.LoggingUtils;
import net.dflmngr.model.entity.DflPlayer;
import net.dflmngr.model.entity.DflPreseasonScores;
import net.dflmngr.model.service.DflPlayerService;
import net.dflmngr.model.service.DflPreseasonScoresService;
import net.dflmngr.model.service.GlobalsService;
import net.dflmngr.model.service.impl.DflPlayerServiceImpl;
import net.dflmngr.model.service.impl.DflPreseasonScoresServiceImpl;
import net.dflmngr.model.service.impl.GlobalsServiceImpl;

public class PreSeasonStatsHandler {
	
	private LoggingUtils loggerUtils;
	
	DflPlayerService dflPlayerService;
	GlobalsService globalsService;
	DflPreseasonScoresService dflPreseasonScoresService;
	
	boolean isExecutable;
		
	String defaultMdcKey = "batch.name";
	String defaultLoggerName = "batch-logger";
	String defaultLogfile = "PreSeasonStats";
	
	String mdcKey;
	String loggerName;
	String logfile;
	
	public PreSeasonStatsHandler() {
		
		PhantomJsDriverManager.getInstance().setup();
		
		dflPlayerService = new DflPlayerServiceImpl();
		globalsService = new GlobalsServiceImpl();
		dflPreseasonScoresService = new DflPreseasonScoresServiceImpl();
		
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
	
	public void execute(int round) {
		
		if(!isExecutable) {
			configureLogging(defaultMdcKey, defaultLoggerName, defaultLogfile);
			loggerUtils.log("info", "Default logging configured");
		}
		
		loggerUtils.log("info", "Downloading Pre-season Stats");
		
		List<String> preSeasonStatsUrls = getPreSeasonStatsUrls(round);
		Map<String, Integer> stats = getPreSeasonStats(preSeasonStatsUrls);
		
		List<DflPreseasonScores> preseasonScores = calculatePreseasonScore(stats);
		
		dflPreseasonScoresService.insertAll(preseasonScores, false);
		
		loggerUtils.log("info", "Player Pre-season stats saved");
	}
	
	private List<String> getPreSeasonStatsUrls(int round) {
		List<String> statsUrls = new ArrayList<>();
		
		String preSeasonFixtureUrl = globalsService.getPreSeasonFixtureUrl();
		
		WebDriver driver = new PhantomJSDriver();
		driver.get(preSeasonFixtureUrl);
		
		List<WebElement> fixtures = driver.findElement(By.className("fixture")).findElement(By.tagName("tbody")).findElements(By.className("broadcast"));
		
		for(WebElement fixture : fixtures) {
			String url = fixture.findElement(By.tagName("a")).getAttribute("href");
			
			int urlRound = Integer.parseInt(url.split("/")[6]);
			if(urlRound == round) {
				statsUrls.add(url);
			}
			
			statsUrls.add(url);
		}
		
		driver.quit();
		
		return statsUrls;
	}
	
	private Map<String, Integer> getPreSeasonStats(List<String> preSeasonStatsUrls) {
		
		Map<String, Integer> allStats = new HashMap<>();
		
		WebDriver driver = new PhantomJSDriver();
		
		for(String url : preSeasonStatsUrls) {
			driver.get(url);
						
			String roundStr = url.split("/")[6];
			String teams = url.split("/")[7];
			String homeTeam = teams.split("-")[0];
			String awayTeam = teams.split("-")[2];
			
			driver.findElement(By.cssSelector("a[href='#full-time-stats']")).click();
			driver.findElement(By.cssSelector("a[href='#advanced-stats']")).click();
			
			List<WebElement> homeStatsRecs = driver.findElement(By.id("homeTeam-advanced")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
			loggerUtils.log("info", "Found home team stats for: round={}; aflTeam={}; ", roundStr, homeTeam);
			List<WebElement> awayStatsRecs = driver.findElement(By.id("awayTeam-advanced")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
			loggerUtils.log("info", "Found away team stats for: round={}; aflTeam={}; ", roundStr, awayTeam);
			
			for(WebElement statsRec : homeStatsRecs) {
				List<WebElement> stats = statsRec.findElements(By.tagName("td"));
				
				String key = homeTeam + stats.get(1).getText() + "-" + roundStr;
				
				String name = stats.get(0).findElements(By.tagName("span")).get(1).getText();
				
				int disposals = Integer.parseInt(stats.get(4).getText());
				int marks = Integer.parseInt(stats.get(9).getText());
				int hitouts = Integer.parseInt(stats.get(12).getText());
				int freesFor = Integer.parseInt(stats.get(17).getText());
				int freesAgainst = Integer.parseInt(stats.get(18).getText());
				int tackles = Integer.parseInt(stats.get(19).getText());
				int goals = Integer.parseInt(stats.get(23).getText()) + Integer.parseInt(stats.get(24).getText());
				
				int total = disposals + marks + hitouts + freesFor + (-freesAgainst) + tackles + (goals * 3);
				
				
				loggerUtils.log("info", "Player stats: name={}, disposals={}, marks={} hitouts={}, freesFor{}, freesAgainst={}, tackles={}, goals={}, total={}",
							name, disposals, marks, hitouts, freesFor, freesAgainst, tackles, goals, total);
				
				allStats.put(key, total);
			}
			
			for(WebElement statsRec : awayStatsRecs) {
				List<WebElement> stats = statsRec.findElements(By.tagName("td"));
				
				String key = awayTeam + stats.get(1).getText() + "-" + roundStr;
				
				String name = stats.get(0).findElements(By.tagName("span")).get(1).getText();
				
				int disposals = Integer.parseInt(stats.get(4).getText());
				int marks = Integer.parseInt(stats.get(9).getText());
				int hitouts = Integer.parseInt(stats.get(12).getText());
				int freesFor = Integer.parseInt(stats.get(17).getText());
				int freesAgainst = Integer.parseInt(stats.get(18).getText());
				int tackles = Integer.parseInt(stats.get(19).getText());
				int goals = Integer.parseInt(stats.get(23).getText()) + Integer.parseInt(stats.get(24).getText());
				
				int total = disposals + marks + hitouts + freesFor + (-freesAgainst) + tackles + (goals * 3);
				
				
				loggerUtils.log("info", "Player stats: name={}, disposals={}, marks={} hitouts={}, freesFor{}, freesAgainst={}, tackles={}, goals={}, total={}",
							name, disposals, marks, hitouts, freesFor, freesAgainst, tackles, goals, total);
				
				allStats.put(key, total);
			}
			
			driver.close();
		}
		
		driver.quit();
		
		return allStats;
		
	}
	
	List<DflPreseasonScores> calculatePreseasonScore(Map<String, Integer> stats) {
		
		List<DflPreseasonScores> preseasonScores = new ArrayList<>();
		
		List<DflPlayer> players = dflPlayerService.findAll();
		
		for(DflPlayer player : players) {
			
			loggerUtils.log("info", "Handling player: player_id={}, afl_player_id={}", player.getPlayerId(), player.getAflPlayerId());
			
			Integer round1 = stats.get(player.getAflPlayerId() + "-" + 1);
			Integer round2 = stats.get(player.getAflPlayerId() + "-" + 2);
			Integer round3 = stats.get(player.getAflPlayerId() + "-" + 3);
			Integer round4 = stats.get(player.getAflPlayerId() + "-" + 4);
			
			DflPreseasonScores scores = new DflPreseasonScores();
			scores.setPlayerId(player.getPlayerId());
			
			if(round1 != null) {
				scores.setRound1(round1);
			}
			if(round2 != null) {
				scores.setRound2(round2);
			}
			if(round3 != null) {
				scores.setRound3(round3);
			}
			if(round4 != null) {
				scores.setRound1(round4);
			}
			
			loggerUtils.log("info", "Player scores: {}", scores);
			
			preseasonScores.add(scores);
		}
		
		return preseasonScores;
	}
	
	
	public static void main(String[] args) {
		PreSeasonStatsHandler testing = new PreSeasonStatsHandler();
		testing.configureLogging("batch.name", "batch-logger", "PreSeasonStats");
		testing.execute(Integer.parseInt(args[0]));
	}
	

}
