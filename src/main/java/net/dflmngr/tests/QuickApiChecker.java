package net.dflmngr.tests;

/*
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JsonObject;
import org.json.simple.Jsoner;
*/

public class QuickApiChecker {

	/*
	public void callHerokuApi() {
		String fullStatsUrl = "http:/www.afl.com.au/match-centre/2017/5/port-v-carl";
		System.out.println("AFL stats URL: " + fullStatsUrl);

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
		postData.put("command", "bin/run_raw_stats_downloader.sh " + 5 + " " + " " + "PORT" + " " + "CARL" + " " + fullStatsUrl);
		postData.put("size", "hobby");
		postData.put("type", "run");

		con.setDoOutput(true);

		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.writeBytes(postData.toJson());
		out.flush();
		out.close();

		int responseCode = con.getResponseCode();

		System.out.println("Spawning Dyno to process fixture: " + postData.toJson());
		System.out.println("Response Code: " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String output;
		StringBuffer response = new StringBuffer();

		while ((output = in.readLine()) != null) {
			response.append(output);
		}
		in.close();
		
		System.out.println("Response data: " + response.toString());
		
		JsonObject responseData = Jsoner.deserialize(response.toString(), new JsonObject());
	}
	*/
	
}
