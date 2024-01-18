package com.cloudcomputing.samza.nycabs;

import org.apache.samza.context.Context;
import org.apache.samza.storage.kv.KeyValueStore;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskCoordinator;
import org.apache.samza.storage.kv.Entry;
import org.apache.samza.storage.kv.KeyValueIterator;

import java.util.*;

/**
 * Consumes the stream of driver location updates and rider cab requests.
 * Outputs a stream which joins these 2 streams and gives a stream of rider to
 * driver matches.
 */
public class DriverMatchTask implements StreamTask, InitableTask {

    /* Define per task state here. (kv stores etc)
       READ Samza API part in Primer to understand how to start
    */
    private KeyValueStore<String, Map<String, String>> KVDrivers;
    
    private String BLOCK_ID = "blockId";
    private String DRIVER_ID = "driverId";
    private String TYPE = "type";
    private String LATITUDE = "latitude";
    private String LONGITUDE = "longitude";
    private String CLIENT_ID = "clientId";
    private String GENDER = "gender";
    private String GENDER_PREF = "gender_preference";
    private String RATING = "rating";
    private String SALARY = "salary";
    private String STATUS = "status";
    private String USER_RATING = "user_rating";
    
    private double MAX_MONEY = 100.0;

    private static final Double DISTANCE_WEIGHT = 0.4;
    private static final Double GENDER_WEIGHT = 0.1;
    private static final Double RATING_WEIGHT = 0.3;
    private static final Double SALARY_WEIGHT = 0.2;  
    
    private static final Double DEFAULT_SCORE = 0.0;
    private static final double DIST_SCORE_BASE = 1.0;
    private static final double MAX_RATING = 5.0;
    private static final double RATING_AVG_FACTOR = 2.0;
    private static final String EMPTY_KEY = "";

    @Override
    @SuppressWarnings("unchecked")
    public void init(Context context) throws Exception {
        // Initialize (maybe the kv stores?)
        KVDrivers = (KeyValueStore<String, Map<String, String>>) context.getTaskContext().getStore("driver-loc");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) {
        /*
        All the messsages are partitioned by blockId, which means the messages
        sharing the same blockId will arrive at the same task, similar to the
        approach that MapReduce sends all the key value pairs with the same key
        into the same reducer.
        */
        String incomingStream = envelope.getSystemStreamPartition().getStream();

        if (incomingStream.equals(DriverMatchConfig.DRIVER_LOC_STREAM.getStream())) {
	    // Handle Driver Location messages
            streamLocation((Map<String, Object>) envelope.getMessage());
            
        } else if (incomingStream.equals(DriverMatchConfig.EVENT_STREAM.getStream())) {
            
	    // Handle Event messages
            streamEvent((Map<String, Object>) envelope.getMessage(), collector);
            
        } else {
            throw new IllegalStateException("Unexpected input stream: " + envelope.getSystemStreamPartition());
        }
    }
    

    /**
     * Fetch driver information from the KeyValueStore based on blockId and driverId.
     *
     * @param blockId  The blockId of the driver.
     * @param driverId The driverId of the driver.
     * @return A map containing driver information, or an empty LinkedHashMap if not found.
     */
    private Map<String, String> fetchDriverInfo(Integer blockId, Integer driverId) {
        Map<String, String> driver = KVDrivers.get(blockId.toString() + ":" + driverId.toString());
        if (driver == null) {
            return new LinkedHashMap<>();
        } else {
            return driver;
        }
    }

    
    /**
     * Process the driver location stream.
     * Input: driver location stream map message.
     *
     * @param driverLoc_entry The map message from the driver location stream.
     */
    private void streamLocation(Map<String, Object> driverLoc_entry) {
        int blockID = (int) driverLoc_entry.get(BLOCK_ID);
        Integer driverID = (int) driverLoc_entry.get(DRIVER_ID);
        Double latitude = (Double) driverLoc_entry.get(LATITUDE);
        Double longitude = (Double) driverLoc_entry.get(LONGITUDE);
    
        // Retrieve KV entry for the driver
        String driverKey = (Integer.toString(blockID) + ":" + driverID.toString());
        Map<String, String> driverInfo = KVDrivers.get(driverKey);
    
        // If the driver is not already in the store, initialize their info
        if (driverInfo == null) {
            driverInfo = new HashMap<>();
            driverInfo.put(DRIVER_ID, driverID.toString());
            driverInfo.put(BLOCK_ID, (Integer.toString(blockID)));
            // Initialize other necessary fields with default values
            driverInfo.put(STATUS, "AVAILABLE"); // Default status
            driverInfo.put(RATING, "0.0"); // Default rating
            driverInfo.put(SALARY, "0"); // Default salary
        }
    
        // Update the driver's location
        driverInfo.put(LATITUDE, latitude.toString());
        driverInfo.put(LONGITUDE, longitude.toString());
    
        // Put driver information back into the KV store
        KVDrivers.put(driverKey, driverInfo);
    }
    
