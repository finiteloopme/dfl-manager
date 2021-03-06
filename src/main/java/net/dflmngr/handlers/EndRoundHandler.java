package net.dflmngr.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

//import net.dflmngr.jndi.JndiProvider;
import net.dflmngr.logging.LoggingUtils;
import net.dflmngr.model.entity.DflAdamGoodes;
import net.dflmngr.model.entity.DflBest22;
import net.dflmngr.model.entity.DflCallumChambers;
import net.dflmngr.model.entity.DflMatthewAllen;
import net.dflmngr.model.entity.DflPlayer;
import net.dflmngr.model.entity.DflTeam;
import net.dflmngr.model.entity.DflTeamPlayer;
import net.dflmngr.model.service.DflBest22Service;
import net.dflmngr.model.service.DflMatthewAllenService;
import net.dflmngr.model.service.DflPlayerService;
import net.dflmngr.model.service.DflTeamPlayerService;
import net.dflmngr.model.service.DflTeamService;
import net.dflmngr.model.service.GlobalsService;
import net.dflmngr.model.service.impl.DflBest22ServiceImpl;
import net.dflmngr.model.service.impl.DflMatthewAllenServiceImpl;
import net.dflmngr.model.service.impl.DflPlayerServiceImpl;
import net.dflmngr.model.service.impl.DflTeamPlayerServiceImpl;
import net.dflmngr.model.service.impl.DflTeamServiceImpl;
import net.dflmngr.model.service.impl.GlobalsServiceImpl;
import net.dflmngr.utils.EmailUtils;

public class EndRoundHandler {
	private LoggingUtils loggerUtils;
	
	boolean isExecutable;
	
	String defaultMdcKey = "batch.name";
	String defaultLoggerName = "batch-logger";
	String defaultLogfile = "EndRound";
	
	String mdcKey;
	String loggerName;
	String logfile;
	
	DflMatthewAllenService 	dflMatthewAllenService;
	GlobalsService globalsService;
	DflPlayerService dflPlayerService;
	DflTeamPlayerService dflTeamPlayerService;
	DflTeamService dflTeamService;
	DflBest22Service dflBest22Service;
	
	String emailOverride;

	public EndRoundHandler() {
		dflMatthewAllenService = new DflMatthewAllenServiceImpl();
		globalsService = new GlobalsServiceImpl();
		dflPlayerService = new DflPlayerServiceImpl();
		dflTeamPlayerService = new DflTeamPlayerServiceImpl();
		dflTeamService = new DflTeamServiceImpl();
		dflBest22Service = new DflBest22ServiceImpl();
	}
	
	public void configureLogging(String mdcKey, String loggerName, String logfile) {
		//loggerUtils = new LoggingUtils(loggerName, mdcKey, logfile);
		loggerUtils = new LoggingUtils(logfile);
		this.mdcKey = mdcKey;
		this.loggerName = loggerName;
		this.logfile = logfile;
		isExecutable = true;
	}
	
	public void execute(int round, String emailOverride) {
		
		try{
			if(!isExecutable) {
				configureLogging(defaultMdcKey, defaultLoggerName, defaultLogfile);
				loggerUtils.log("info", "Default logging configured");
			}
			
			loggerUtils.log("info", "Executing end round for round={}",  round);
			
			if(emailOverride != null && !emailOverride.equals("")) {
				loggerUtils.log("info", "Overriding email with: {}", emailOverride);
				this.emailOverride = emailOverride;
			}
			
			MatthewAllenHandler matthewAllenHandler = new MatthewAllenHandler();
			matthewAllenHandler.configureLogging(mdcKey, loggerName, logfile);
			matthewAllenHandler.execute(round);
			List<DflMatthewAllen> matthewAllenStandings = dflMatthewAllenService.getForRound(round);
			Collections.sort(matthewAllenStandings, Collections.reverseOrder());
			
			AdamGoodesHandler adamGoodesHandler = new AdamGoodesHandler();
			adamGoodesHandler.configureLogging(mdcKey, loggerName, logfile);
			adamGoodesHandler.execute(round);
			List<DflAdamGoodes> adamGoodesStandings = adamGoodesHandler.getMedalStandings();
			List<DflAdamGoodes> topFirstYears = adamGoodesHandler.getTopFirstYears();
			
			CallumChambersHandler callumChambersHandler = new CallumChambersHandler();
			callumChambersHandler.configureLogging(mdcKey, loggerName, logfile);
			callumChambersHandler.execute(round);
			List<DflCallumChambers> callumChambersStandings = callumChambersHandler.getMedalStandings();
			
			Best22Handler best22Handler = new Best22Handler();
			best22Handler.configureLogging(mdcKey, loggerName, logfile);
			best22Handler.execute(round);
			List<DflBest22> best22 = dflBest22Service.getForRound(round);
			
			boolean sendBest22 = false;
			if(round == 6 || round == 12 || round == 18) {
				sendBest22 = true;
			}
			
			if(!globalsService.getSendMedalReports(round)) {
				createAndSendEmail(round, matthewAllenStandings, adamGoodesStandings, topFirstYears, callumChambersStandings, best22, sendBest22);
			}
			
			globalsService.setCurrentRound(round+1);
			
			dflMatthewAllenService.close();
			globalsService.close();
			dflPlayerService.close();
			dflTeamPlayerService.close();
			dflTeamService.close();	

			loggerUtils.log("info", "End round completed");
			
		} catch (Exception ex) {
			loggerUtils.log("error", "Error in ... ", ex);
		}
		
	}
	
