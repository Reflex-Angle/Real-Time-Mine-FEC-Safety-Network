package org.fog.application;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;

public class SurroundingInfoProcessor {
	public class java {

	}

	private double threshold;
    private int responseModuleId;
    private String appId;
    private int userId;

    public SurroundingInfoProcessor(double threshold, int responseModuleId, String appId, int userId) {
        this.threshold = threshold;
        this.responseModuleId = responseModuleId;
        this.appId = appId;
        this.userId = userId;
    }

    public boolean processEnvReading(double EnvReading,int responseModuleId) {
        boolean b=EnvReading > threshold;
        if(b) {
        	sendAlert(responseModuleId);
        	System.out.println("SR: "+EnvReading);
        }
        return b;
    }

    public void sendAlert(int sourceDeviceId) {
        System.out.println("ALERT: Surrounding environment unstable! Sending alert to response module.");

        // Create the alert tuple
        Tuple alertTuple = new Tuple(
            appId,                      // application ID
            CloudSim.getEntityId("TupleAlert"), // cloudlet ID (unique identifier)
            Tuple.UP,                   // direction of the tuple
            1000,                       // cloudlet length (arbitrary value)
            1,                          // PEs (number of processing elements required)
            1000,                       // cloudlet file size (arbitrary value)
            1000,                       // cloudlet output size (arbitrary value)
            new UtilizationModelFull(), // CPU utilization model
            new UtilizationModelFull(), // RAM utilization model
            new UtilizationModelFull()  // BW utilization model
        );

        // Set additional attributes for the alert
        alertTuple.setTupleType("SrAlert");            // Unique tuple type
        alertTuple.setSourceDeviceId(sourceDeviceId);   // ID of the source device
        alertTuple.setDestModuleName("response-module"); // Name of destination module
        alertTuple.setUserId(userId);                   // User ID associated with this alert

        // Send the tuple as an event to the response module
        CloudSim.send(sourceDeviceId, responseModuleId, 0, FogEvents.TUPLE_ARRIVAL, alertTuple);
    }
}