    /**
     * Process the event stream with 11 key-value pairs.
     *
     * @param entry     The map message from the event stream.
     * @param collector The message collector for sending output.
     */
    private void streamEvent(Map<String, Object> entry, MessageCollector collector) {
        String type = (String) entry.get(TYPE);
    
        switch (type) {
            case "RIDE_REQUEST":
                handleRideRequest(entry, collector);
                break;
            case "RIDE_COMPLETE":
                handleRideUpdate(entry, type);
                break;
            case "ENTERING_BLOCK":
                handleBlockUpdate(entry, type);
                break;
            default:
                Integer driverID = (Integer) entry.get(DRIVER_ID);
                KVDrivers.delete(driverID.toString());
                break;
        }
    }

    /**
     * Handles a ride request by selecting the best available driver.
     * This method processes the ride request information, searches for available drivers,
     * calculates a match score based on various criteria like location, gender preference,
     * rating, and salary, and then selects the best driver.
     *
     * It then updates the store by removing the selected driver and sends out the
     * result via a message collector.
     *
     * @param entry The map message containing ride request details like latitude, longitude, 
     *              block ID, client ID, and gender preference.
     * @param collector The message collector used to send out the result.
     */
    private void handleRideRequest(Map<String, Object> entry, MessageCollector collector) {
        // Validate all required fields in entry
        if (entry == null || !entry.containsKey(LATITUDE) || !entry.containsKey(LONGITUDE) ||
            !entry.containsKey(BLOCK_ID) || !entry.containsKey(CLIENT_ID) || !entry.containsKey(GENDER_PREF)) {
            return;
        }
    
        Double rideLatitude = (Double) entry.get(LATITUDE);
        Double rideLongitude = (Double) entry.get(LONGITUDE);
        Integer blockID = (Integer) entry.get(BLOCK_ID);
        Integer clientID = (Integer) entry.get(CLIENT_ID);
        String genderPref = (String) entry.get(GENDER_PREF);

        if (rideLatitude == null || rideLongitude == null || blockID == null || clientID == null || genderPref == null) {
            return;
        }

        String bestEntryKey = EMPTY_KEY;
        String bestDriverID = EMPTY_KEY;
        Double bestDriverScore = DEFAULT_SCORE;
    
        KeyValueIterator<String, Map<String, String>> driverIter = KVDrivers.range(blockID.toString() + ":", blockID.toString() + ";");
    
        while (driverIter.hasNext()) {
            Entry<String, Map<String, String>> record = driverIter.next();
            Map<String, String> driverEntry = record.getValue();
            String entryKey = record.getKey();
    
            // Check if driverEntry is valid and driver is available
            if (driverEntry == null || !driverEntry.containsKey(STATUS) || !driverEntry.get(STATUS).equals("AVAILABLE")) {
                continue;
            }
    
            Double driverLatitude = parseDouble(driverEntry.get(LATITUDE));
            Double driverLongitude = parseDouble(driverEntry.get(LONGITUDE));
            Double rating = parseDouble(driverEntry.get(RATING));
            Double salary = parseDouble(driverEntry.get(SALARY));
    
            // Ensure all required values are non-null and valid
            if (driverLatitude == null || driverLongitude == null || rating == null || salary == null) {
                continue;
            }
    
            double distX = Math.pow(driverLatitude - rideLatitude, 2);
            double distY = Math.pow(driverLongitude - rideLongitude, 2);
            double distance = Math.sqrt(distX + distY);
            String driverGender = driverEntry.get(GENDER);
            Double genderScore = DEFAULT_SCORE;
            if (driverGender != null && (driverGender.equals(genderPref) || driverGender.equals("N"))) {
                genderScore = DIST_SCORE_BASE;
            }
            Double distScore = DIST_SCORE_BASE * Math.pow(Math.E, -DIST_SCORE_BASE * distance);
            double matchScore = DISTANCE_WEIGHT * distScore + GENDER_WEIGHT * genderScore + RATING_WEIGHT * (rating / MAX_RATING) + SALARY_WEIGHT * (DIST_SCORE_BASE - (salary / MAX_MONEY));
    
            if (matchScore > bestDriverScore) {
                bestDriverScore = matchScore;
                bestEntryKey = entryKey;
                bestDriverID = entryKey.split(":")[1];
            }
        }
        driverIter.close();
    
        if (!bestEntryKey.equals(EMPTY_KEY)) {
            KVDrivers.delete(bestEntryKey);
    
            Map<String, String> output = new HashMap<>();
            output.put(DRIVER_ID, bestDriverID);
            output.put(CLIENT_ID, clientID.toString());
            collector.send(new OutgoingMessageEnvelope(DriverMatchConfig.MATCH_STREAM, output));
        }
    }
    
