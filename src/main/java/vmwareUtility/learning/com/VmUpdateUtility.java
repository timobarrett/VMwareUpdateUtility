package vmwareUtility.learning.com;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import com.vmware.vim25.InvalidLogin;
import com.vmware.vim25.mo.*;


/**
 * Created by tim_barrett
 *  Entry point for utility to change VMware virtual machines
 *
 */
public class VmUpdateUtility {
    public static AppConsole console;
    public static Creds creds;
    public static VMwareUtils vmwareUtils;
    private boolean vCenterDeployment;
    private List<VMInfo> VirtualMachineList;
    private static final Logger LOGGER = Logger.getLogger("VmUpdateUtility");

    /**
     * Entry point for this java console based application
     * Application updates OS version(to ensure proper drivers),memory, memory reservation and CPU reservation
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        VmUpdateUtility vmUtil = new VmUpdateUtility();
        vmwareUtils = new VMwareUtils(LOGGER);
        console = new AppConsole();
        vmUtil.startApp();
    }

    /**
     * Starts the console based application
     * @throws Exception
     */
    public void startApp() throws Exception{

        console.presentFirstUserScreen();
        vCenterDeployment = vCenterUsed();
        creds = console.askForCreds(vCenterDeployment);

        try {
            // get Connection to server
            HostSystem esxHost = connectToHost();
            // get all of the vms on the host
            VirtualMachine[] virtualMachines = esxHost.getVms();
            if (virtualMachines.length == 0) {
                LOGGER.info("\nVirtual machines not present on the host. Exiting");
            } else
            {
                // cache machines and launch menu
                //TODO  this needs to be revisted
                addVirtualMachinesToList(virtualMachines);
                while (true) {
                    String selected = console.appMenu(creds.getHostIP(), VirtualMachineList);
                    if (selected.equalsIgnoreCase("q")) {
                        break;
                    }else
                    if (selected != null) {
                        if (Integer.valueOf(selected) > virtualMachines.length) {
                            LOGGER.error("VM selected is not on the list of VMs to process");
                        } else {
                            updateUserSelectedVM(VirtualMachineList.get(Integer.valueOf(selected)));
                        }
                    }
                }
            }

        } catch(InvalidLogin e) {
            LOGGER.error("\nERROR: Invalid username/password.");

        } catch(RemoteException e) {
            LOGGER.error("\nERROR: Unable to connect.");

        } catch (NullPointerException e) {

        } catch(Exception e) {
            LOGGER.debug("Error occurred", e);
            LOGGER.error("\nError occurred. Read the log for more information.");
        }
    }

    /**
     * prompt the user to determine if they are running VMs in a virtual center
     * @return
     */
    public boolean vCenterUsed(){
        String response = console.getUserInput("\n\n Is VMs ESXi Host installed in vCenter? (y or n", false);
        return (response.equalsIgnoreCase("y")? true:false);
    }

    /**
     * connect to either the vcenter or the esx host
     * @return
     * @throws Exception
     */
    public HostSystem connectToHost() throws Exception {
        ServiceInstance serviceInstance;
        HostSystem esxHost;
        try {
            if (vCenterDeployment) {
                serviceInstance = vmwareUtils.createServerInstance(creds.getvCenterIP(),creds.getUsername(), creds.getPassword());
                ManagedEntity[] hosts = vmwareUtils.getAllHostsInVCenter(serviceInstance);
                esxHost = locateHost(hosts);

            } else
            {
                serviceInstance = vmwareUtils.createServerInstance(creds.getHostIP(),creds.getUsername(), creds.getPassword());
                esxHost = vmwareUtils.getHostSystem(serviceInstance);
            }
        } catch (MalformedURLException | RemoteException e) {
            throw e;
        }
        return esxHost;
    }

    /**
     *
     * @param hosts
     * @return
     * @throws RemoteException
     */
    public HostSystem locateHost(ManagedObject[] hosts ) throws RemoteException {
        for (ManagedObject host : hosts) {
            HostSystem esxHost = (HostSystem)host;
            if (esxHost.getName().equalsIgnoreCase(creds.getHostIP())) {
                return esxHost;
            }
        }
        throw new RemoteException();
    }

    /**
     * create a list of VMs found on host or vcenter
     * @param virtualMachines
     */
    public void addVirtualMachinesToList(VirtualMachine[] virtualMachines) {
        VirtualMachineList = new ArrayList<VMInfo>();
        for (int i=0; i < virtualMachines.length; i++){
            VirtualMachineList.add(new VMInfo(virtualMachines[i]));
        }
    }

    /**
     * update the VMs setting as requested
     * remove VM from list if successfully updated
     * @param vm
     */
    private void updateUserSelectedVM(VMInfo vm){
        LOGGER.info("\n Processing VM named = " + vm.getName());
        boolean vmToolsUpdated = true;

        if (!vm.isNameValid()){
            LOGGER.error("\n VMs with spaces in their name are not supported.");
            LOGGER.error("\n Please correct the VM name before trying again");
            return;
        }
        if (!vm.isPoweredOn()){
            LOGGER.error("\n The vm selected is powered off.");
            LOGGER.error("\n Power on the vm before trying again");
            return;
        }
        if (!vm.isVmwareToolsUpToDate()) {
            LOGGER.info("\n vmware tools out of date - updating on vm " + vm.getName());
            vmToolsUpdated = vmwareUtils.updateVMwareTools(vm);
        }
        if (vmwareUtils.modifyVMSettings(vm) && vmToolsUpdated){
            VirtualMachineList.remove(vm);
        }

    }
}
