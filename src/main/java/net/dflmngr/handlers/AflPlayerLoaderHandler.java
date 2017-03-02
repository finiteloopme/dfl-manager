package net.dflmngr.handlers;

import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import net.dflmngr.jndi.JndiProvider;
import net.dflmngr.logging.LoggingUtils;
import net.dflmngr.model.entity.AflPlayer;
import net.dflmngr.model.entity.AflTeam;
import net.dflmngr.model.entity.DflPlayer;
import net.dflmngr.model.entity.DflUnmatchedPlayer;
import net.dflmngr.model.service.AflPlayerService;
import net.dflmngr.model.service.AflTeamService;
import net.dflmngr.model.service.DflPlayerService;
import net.dflmngr.model.service.DflUnmatchedPlayerService;
import net.dflmngr.model.service.GlobalsService;
import net.dflmngr.model.service.impl.AflPlayerServiceImpl;
import net.dflmngr.model.service.impl.AflTeamServiceImpl;
import net.dflmngr.model.service.impl.DflPlayerServiceImpl;
import net.dflmngr.model.service.impl.DflUnmatchedPlayerServiceImpl;
import net.dflmngr.model.service.impl.GlobalsServiceImpl;

public class AflPlayerLoaderHandler {
	private LoggingUtils loggerUtils;
	
	private AflTeamService aflTeamService;
	private AflPlayerService aflPlayerService;
	private DflPlayerService dflPlayerService;
	private GlobalsService globalsService;
	private DflUnmatchedPlayerService dflUnmatchedPlayerService;
	
	private DateFormat df = new SimpleDateFormat("dd.MM.yy");
	
	public AflPlayerLoaderHandler() {
		
		//loggerUtils = new LoggingUtils("batch-logger", "batch.name", "AflPlayerLoader");
		loggerUtils = new LoggingUtils("AflPlayerLoader");
		
		try {
			
			//JndiProvider.bind();
			
			aflTeamService = new AflTeamServiceImpl();
			aflPlayerService = new AflPlayerServiceImpl();
			dflPlayerService = new DflPlayerServiceImpl();
			globalsService = new GlobalsServiceImpl();
			dflUnmatchedPlayerService = new DflUnmatchedPlayerServiceImpl();
			
		} catch (Exception ex) {
			loggerUtils.log("error", "Error in ... ", ex);
		}	
	}
	
	public void execute() {
		
		try {
			loggerUtils.log("info", "Executing AflPlayerLoader ...");
			
			List<AflTeam> aflTeams = aflTeamService.findAll();
			
			loggerUtils.log("info", "Processing teams: {}", aflTeams);
			
			processTeams(aflTeams);
			
			loggerUtils.log("info", "AflPlayerLoader Complete");
		} catch (Exception ex) {
			loggerUtils.log("error", "Error in ... ", ex);
		}
		
	}
	
	private void processTeams(List<AflTeam> aflTeams) throws Exception {
		
		List<AflPlayer> aflPlayers = new ArrayList<>();
		
		for(AflTeam team : aflTeams) {
			
			loggerUtils.log("info", "Working on team: {}", team.getTeamId());
			
			
			String teamListUrlS = team.getWebsite() + "/" + team.getSeniorUri();
			loggerUtils.log("info", "Senior list URL: {}", teamListUrlS);
			
			boolean isStreamOpen = false;
			int maxRetries = 5;
			int retries = 0;
			
			InputStream teamPage = null;
			
			while(!isStreamOpen) {
				boolean exception = false;
				try {
					teamPage = new URL(teamListUrlS).openStream();
				} catch (Exception ex) {
					exception = true;
					retries++;
					loggerUtils.log("info", "Failed to open team page retries {} of {}", retries, maxRetries);
					if(retries == maxRetries) {
						throw ex;
					}
				}
				if(!exception) {
					if(teamPage == null) {
						retries++;
						loggerUtils.log("info", "Failed to open team page retries {} of {}", retries, maxRetries);
						if(retries == maxRetries) {
							Exception ex = new Exception("Max re-tries hit failed");
							throw ex;
						}
					} else {
						isStreamOpen = true;
					}
				}
			}
			
			//Document doc = Jsoup.parse(new URL(teamListUrlS).openStream(), "UTF-8", teamListUrlS);
			Document doc = Jsoup.parse(teamPage, "UTF-8", teamListUrlS);
			aflPlayers.addAll(extractPlayers(team.getTeamId(), doc));
			
			loggerUtils.log("info", "Seniors added to list");
			
			if(team.getRookieUri() != null && !team.getRookieUri().equals("")) {
				String teamListUrlR = team.getWebsite() + "/" + team.getRookieUri();
				loggerUtils.log("info", "Rookie list URL: {}", teamListUrlS);
				
				doc = Jsoup.parse(new URL(teamListUrlR).openStream(), "UTF-8", teamListUrlR);
				aflPlayers.addAll(extractPlayers(team.getTeamId(), doc));
				
				loggerUtils.log("info", "Rookies added to list");
			} else {
				loggerUtils.log("info", "No rookie list");
			}
		}
		
		loggerUtils.log("info", "Saving players to database ...");
		aflPlayerService.insertAll(aflPlayers, false);
		
		loggerUtils.log("info", "Creating afl-dfl player cross references");
		crossRefAflDflPlayers(aflPlayers);
	}
	
