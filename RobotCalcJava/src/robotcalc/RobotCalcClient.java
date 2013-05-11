/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotcalc;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *
 * @author Juan Rivillas
 */
public class RobotCalcClient {

    /**
     * Attributes
     */
    private static RobotCalcInterface rmiServidor;
    private static Registry registro;
    private static String direccionServidor = "127.0.0.1";
    private static String puertoServidor = "3232";

    /*
     * Public Constructor
     */
    public RobotCalcClient() {
        conectarseAlServidor();
        showResult();
        readXML();
        //callRemoteService();
    }

    private static void conectarseAlServidor() {
        try {
            // Obtain the registry
            registro = LocateRegistry.getRegistry(direccionServidor,
                    (new Integer(puertoServidor)).intValue());
            // Creates remote object
            rmiServidor = (RobotCalcInterface) (registro.lookup("rmiServidor"));
        } catch (RemoteException | NotBoundException e) {
            e.getMessage();
        }
    }

    private void showResult() {
        try {
            rmiServidor.showResult();
        } catch (Exception e) {
            e.getMessage();
        }
    }
    
    private void readXML(){
        try {
            rmiServidor.readOperations();
        } catch (Exception e) {
            e.getMessage();
        }
    }
    
    private void callRemoteService() {
        try {
            rmiServidor.readOperations();
        } catch (Exception e) {
            e.getMessage();
        }
    }
    
    public static void main(String args[]) {
        RobotCalcClient robotCalcClient = new RobotCalcClient();
    }

   
}
