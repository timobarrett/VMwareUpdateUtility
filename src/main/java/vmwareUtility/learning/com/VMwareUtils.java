package vmwareUtility.learning.com;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;


import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Created by tim_barrett on 12/9/2015.
 */
public class VMwareUtils {
    private Logger LOGGER;
    private final String guestToolStatus = "guestToolsCurrent";
    private final String guestoolState = "guestoolsRunning";

    /**
     * Constructor
     * @param LOGGER
     */
    public VMwareUtils (Logger LOGGER){
        this.LOGGER = LOGGER;
    }

    /**
     * get a server instance
     * @param hostIP
     * @param username
     * @param password
     * @return
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public ServiceInstance createServerInstance(String hostIP, String username, String password)
            throws MalformedURLException, RemoteException {
        return new ServiceInstance(new URL("https://" + hostIP + "/sdk"), username, password, true);
    }

    /**
     * get all hosts
     * @param instance
     * @return
     * @throws RemoteException
     */
    public ManagedEntity[] getAllHostsInVCenter(ServiceInstance instance) throws RemoteException {
        Folder rootFolder = instance.getRootFolder();
        return new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
    }

    /**
     * get specific host VMs
     * @param instance
     * @return
     * @throws RemoteException
     */
    public HostSystem getHostSystem(ServiceInstance instance) throws RemoteException {
        return (HostSystem) getAllHostsInVCenter(instance)[0];
    }

    /**
     * update the version of vmware tools running on host
     * @param vm
     * @return
     */
    public boolean updateVMwareTools(VMInfo vm) {

        LOGGER.info("VM " + vm.getName() + "- upgrading out of date VMware Tools");
        try {

            Task task = vm.getVm().upgradeTools_Task("");
            String taskResult = task.waitForTask();
            LOGGER.debug("VMware Tools upgrade result: " + taskResult);
            if(!taskResult.equals("success")){
                return false;
            }
            //Next is ugly but needed to ensure upgrade completes and VM reboots
            Thread.sleep(10000);
        }catch (RemoteException | InterruptedException e) {
            LOGGER.debug( "Error updating the VMware Tools for VM " + vm.getName() + " " + e.getMessage());
        }
        // Wait for the tools to be running and current
        int counter = 0;
        while (!isVMwareToolUpdateDone(vm)) {

            LOGGER.debug( "VM " + vm.getName() + ": Waiting for upgrade to finish. Elapsed Time: " + (counter * 5) + " seconds.");

            try {
                Thread.sleep(5000);
                counter++;
                if (counter > 120) {
                    LOGGER.error("vmware tools update reporting old version after 10 minutes for vm "+ vm.getName()+" `NOT UPDAED");
                    return false;
                }
            } catch (InterruptedException e) {
                LOGGER.error("Interruption received during vmware tools upgrade");
                return false;
            }
        }
        waitForTaskCompletion(vm);

        return true;
    }

    /**
     * check to see if the vmware tools upgrade is complete
     * @param vm
     * @return
     */
    private boolean isVMwareToolUpdateDone(VMInfo vm){
        GuestInfo vmGuest = vm.getVm().getGuest();

        return vmGuest.getToolsRunningStatus().contains(guestoolState) &&
                vmGuest.getToolsVersionStatus2().contains(guestToolStatus);
    }

    /**
     * make sure all tasks for vm have been completed
     * @param vm
     * @return
     */
    private boolean waitForTaskCompletion(VMInfo vm) {
        LOGGER.debug("in waitForTaskCompletion for vm " + vm.getName());
        boolean runningTask = true;
        while (runningTask) {
            runningTask = false;
            try {
                for (Task task : vm.getVm().getRecentTasks()) {
                    if (task.getTaskInfo().getState().equals(TaskInfoState.running) || task.getTaskInfo().equals(TaskInfoState.queued)) {
                        runningTask = true;
                    }
                    if (runningTask) {
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException | RemoteException re) {
                LOGGER.error(re.getLocalizedMessage());
            }
        }
        return runningTask;
    }

    /**
     * Modify the VM specification for OS version, memory, memory reservation and cpu reservation
     * @param vm
     * @return
     */
    public boolean modifyVMSettings(VMInfo vm) {

        VirtualMachineConfigSpec changeSpec = new VirtualMachineConfigSpec();
        if (!vm.doesOsMatchSettings()) {
            changeSpec.setGuestId(vm.getGuestOsId());
            LOGGER.info("OS Setting of " + vm.getName() + " changing to " + vm.getGuestOsId());
        }
        if (vm.memoryNeedsUpdating()) {
            changeSpec.setMemoryMB(vm.getMemoryRequired().longValue());
            LOGGER.info("Updating Memory of " + vm.getName() + " changing to " + vm.getMemoryRequired());
        }
        if (vm.memoryReservationNeedsUpdating()) {
            ResourceAllocationInfo memoryAllocation = new ResourceAllocationInfo();
            memoryAllocation.setReservation(vm.getMemoryReservationRequired().longValue());
            changeSpec.setMemoryAllocation(memoryAllocation);
            LOGGER.info("Updating Reserved memory for " + vm.getName() + " changing to " + vm.getMemoryReservationRequired());
        }
        if (vm.cpuReservationNeedsUpdating()) {
            ResourceAllocationInfo cpuAllocation = new ResourceAllocationInfo();
            cpuAllocation.setReservation(vm.getCpuReservationRequired().longValue());
            changeSpec.setCpuAllocation(cpuAllocation);
            LOGGER.info("Updating reserved CPU for " + vm.getName() + " changing to " + vm.getCpuReservationRequired());
        }

        try {
            Task task = vm.getVm().reconfigVM_Task(changeSpec);
            boolean result = waitForTaskCompletion(vm);

            if (result) {
                return true;
            } else {
                return false;
            }

        } catch (RemoteException re) {
            LOGGER.error("Error updating VM " + vm.getName() + " settings. "+ re.getLocalizedMessage());
        }
        return false;
    }


}
