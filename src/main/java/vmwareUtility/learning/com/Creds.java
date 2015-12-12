package vmwareUtility.learning.com;

/**
 * Created by tim_barrett on 12/9/2015.
 */
public class Creds {
    private String vCenterIP;
    private String hostIP;
    private String username;
    private String password;

    public Creds(String vCenterIP, String hostIP, String username, String password) {
        this.vCenterIP = vCenterIP;
        this.hostIP = hostIP;
        this.username = username;
        this.password = password;
    }

    public String getvCenterIP() {
        return vCenterIP;
    }
    public void setvCenterAddress(String vCenterIP) {
        this.vCenterIP = vCenterIP;
    }

    public String getHostIP() {
        return hostIP;
    }
    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

//    public String toString() {
//        return this.hasVCenter+this.vCenterAddress+this.hostAddress+this.username+this.password;
//    }

}