	/*
	private void processTeamsAlt(List<AflTeam> aflTeams) throws Exception {
		
		List<AflPlayer> aflPlayers = new ArrayList<>();
		
		int webdriverTimeout = globalsService.getWebdriverTimeout();
		
		for(AflTeam team : aflTeams) {
			loggerUtils.log("info", "Working on team: {}", team.getTeamId());
			
			
		}
		
	}
	*/
	
	private List<AflPlayer> extractPlayers(String teamId, Document doc) throws Exception {
		
		List<AflPlayer> aflPlayers = new ArrayList<>();
		
		Element playerListTable = doc.getElementById("tlist").getElementsByTag("tbody").get(0);
		
		Elements playerRecs = playerListTable.getElementsByTag("tr");
		for(Element playerRec : playerRecs) {
			
			AflPlayer aflPlayer = new AflPlayer();
			
			Elements playerData = playerRec.getElementsByTag("td");
			
			aflPlayer.setTeamId(teamId);
			aflPlayer.setJumperNo(Integer.parseInt(playerData.get(0).text()));
			aflPlayer.setPlayerId(aflPlayer.getTeamId()+aflPlayer.getJumperNo());
			
			String []name = playerData.get(1).text().split(" ");
			aflPlayer.setFirstName(name[0]);
			aflPlayer.setSecondName(name[1]);
			
			aflPlayer.setHeight(Integer.parseInt(playerData.get(2).text()));
			aflPlayer.setWeight(Integer.parseInt(playerData.get(3).text()));
			aflPlayer.setDob(df.parse(playerData.get(4).text()));
			
			loggerUtils.log("info", "Extraced player data: {}", aflPlayer);
			
			aflPlayers.add(aflPlayer);
		}
		
		return aflPlayers;
	}
	
	private void crossRefAflDflPlayers(List<AflPlayer> aflPlayers) {
		
		Map<String, DflPlayer> dflPlayerCrossRefs = dflPlayerService.getCrossRefPlayers();
		
		for(AflPlayer aflPlayer : aflPlayers) {
			String aflPlayerCrossRef = (aflPlayer.getFirstName() + "-" + aflPlayer.getSecondName() + "-" + globalsService.getAflTeamMap(aflPlayer.getTeamId())).toLowerCase();
			DflPlayer dflPlayer = dflPlayerCrossRefs.get(aflPlayerCrossRef);
			
			if(dflPlayer != null) {
				
				int dflPlayerId = dflPlayer.getPlayerId();
				String aflPlayerId = aflPlayer.getPlayerId();
				
				loggerUtils.log("info", "Matched player - CrossRef: {}, DflPlayerId: {}, AflPlayerId {}", aflPlayerCrossRef, dflPlayerId, aflPlayerId);
				
				dflPlayer.setAflPlayerId(aflPlayerId);
				aflPlayer.setDflPlayerId(dflPlayerId);
				
				dflPlayerService.insert(dflPlayer);
				aflPlayerService.insert(aflPlayer);
				
				dflPlayerCrossRefs.remove(aflPlayerCrossRef);
			}
		}
		
		List<DflUnmatchedPlayer> unmatchedPlayers = new ArrayList<>();
		
		for (Map.Entry<String, DflPlayer> entry : dflPlayerCrossRefs.entrySet()) {
		    String crossRef = entry.getKey();
		    DflPlayer player = entry.getValue();
		    
		    loggerUtils.log("info", "Unmatched player: {}", crossRef);
		    
		    DflUnmatchedPlayer unmatchedPlayer = new DflUnmatchedPlayer();
		    unmatchedPlayer.setPlayerId(player.getPlayerId());
		    unmatchedPlayer.setFirstName(player.getFirstName());
		    unmatchedPlayer.setLastName(player.getLastName());
		    unmatchedPlayer.setInitial(player.getInitial());
		    unmatchedPlayer.setStatus(player.getStatus());
		    unmatchedPlayer.setAflClub(player.getAflClub());
		    unmatchedPlayer.setPosition(player.getPosition());
		    unmatchedPlayer.setFirstYear(player.isFirstYear());
		    
		    unmatchedPlayers.add(unmatchedPlayer);
		}
		
		
		dflUnmatchedPlayerService.insertAll(unmatchedPlayers, false);
		
	}
	
	public static void main(String[] args) {
		
		AflPlayerLoaderHandler aflPlayerLoader = new AflPlayerLoaderHandler();
		aflPlayerLoader.execute();
		
	}
	

}