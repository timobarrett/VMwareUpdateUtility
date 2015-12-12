package vmwareUtility.learning.com;

import com.vmware.vim25.mo.VirtualMachine;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by tim_barrett on 12/9/2015.
 */
public class AppConsole {

    private static final Logger LOGGER = Logger.getLogger("VmUpdateUtility");
    //generic constructor
    public AppConsole(){}

    /**
     *
     */
    public void presentFirstUserScreen(){
        // display user facing console prompts
        //TODO what about OS version and NIC changes??
        LOGGER.info("\nVMware VM Upgrade Utility \n");
        LOGGER.info("This Utility Will:");
        LOGGER.info(" - Update VMware Tools on a VM");
        LOGGER.info(" - Change the VMware OS Setting ");
        LOGGER.info(" - Change the Memory");
        LOGGER.info(" - Change the CPU Reservation");
        LOGGER.info(" - Change the Memory Reservation");
        LOGGER.info(" - Change the Network Adapter Type");

        LOGGER.info("VM's must be powered on with VMware Tools installed: ");
    }

    /**
     *
     * @param outputToUser
     * @param passwordPrompt
     * @return
     */
    public String getUserInput(String outputToUser,boolean passwordPrompt){
        String response;

        if (passwordPrompt){
            response = new String(System.console().readPassword(outputToUser));
            LOGGER.debug(outputToUser + "****");
        }
        else {
            response = new String(System.console().readLine(outputToUser));
            LOGGER.debug(outputToUser + response);
        }
        return response;
    }

    public Creds askForCreds(boolean vCenterInUse){
        String vCenterIP;
        String hostIP;
        String username;
        String password;

        if (vCenterInUse){
            vCenterIP = getUserInput("Enter the IP Address of your vCenter: ",false);
            username = getUserInput("Enter administrator username for your vCenter: ", false);
            password = getUserInput("Enter administrator password for your vCenter: ", true);
            hostIP = getUserInput("Enter the ESXi Hosting your virtual machines: ",false);
        }
        else {
            vCenterIP = " ";
            hostIP = getUserInput("Enter the ESXi Hosting your virtual machines: ",false);
            username = getUserInput("Enter administrator username for your ESXi host: ", false);
            password = getUserInput("Enter administrator password for your ESXi host: ", true);
        }
        return new Creds(vCenterIP,hostIP,username,password);
    }
    protected  String appMenu(String hostIP,List<VMInfo> vmList) throws Exception{

            LOGGER.info("\n--------- Main Menu ---------");
            LOGGER.info("\n -- Virtual Machines to Update --");

            for (int i=0; i<vmList.size(); i++){
                if (!vmList.get(i).isVmValid()) {
                    String machine = " " + (i+1) + ". "+ vmList.get(i).getName();
                    LOGGER.info(machine);
                }
            }
            return getUserInput("Enter Server Number above \"Q\" to quit: ", false);
//            if (userSelect.equalsIgnoreCase("q")){ returnValue ="q";}
//            else {returnValue = user
//                vmToProcess = handleSingleInput(userInput);
//            }
//
//            if (vmToProcess != null) {
//                processVMs(vmToProcess);
//
//                VMCache.refresh();
//            }

    }
}