	private void createAndSendEmail(int round, List<DflMatthewAllen> matthewAllenStandings, List<DflAdamGoodes> adamGoodesStandings,
			List<DflAdamGoodes> topFirstYears, List<DflCallumChambers> callumChambersStandings, List<DflBest22> best22, boolean sendBest22) throws Exception {
		
		String dflMngrEmail = globalsService.getEmailConfig().get("dflmngrEmailAddr");
		
		String subject = "End of Round " + round + ", Current Medal Standings";
		
		String body = "Matthew Allen Medal Top 5:\n";
		for(int i = 0; i < 5; i++) {
			DflMatthewAllen standing = matthewAllenStandings.get(i);
			
			DflPlayer player = dflPlayerService.get(standing.getPlayerId());
			DflTeamPlayer teamPlayer = dflTeamPlayerService.get(standing.getPlayerId());
			DflTeam team = dflTeamService.get(teamPlayer.getTeamCode());
			
			body = body + (i+1) + ". " + standing.getPlayerId() + " " + player.getFirstName() + " " + player.getLastName() + ", " + team.getName() + " - " + standing.getTotal() + "\n";
		}
		
		body = body + "\nAdam Goodes Medal Top 5:\n";
		for(int i = 0; i < 5; i++) {
			if(i < adamGoodesStandings.size()) {
				DflAdamGoodes standing = adamGoodesStandings.get(i);
				
				DflPlayer player = dflPlayerService.get(standing.getPlayerId());
				DflTeamPlayer teamPlayer = dflTeamPlayerService.get(standing.getPlayerId());
				DflTeam team = dflTeamService.get(teamPlayer.getTeamCode());
				
				body = body + (i+1) + ". " + standing.getPlayerId() + " " + player.getFirstName() + " " + player.getLastName() + ", " + team.getName() + " - " + standing.getTotalScore() + "\n";
			}
		}
		
		body = body + "\nFirst Year Player Top 5:\n";
		for(int i = 0; i < 5; i++) {
			if(i < topFirstYears.size()) {
				DflAdamGoodes standing = topFirstYears.get(i);
				
				DflPlayer player = dflPlayerService.get(standing.getPlayerId());
				DflTeamPlayer teamPlayer = dflTeamPlayerService.get(standing.getPlayerId());
				
				if(teamPlayer != null) {
					DflTeam team = dflTeamService.get(teamPlayer.getTeamCode());
					body = body + (i+1) + ". " + standing.getPlayerId() + " " + player.getFirstName() + " " + player.getLastName() + ", " + team.getName() + " - " + standing.getTotalScore() + "\n";
				} else {
					body = body + (i+1) + ". " + standing.getPlayerId() + " " + player.getFirstName() + " " + player.getLastName() + ", Not Drafted - " + standing.getTotalScore() + "\n";
				}	
			}
		}
		
		body = body + "\nCallum Chambers Medal Top 5:\n";
		for(int i = 0; i < 5; i++) {
			DflCallumChambers standing = callumChambersStandings.get(i);
			
			DflPlayer player = dflPlayerService.get(standing.getPlayerId());
			DflTeamPlayer teamPlayer = dflTeamPlayerService.get(standing.getPlayerId());
			DflTeam team = dflTeamService.get(teamPlayer.getTeamCode());
			
			body = body + (i+1) + ". " + standing.getPlayerId() + " " + player.getFirstName() + " " + player.getLastName() + ", " + team.getName() + " - " + standing.getTotalScore() + "\n";
		}
		
		if(sendBest22) {
			body = body + "\nDFL Best 22 after Round " + round + "\n";
			
			List<String> ff = new  ArrayList<>();
			List<String> fwd = new  ArrayList<>();
			List<String> rck = new  ArrayList<>();
			List<String> mid = new  ArrayList<>();
			List<String> fb = new  ArrayList<>();
			List<String> def = new  ArrayList<>();
			List<String> bench = new  ArrayList<>();
			
			for(DflBest22 best22Player : best22) {
				DflPlayer player = dflPlayerService.get(best22Player.getPlayerId());
				DflTeamPlayer teamPlayer = dflTeamPlayerService.get(best22Player.getPlayerId());
				DflTeam team = dflTeamService.get(teamPlayer.getTeamCode());
				if(best22Player.isBench()) {
					bench.add(player.getPlayerId() + " " + player.getFirstName() + " " + player.getLastName() + " " + team.getName() + " - " + best22Player.getScore());
				} else {
					String displayString = player.getPlayerId() + " " + player.getFirstName() + " " + player.getLastName() + " " + team.getName() + " - " + best22Player.getScore();
					switch(player.getPosition()) {
						case "FF": ff.add(displayString);
						case "Fwd": fwd.add(displayString);
						case "Rck": rck.add(displayString);
						case "Mid": mid.add(displayString);
						case "FB": fb.add(displayString);
						case "Def": def.add(displayString);
					}
				}
			}
			
			body = body + "FB:\n";
			for(String displayString : fb) {
				body = body + displayString + "\n";
			}
			body = body + "Def:\n";
			for(String displayString : def) {
				body = body + displayString + "\n";
			}
			body = body + "Rck:\n";
			for(String displayString : rck) {
				body = body + displayString + "\n";
			}
			body = body + "Mid:\n";
			for(String displayString : mid) {
				body = body + displayString + "\n";
			}
			body = body + "Fwd:\n";
			for(String displayString : fwd) {
				body = body + displayString + "\n";
			}
			body = body + "FB:\n";
			for(String displayString : fb) {
				body = body + displayString + "\n";
			}
			body = body + "Bench:\n";
			for(String displayString : bench) {
				body = body + displayString + "\n";
			}
		}
				
		body = body + "\nDFL Manager Admin";
		
		List<String> to = new ArrayList<>();

		if(emailOverride != null && !emailOverride.equals("")) {
			to.add(emailOverride);
		} else {
			List<DflTeam> teams = dflTeamService.findAll();
			for(DflTeam team : teams) {
				to.add(team.getCoachEmail());
			}
		}
		
		loggerUtils.log("info", "Emailing to={};", to);
		EmailUtils.sendTextEmail(to, dflMngrEmail, subject, body, null);
	}
	
	
	public static void main(String[] args) {
		
		Options options = new Options();
		Option roundOpt  = Option.builder("r").argName("round").hasArg().desc("round to run on").type(Number.class).required().build();
		Option emailOPt = Option.builder("e").argName("email").hasArg().desc("override email distribution").build();
		options.addOption(roundOpt);
		options.addOption(emailOPt);
		
		try {
			int round = 0;
			String email = null;
						
			CommandLineParser parser = new DefaultParser();
			CommandLine cli = parser.parse(options, args);
			
			round = ((Number)cli.getParsedOptionValue("r")).intValue();
			
			if(cli.hasOption("e")) {
				email = cli.getOptionValue("e");
			}
		
			//JndiProvider.bind();
			
			EndRoundHandler endRound = new EndRoundHandler();
			endRound.configureLogging("batch.name", "batch-logger", ("EndRound_R" + round));
			endRound.execute(round, email);
		} catch (ParseException ex) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "MatthewAllenHandler", options );
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
