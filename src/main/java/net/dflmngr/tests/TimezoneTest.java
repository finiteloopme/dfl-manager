package net.dflmngr.tests;

import java.time.ZoneId;
import java.util.TreeSet;
 
/**
 * Displays available time zones (zoneIds)
 *
 * @author www.javadb.com
 */
public class TimezoneTest {
    
    public static void main(String[] args) {
        
        TreeSet<String> sortedZones = new TreeSet<>(ZoneId.getAvailableZoneIds());
        
        System.out.println("Number of zones: " + sortedZones.size());
        System.out.println("");
 
        for (String zone : sortedZones) {
            System.out.println(zone);
        }
        
        //Or if you want to use functional operations
        /*
        sortedZones.stream().forEach((zone) -> {
            System.out.println(zone);
        });
        */
    }
}
