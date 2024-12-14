import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Actuator;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementMapping;
import org.fog.application.Application;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.utils.FogLinearPowerModel;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerModel;
import org.cloudbus.cloudsim.Host;

import java.util.ArrayList;
import java.util.List;

public class MineSafetyMonitoring {
    
    public static void main(String[] args) {
        Log.printLine("Starting Mine Safety Monitoring...");

        try {
            int numOfFogDevices = 3; // Define number of fog nodes for sensors
            List<FogDevice> fogDevices = createFogDevices(numOfFogDevices);
            Application app = createApplication("MineSafetyApp");

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
            
            // Assign modules to devices
            moduleMapping.addModuleToDevice("master-module", "router");
            moduleMapping.addModuleToDevice("response-module", "proxy-server");
            
            for (FogDevice device : fogDevices) {
                if (device.getName().startsWith("g")) {
                    moduleMapping.addModuleToDevice("gasinfo-module", device.getName());
                } else if (device.getName().startsWith("c")) {
                    moduleMapping.addModuleToDevice("chinfo-module", device.getName());
                } else if (device.getName().startsWith("s")) {
                    moduleMapping.addModuleToDevice("srinfo-module", device.getName());
                }
            }

            Controller controller = new Controller("master-controller", fogDevices, new ArrayList<>(), moduleMapping);
            controller.submitApplication(app, 0, new ModulePlacementMapping(fogDevices, app, moduleMapping));

            Log.printLine("Mine Safety Monitoring finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("An error occurred.");
        }
    }

    private static List<FogDevice> createFogDevices(int numOfFogDevices) {
        List<FogDevice> fogDevices = new ArrayList<>();

        FogDevice router = createFogDevice("router", 10000, 4000, 10000, 10000, 0, 10);
        fogDevices.add(router);

        FogDevice proxyServer = createFogDevice("proxy-server", 8000, 4000, 10000, 10000, 1, 10);
        fogDevices.add(proxyServer);

        // Create fog nodes for each sensor type
        for (int i = 0; i < numOfFogDevices; i++) {
            String name;
            if (i == 0) {
                name = "gas-fog";
            } else if (i == 1) {
                name = "chem-fog";
            } else {
                name = "sr-fog";
            }
            FogDevice fogDevice = createFogDevice(name, 5000, 1000, 1000, 1000, 2, 5);
            fogDevices.add(fogDevice);
        }

        return fogDevices;
    }

    private static FogDevice createFogDevice(String name, long mips, int ram, long upBw, long downBw, int level, double ratePerMips) {
        List<PowerHost> hostList = new ArrayList<>();
        List<Host> peList = new ArrayList<>();
        
        PowerHost host = new PowerHost(
            FogUtils.generateEntityId(),
            new RamProvisionerSimple(ram),
            new BwProvisionerSimple(10000),
            1000000,
            peList,
            new VmSchedulerTimeShared(peList),
            new FogLinearPowerModel(100, 100)
        );

        FogDevice device = new FogDevice(name, new FogDeviceCharacteristics("x86", "Linux", "Xen", host, 10, 3, 0.01, 0.01, ratePerMips),
                new AppModuleAllocationPolicy(hostList), new LinkedList<Storage>(), upBw, downBw, 0, ratePerMips);
        device.setLevel(level);

        return device;
    }

    private static Application createApplication(String appId) {
        Application app = Application.createApplication(appId, 1);

        app.addAppModule("master-module", 10);
        app.addAppModule("response-module", 10);
        app.addAppModule("gasinfo-module", 10);
        app.addAppModule("chinfo-module", 10);
        app.addAppModule("srinfo-module", 10);

        app.addAppEdge("gas-sensor", "gasinfo-module", 1000, 200, "GAS", Tuple.UP, AppEdge.SENSOR);
        app.addAppEdge("chem-sensor", "chinfo-module", 1000, 200, "CHEM", Tuple.UP, AppEdge.SENSOR);
        app.addAppEdge("sr-sensor", "srinfo-module", 1000, 200, "SR", Tuple.UP, AppEdge.SENSOR);

        app.addAppEdge("gasinfo-module", "master-module", 2000, 200, "GAS_PROCESS", Tuple.UP, AppEdge.MODULE);
        app.addAppEdge("chinfo-module", "master-module", 2000, 200, "CHEM_PROCESS", Tuple.UP, AppEdge.MODULE);
        app.addAppEdge("srinfo-module", "master-module", 2000, 200, "SR_PROCESS", Tuple.UP, AppEdge.MODULE);

        app.addAppEdge("master-module", "response-module", 2000, 200, "ALERT", Tuple.UP, AppEdge.MODULE);
        
        List<AppLoop> loops = new ArrayList<>();
        
        List<String> gasLoop = new ArrayList<>();
        gasLoop.add("gas-sensor");
        gasLoop.add("gasinfo-module");
        gasLoop.add("master-module");
        gasLoop.add("response-module");
        loops.add(new AppLoop(gasLoop));
        
        List<String> chemLoop = new ArrayList<>();
        chemLoop.add("chem-sensor");
        chemLoop.add("chinfo-module");
        chemLoop.add("master-module");
        chemLoop.add("response-module");
        loops.add(new AppLoop(chemLoop));
        
        List<String> srLoop = new ArrayList<>();
        srLoop.add("sr-sensor");
        srLoop.add("srinfo-module");
        srLoop.add("master-module");
        srLoop.add("response-module");
        loops.add(new AppLoop(srLoop));

        app.setLoops(loops);

        return app;
    }
}
