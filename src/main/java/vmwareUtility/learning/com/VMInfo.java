package vmwareUtility.learning.com;

import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.VirtualMachine;

import java.util.List;
import java.util.Locale;

/**
 * Created by tim_barrett on 12/9/2015.
 */
public class VMInfo {
    private VirtualMachine vm;

    private String vmName;
    private boolean isNameValid;
    private boolean poweredOn;
    private boolean vmwareToolsUpToDate;
    private boolean osMatchesSettings;
    private String guestOsId;
    private boolean isOSWindows;
    private Integer memoryRequired;
    private int memory;
    private int cpuReservation;
    private Integer cpuReservationRequired;
    private int memoryReservation;
    private Integer memoryReservationRequired;

    private String requiredAdapter;
    private List<String> adapters;

    /**
     *
     * @param vm The vm that this VMSummary represents
     */
    public VMInfo(VirtualMachine vm) {

        // these will never change, so set them here
        this.vm = vm;
        setVMParamChanges();
    }

    public void setVMParamChanges() {
        Integer cpuReservationRequired = null;
        Integer memoryReservationRequired = null;
        Integer memoryRequired = null;
        isOSWindows = vm.getGuest().getGuestFamily().toLowerCase().contains("windows");
        boolean isPoweredOn = VirtualMachinePowerState.poweredOn.equals(vm.getRuntime().getPowerState());
        boolean vmToolsUpToDate = vm.getGuest().getToolsVersionStatus2().equals("guestoolsCurrent");

        if (isOSWindows) {
            cpuReservationRequired = 1800;
            memoryRequired = 4096;
            memoryReservationRequired = 4096;
        } else {
            cpuReservationRequired = 400;
            memoryRequired = 4096;
            memoryReservationRequired = 2048;
        }

        this.vmName = vm.getName();
        this.isNameValid = !this.vmName.contains(" ");
        this.memory = vm.getConfig().getHardware().getMemoryMB();
        this.memoryRequired = memoryRequired;
        this.poweredOn = isPoweredOn;
        this.vmwareToolsUpToDate = vmToolsUpToDate;
        this.guestOsId = vm.getGuest().getGuestId();
        this.osMatchesSettings = vm.getConfig().getGuestId().equals(this.guestOsId);;
        this.cpuReservationRequired = cpuReservationRequired;
        this.cpuReservation = vm.getSummary().getConfig().getCpuReservation();
        this.memoryRequired = vm.getSummary().getConfig().getMemoryReservation();
        this.memoryReservationRequired = memoryReservationRequired;

//        this.requiredAdapter = "";
//        this.adapters = new ArrayList<>();
    }

