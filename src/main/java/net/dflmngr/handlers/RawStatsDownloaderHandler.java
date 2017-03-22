package net.dflmngr.handlers;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import net.dflmngr.logging.LoggingUtils;
import net.dflmngr.model.entity.RawPlayerStats;
import net.dflmngr.model.service.RawPlayerStatsService;
import net.dflmngr.model.service.impl.RawPlayerStatsServiceImpl;

public class RawStatsDownloaderHandler {
	private LoggingUtils loggerUtils;
	
	boolean isExecutable;
	
	String defaultLogfile = "RoundProgress";
	String logfile;
	
	RawPlayerStatsService rawPlayerStatsService;
	
	public RawStatsDownloaderHandler() {
		PhantomJsDriverManager.getInstance().setup();
		
		rawPlayerStatsService = new RawPlayerStatsServiceImpl();
		
		isExecutable = false;
	}
		
	public void configureLogging(String logfile) {
		loggerUtils = new LoggingUtils(logfile);
		this.logfile = logfile;
		isExecutable = true;
	}
	
	public void execute(int round, String homeTeam, String awayTeam, String statsUrl) {
		
		try {
			if(!isExecutable) {
				configureLogging(defaultLogfile);
				loggerUtils.log("info", "Default logging configured");
			}
			
			loggerUtils.log("info", "Downloading AFL stats: round={}, homeTeam={} awayTeam={} ur={}", round, homeTeam, awayTeam, statsUrl);
			
			List<RawPlayerStats> playerStats = null;
			boolean statsDownloaded = false;
			for(int i = 0; i < 5; i++) {
				loggerUtils.log("info", "Attempt {}", i);
				playerStats = downloadStats(round, homeTeam, awayTeam, statsUrl);
				loggerUtils.log("info", "Player stats count: {}", playerStats.size());
				if(playerStats.size() >= 44) {
					statsDownloaded = true;
					break;
				}
			}
			if(statsDownloaded) {
				loggerUtils.log("info", "Saving player stats to database");
				
				rawPlayerStatsService.removeStatsForRoundAndTeam(round, homeTeam);
				rawPlayerStatsService.removeStatsForRoundAndTeam(round, awayTeam);
				rawPlayerStatsService.insertAll(playerStats, false);
				
				loggerUtils.log("info", "Player stats saved");
			} else {
				loggerUtils.log("info", "Player stats were not downloaded");
			}
			
		} catch (Exception ex) {
			loggerUtils.log("error", "Error in ... ", ex);
		}
	}

	private List<RawPlayerStats> downloadStats(int round, String homeTeam, String awayTeam, String statsUrl) throws Exception {
		
		List<RawPlayerStats> playerStats = new ArrayList<>();
		
		WebDriver driver = new PhantomJSDriver();
						
		try {
			driver.get(statsUrl);
		} catch (Exception ex) {
			if(driver.findElements(By.cssSelector("a[href='#full-time-stats']")).isEmpty()) {
				driver.quit();
				throw new Exception("Error Loading page, URL:" + statsUrl, ex);
			}
		}
						
		playerStats.addAll(getStats(round, homeTeam, "h", driver));
		playerStats.addAll(getStats(round, awayTeam, "a", driver));
			
		driver.quit();
				
		return playerStats;
	}
	
	private List<RawPlayerStats> getStats(int round, String aflTeam, String homeORaway, WebDriver driver) throws Exception {
		
		driver.findElement(By.cssSelector("a[href='#full-time-stats']")).click();
		driver.findElement(By.cssSelector("a[href='#advanced-stats']")).click();
		
		List<WebElement> statsRecs;
		List<RawPlayerStats> teamStats = new ArrayList<>();
		
		if(homeORaway.equals("h")) {
			statsRecs = driver.findElement(By.id("homeTeam-advanced")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
			loggerUtils.log("info", "Found home team stats for: round={}; aflTeam={}; ", round, aflTeam);
		} else {
			statsRecs = driver.findElement(By.id("awayTeam-advanced")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
			loggerUtils.log("info", "Found away team stats for: round={}; aflTeam={}; ", round, aflTeam);
		}
		

		
		for(WebElement statsRec : statsRecs) {
			List<WebElement> stats = statsRec.findElements(By.tagName("td"));
			
			RawPlayerStats playerStats = new RawPlayerStats();
			playerStats.setRound(round);
						
			playerStats.setName(stats.get(0).findElements(By.tagName("span")).get(1).getText());
			
			playerStats.setTeam(aflTeam);
			
			playerStats.setJumperNo(Integer.parseInt(stats.get(1).getText()));
			playerStats.setKicks(Integer.parseInt(stats.get(2).getText()));
			playerStats.setHandballs(Integer.parseInt(stats.get(3).getText()));
			playerStats.setDisposals(Integer.parseInt(stats.get(4).getText()));
			playerStats.setMarks(Integer.parseInt(stats.get(9).getText()));
			playerStats.setHitouts(Integer.parseInt(stats.get(12).getText()));
			playerStats.setFreesFor(Integer.parseInt(stats.get(17).getText()));
			playerStats.setFreesAgainst(Integer.parseInt(stats.get(18).getText()));
			playerStats.setTackles(Integer.parseInt(stats.get(19).getText()));
			playerStats.setGoals(Integer.parseInt(stats.get(23).getText()));
			playerStats.setBehinds(Integer.parseInt(stats.get(24).getText()));
			
			loggerUtils.log("info", "Player stats: {}", playerStats);
			
			teamStats.add(playerStats);
		}
		
		return teamStats;
	}
	
	// For internal testing
	public static void main(String[] args) {
		
		int round = Integer.parseInt(args[0]);
		String homeTeam = args[1];
		String awayTeam = args[2];
		String statsUrl = args[3];
		
		RawStatsDownloaderHandler handler = new RawStatsDownloaderHandler();
		handler.configureLogging("RawPlayerDownloader");
		handler.execute(round, homeTeam, awayTeam, statsUrl);
	}
}