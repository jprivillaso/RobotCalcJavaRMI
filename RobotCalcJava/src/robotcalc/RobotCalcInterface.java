/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotcalc;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import org.json.JSONException;

/**
 *
 * @author Juan Rivillas
 */
public interface RobotCalcInterface extends Remote {

    void readOperations() throws RemoteException;

    void showResult() throws RemoteException;
    
    String callRemoteService(String serverURL, Map<String, String> params) throws RemoteException, IOException, JSONException;
}