    /**
     * Handles updates to a ride, specifically updating driver information.
     * This method processes the ride update information, retrieves and updates the driver's information 
     * in the store based on the updated ride details. It includes updating the driver's location,
     * gender, salary, and rating.
     *
     * @param entry The map message containing updated ride details like latitude, longitude, 
     *              block ID, driver ID, gender, rating, salary, and user rating.
     * @param type  The type of update being handled (not used in the current implementation).
     */
    private void handleRideUpdate(Map<String, Object> entry, String type) {
        Double rideLatitude = (Double) entry.get(LATITUDE);
        Double rideLongitude = (Double) entry.get(LONGITUDE);
        Integer blockID = (Integer) entry.get(BLOCK_ID);
        Integer driverID = (Integer) entry.get(DRIVER_ID);

        // Obtain information for the driver from the store (for updating)
        Map<String, String> driverInfo = fetchDriverInfo(blockID, driverID);
        // Initialize new driver if not already in KV store
        if (driverInfo == null) {
            driverInfo = new HashMap<>();
            driverInfo.put(DRIVER_ID, driverID.toString());
            driverInfo.put(BLOCK_ID, blockID.toString());
            // Initialize other necessary fields with default values
            driverInfo.put(STATUS, "AVAILABLE"); // Default status
            driverInfo.put(RATING, "0.0"); // Default rating
            driverInfo.put(SALARY, "0"); // Default salary
        }
        // Retrieve more driver information
        String gender = (String) entry.get(GENDER);
        Double rating = (Double) entry.get(RATING);
        Integer salary = (Integer) entry.get(SALARY);

        // Update necessary information (location)
        driverInfo.put(LONGITUDE, rideLongitude.toString());
        driverInfo.put(LATITUDE, rideLatitude.toString());
        driverInfo.put(GENDER, gender);
        driverInfo.put(SALARY, salary.toString());
        Double userRating = (Double) entry.get(USER_RATING);

        // Update the score
        Double newRating = (rating + userRating) / RATING_AVG_FACTOR;
        driverInfo.put(STATUS, "AVAILABLE");
        driverInfo.put(RATING, newRating.toString());
        KVDrivers.put((Integer.toString(blockID) + ":" + driverID.toString()), driverInfo);

    }

    /**
     * Handles updates to a driver's block, specifically updating driver information based on block changes.
     * This method processes the block update information, retrieves and updates the driver's information 
     * in the store based on the updated block details. It updates the driver's location, gender,
     * salary, and status.
     *
     * @param entry The map message containing updated block details like latitude, longitude,
     *              block ID, driver ID, gender, salary, and status.
     * @param type  The type of update being handled (not used in the current implementation).
     */
    private void handleBlockUpdate(Map<String, Object> entry, String type) {
        Double rideLatitude = (Double) entry.get(LATITUDE);
        Double rideLongitude = (Double) entry.get(LONGITUDE);
        Integer blockID = (Integer) entry.get(BLOCK_ID);
        Integer driverID = (Integer) entry.get(DRIVER_ID);

        // Obtain information for the driver from the store (for updating)
        Map<String, String> driverInfo = fetchDriverInfo(blockID, driverID);
        // Initialize new driver if not already in KV store
        if (driverInfo == null) {
            driverInfo = new HashMap<>();
            driverInfo.put(DRIVER_ID, driverID.toString());
            driverInfo.put(BLOCK_ID, blockID.toString());
            // Initialize other necessary fields with default values
            driverInfo.put(STATUS, "AVAILABLE"); // Default status
            driverInfo.put(RATING, "0.0"); // Default rating
            driverInfo.put(SALARY, "0"); // Default salary
        }
        // Retrieve more driver information
        String gender = (String) entry.get(GENDER);
        Double rating = (Double) entry.get(RATING);
        Integer salary = (Integer) entry.get(SALARY);

        // Update necessary information (location)
        driverInfo.put(LONGITUDE, rideLongitude.toString());
        driverInfo.put(LATITUDE, rideLatitude.toString());
        driverInfo.put(GENDER, gender);
        driverInfo.put(SALARY, salary.toString());

        // ENTERING BLOCK information
        String status = (String) entry.get(STATUS);
        driverInfo.put(STATUS, status);
        driverInfo.put(RATING, rating.toString());
        KVDrivers.put((Integer.toString(blockID) + ":" + driverID.toString()), driverInfo);
    }

    private Double parseDouble(String value) {
        try {
            return value != null ? Double.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}