    public VirtualMachine getVm() {
        return vm;
    }
    public void setVm(VirtualMachine vm) {
        this.vm = vm;
    }
    public String getName() {
        return vmName;
    }
    public boolean isNameValid() {
        return isNameValid;
    }
    public void setNameValid(boolean nameValid) {
        this.isNameValid = nameValid;
    }
    public boolean isPoweredOn() {
        return poweredOn;
    }
    public boolean isVmwareToolsUpToDate() {
        return vmwareToolsUpToDate;
    }
    public boolean doesOsMatchSettings() {
        return osMatchesSettings;
    }
    public String getGuestOsId() {
        return guestOsId;
    }
    public void setGuestOsId(String guestOsId) {
        this.guestOsId = guestOsId;
    }

//    public String getRequiredAdapter() {
//        return requiredAdapter;
//    }
//
//    public void setRequiredAdapter(String requiredAdapter) {
//        this.requiredAdapter = requiredAdapter;
//    }
//
//    public List<String> getAdapters() {
//        return adapters;
//    }
//Getters and Setters
    public boolean isWindows() {
        return isOSWindows;
    }
    public void setVmwareToolsUpToDate(boolean vmwareToolsUpToDate) {
        this.vmwareToolsUpToDate = vmwareToolsUpToDate;
    }
    public Integer getMemoryRequired() {
        return memoryRequired;
    }
    public void setMemoryRequired(Integer memoryRequired) {
        this.memoryRequired = memoryRequired;
    }
    public boolean memoryNeedsUpdating(){return (memoryRequired != null && memoryRequired != memory);}
    public int getCpuReservation() {
        return cpuReservation;
    }
    public void setCpuReservation(int cpuReservation) {
        this.cpuReservation = cpuReservation;
    }
    public Integer getCpuReservationRequired() {
        return cpuReservationRequired;
    }
    public void setCpuReservationRequired(Integer cpuReservationRequired) {this.cpuReservationRequired = cpuReservationRequired;}
    public boolean cpuReservationNeedsUpdating() {return (cpuReservationRequired != null && cpuReservationRequired != cpuReservation);}
    public int getMemoryReservation() {
        return memoryReservation;
    }
    public void setMemoryReservation(int memoryReservation) {
        this.memoryReservation = memoryReservation;
    }
    public Integer getMemoryReservationRequired() {
        return memoryReservationRequired;
    }
    public void setMemoryReservationRequired(Integer memoryReservationRequired) {this.memoryReservationRequired = memoryReservationRequired;}
    public boolean memoryReservationNeedsUpdating() {return (memoryReservationRequired != null && memoryReservationRequired != memoryReservation);}

    /**
     * Returns true of all adapters match the required, false otherwise
     *
     * @return
     */
//    public boolean areNetworkAdaptersCorrect(){
//
//        if(adapters != null && requiredAdapter != null && !requiredAdapter.trim().equals("")){
//
//            for(String adapter : adapters){
//                if (!adapter.toLowerCase(Locale.ENGLISH).equals(requiredAdapter)){
//                    return false;
//                }
//            }
//        }
//
//        return true;
//    }

    public boolean isVmValid() {
        return poweredOn && isNameValid && vmwareToolsUpToDate && osMatchesSettings && !memoryNeedsUpdating() &&
                !cpuReservationNeedsUpdating() && !memoryReservationNeedsUpdating();
    }

    public String toString(){

        String indent = "   - ";
        String newLine = "\n";

        StringBuilder summary = new StringBuilder();

        summary.append(vmName + newLine);

        // Power
        if(!poweredOn){
            summary.append(indent + "Power: Off" + newLine);
        }

        if (!isNameValid) {
            summary.append(indent + "Name: Virtual Machine names cannot contain a space.");
        }

        // Tools
        if(!vmwareToolsUpToDate){
            summary.append(indent + "Tools: Needs to be updated" + newLine);
        }

        // OS
        if(!osMatchesSettings){
            summary.append(indent + "OS: Settings needs to be updated to Guest" + newLine);
        }

        // Memory
        if(memoryNeedsUpdating()){
            summary.append(indent + "Memory: Needs to be updated from " + memory + " MB to " + memoryRequired + " MB" + newLine);
        }

        // CPU Reservation
        if (cpuReservationNeedsUpdating()) {
            summary.append(indent + "CPU Reservation: Needs to be updated from " + cpuReservation + " MHz to " + cpuReservationRequired + " MHz" + newLine);
        }

        // Memory Reservation
        if (memoryReservationNeedsUpdating()) {
            summary.append(indent + "Memory Reservation: Needs to be updated from " + memoryReservation + " MB to " + memoryReservationRequired + " MB" + newLine);
        }

//        // Network connections
//        if (requiredAdapter != null) {
//            if (!requiredAdapter.equals("")) {
//                for (String adapter : adapters) {
//
//                    if (!adapter.toLowerCase(Locale.ENGLISH).equals(requiredAdapter)) {
//                        summary.append(indent + "Network Adapter: " + adapter + " Needs updating to " + requiredAdapter + newLine);
//                    }
//                }
//            }
//        }

        return summary.toString();
    }
}
