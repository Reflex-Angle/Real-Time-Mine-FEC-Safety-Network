package org.fog.entities;

import java.util.ArrayList;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.ChemicalInfoProcessor;
import org.fog.application.GasInfoProcessor;
import org.fog.application.SurroundingInfoProcessor;
import org.fog.utils.*;
import org.fog.utils.distribution.DeterministicDistribution;
import org.fog.utils.distribution.Distribution;
import java.util.Random;
import org.fog.entities.SensorType;

public class Sensor extends SimEntity{
	
	private int gatewayDeviceId;
	private GeoLocation geoLocation;
	private long outputSize;
	private String appId;
	private int userId;
	private String tupleType;
	private String sensorName;
	private String destModuleName;
	private int controllerId;
	private Application app;
	private double latency;
	private SensorType sensorType;
    private double sensorValue;
    private double minValue; // Minimum range for random value
    private double maxValue; // Maximum range for random value
    private Random random = new Random(); // Random instance for generating values

	private int transmissionStartDelay = Config.TRANSMISSION_START_DELAY;
	Distribution transmitDistribution = new DeterministicDistribution(5); // Emits every 5 seconds

	public Sensor(String name, int userId, String appId,
            SensorType sensorType, String tupleType, String destModuleName, double minValue, double maxValue) {
		super(name);
		this.sensorType = sensorType;
		this.setAppId(appId);
		this.outputSize = 3;
		this.setTransmitDistribution(transmitDistribution);
		setUserId(userId);
		setDestModuleName(destModuleName);
		setTupleType(tupleType);
		setSensorName(sensorName);
		setLatency(latency);
		this.minValue = minValue;
        this.maxValue = maxValue;
	}
	
	public Sensor(String name, int userId, String appId, int gatewayDeviceId, double latency, GeoLocation geoLocation, 
			Distribution transmitDistribution, String tupleType) {
		super(name);
		this.setAppId(appId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		this.outputSize = 3;
		this.setTransmitDistribution(transmitDistribution);
		setUserId(userId);
		setTupleType(tupleType);
		setSensorName(sensorName);
		setLatency(latency);
	}
	
	/**
	 * This constructor is called from the code that generates PhysicalTopology from JSON
	 * @param name
	 * @param tupleType
	 * @param string 
	 * @param userId
	 * @param appId
	 * @param transmitDistribution
	 */
	public Sensor(String name, String tupleType, int userId, String appId, Distribution transmitDistribution) {
		super(name);
		this.setAppId(appId);
		this.setTransmitDistribution(transmitDistribution);
		setTupleType(tupleType);
		setSensorName(tupleType);
		setUserId(userId);
	}
	private void generateSensorValue() {
        sensorValue = minValue + (maxValue - minValue) * random.nextDouble();
        if(getTupleType().equals("GAS")) {
        	GasInfoProcessor g1=new GasInfoProcessor(90.0,26,appId,userId);
        	g1.processGasReading(sensorValue,26);
        	
        }
        else if (getTupleType().equals("CH")) {
        	ChemicalInfoProcessor c1=new ChemicalInfoProcessor(29,26,appId,userId);
        	c1.processChemReading(sensorValue,26);
        	
        }
        else{
        	SurroundingInfoProcessor s1=new SurroundingInfoProcessor(4.5,26,appId,userId);
        	s1.processEnvReading(sensorValue,26);
        	
        }
    }
	public void transmit(){
		generateSensorValue();
		AppEdge _edge = null;
		for(AppEdge edge : getApp().getEdges()){
			if(edge.getSource().equals(getTupleType()))
				_edge = edge;
		}
		if (_edge == null) {
	        System.out.println("no matching edge found for "+getTupleType());
	        return; // Exit the method if no edge is found
	    }
		long cpuLength = (long) _edge.getTupleCpuLength();
		long nwLength = (long) _edge.getTupleNwLength();
		
		Tuple tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, cpuLength, 1, nwLength, outputSize, 
				new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
		tuple.setUserId(getUserId());
		tuple.setTupleType(getTupleType());
		tuple.setSensorValue(sensorValue);
		tuple.setDestModuleName(_edge.getDestination());
		tuple.setSrcModuleName(getSensorName());
		Logger.debug(getName(), "Sending tuple with tupleId = "+tuple.getCloudletId());

		tuple.setDestinationDeviceId(getGatewayDeviceId());

		int actualTupleId = updateTimings(getSensorName(), tuple.getDestModuleName());
		tuple.setActualTupleId(actualTupleId);
		
		send(gatewayDeviceId, getLatency(), FogEvents.TUPLE_ARRIVAL,tuple);
	}
	
	protected int updateTimings(String src, String dest){
		Application application = getApp();
		for(AppLoop loop : application.getLoops()){
			if(loop.hasEdge(src, dest)){
				
				int tupleId = TimeKeeper.getInstance().getUniqueId();
				if(!TimeKeeper.getInstance().getLoopIdToTupleIds().containsKey(loop.getLoopId()))
					TimeKeeper.getInstance().getLoopIdToTupleIds().put(loop.getLoopId(), new ArrayList<Integer>());
				TimeKeeper.getInstance().getLoopIdToTupleIds().get(loop.getLoopId()).add(tupleId);
				TimeKeeper.getInstance().getEmitTimes().put(tupleId, CloudSim.clock());
				return tupleId;
			}
		}
		return -1;
	}
	
	@Override
	public void startEntity() {
		if (getTransmitDistribution() == null) {
	        Logger.error(getName(), "Transmit distribution is null");
	    }
	    if (getId() == -1) {
	        Logger.error(getName(), "ID is not set");
	    }
		send(gatewayDeviceId, CloudSim.getMinTimeBetweenEvents(), FogEvents.SENSOR_JOINED, geoLocation);
		send(getId(), getTransmitDistribution().getNextValue() + transmissionStartDelay, FogEvents.EMIT_TUPLE);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.TUPLE_ACK:
			//transmit(transmitDistribution.getNextValue());
			break;
		case FogEvents.EMIT_TUPLE:
			transmit();
			send(getId(), getTransmitDistribution().getNextValue(), FogEvents.EMIT_TUPLE);
			break;
		}
			
	}

	@Override
	public void shutdownEntity() {
		
	}

	public int getGatewayDeviceId() {
		return gatewayDeviceId;
	}

	public void setGatewayDeviceId(int gatewayDeviceId) {
		this.gatewayDeviceId = gatewayDeviceId;
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getTupleType() {
		return tupleType;
	}

	public void setTupleType(String tupleType) {
		this.tupleType = tupleType;
	}

	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getDestModuleName() {
		return destModuleName;
	}

	public void setDestModuleName(String destModuleName) {
		this.destModuleName = destModuleName;
	}

	public Distribution getTransmitDistribution() {
		return transmitDistribution;
	}

	public void setTransmitDistribution(Distribution transmitDistribution) {
		this.transmitDistribution = transmitDistribution;
	}

	public int getControllerId() {
		return controllerId;
	}

	public void setControllerId(int controllerId) {
		this.controllerId = controllerId;
	}

	public Application getApp() {
		return app;
	}

	public void setApp(Application app) {
		this.app = app;
	}

	public Double getLatency() {
		return latency;
	}

	public void setLatency(Double latency) {
		this.latency = latency;
	}

	protected long getOutputSize(){return this.outputSize;}

	public void setTransmissionStartDelay(int transmissionStartDelay) {
		this.transmissionStartDelay = transmissionStartDelay;
	}

	public int getTransmissionStartDelay() {
		return transmissionStartDelay;
	}

}
