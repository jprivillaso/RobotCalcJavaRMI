package robotcalc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author Juan Rivillas
 */
public class RobotCalcServer extends UnicastRemoteObject implements RobotCalcInterface {

    /**
     * Attributes
     */
    private int port;
    private String ip;
    private Registry registry;
    private URL url;
    private BufferedReader bufferReader;
    private StringBuilder stringBuilder;
    private String line;
    
    /**
     * Public Constructor
     */
    public RobotCalcServer() throws RemoteException {
        try {
            // Obtain this host direction
            ip = (InetAddress.getLocalHost()).toString();
        } catch (Exception e) {
            throw new 
                 RemoteException("It was not possible to obtain the direction");
        }
        //Assign the port
        this.port = 3232;
        try {
            // Create registry and match name and object
            registry = LocateRegistry.createRegistry(port);
            registry.rebind("rmiServidor", this);
        } catch (RemoteException e) {
            e.getMessage();
        }
        System.out.println("The server has been created successfully");
    }
    
    /**
     * Read the operations of the xml file
     * @throws RemoteException 
     */
    @Override
    public void readOperations() throws RemoteException {
        try {
            readXML();
        } catch (Exception e) {
            e.getMessage();
        }
    }
    
    /**
     * Show the IP 
     * @throws RemoteException 
     */
    @Override
    public void showResult() throws RemoteException {
        System.out.println
                ("La direccion a la que se está conectando es " + this.ip);
    }
    
    /**
     * Calls a remote web service, it receives the URL and the parameters to send
     * @param serverURL
     * @param params
     * @return
     * @throws RemoteException
     * @throws IOException
     * @throws JSONException 
     */
    @Override
    public String callRemoteService(String serverURL, Map<String, String> params)
            throws RemoteException, IOException, JSONException {
        try {
            url = new URL(serverURL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + serverURL);
        }
        JSONObject json_obj_req = new JSONObject(params);
        HttpURLConnection conn_to_server = null;
        try {
            //Configure the connection
            conn_to_server = (HttpURLConnection) url.openConnection();
            conn_to_server.setDoOutput(true);
            conn_to_server.setDoInput(true);
            conn_to_server.setUseCaches(false);
            conn_to_server.setRequestMethod("POST");
            conn_to_server.setRequestProperty("Content-Type","application/json");
            //Send the request to the web service
            sendRequest(conn_to_server.getOutputStream(), json_obj_req);

            // Handle the response. Only 200 is success
            int status = conn_to_server.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
            
            //Handle the writing of the response of the service 
            bufferReader = new BufferedReader
                    (new InputStreamReader(conn_to_server.getInputStream()));
            stringBuilder = new StringBuilder();
            while ((line = bufferReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            
            //Return the result of the webservice
            return stringBuilder.toString();
        } finally {
            if (conn_to_server != null) {
                conn_to_server.disconnect();
            }
        }
    }
    
    /**
     * Read XML method
     */
    private void readXML() {
        try {
            File fXmlFile = new File("src/robotcalc/ServicesLocation.xml");
            DocumentBuilderFactory dbFactory = 
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("webservice");

            for (int temp = 0; temp < 4; temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    
                    /*
                     * Build the url where the web service is located and send
                     * it to the callRemoteService method
                    */
                    String urlService = 
                            eElement.getElementsByTagName
                                ("url").item(0).getTextContent();
                    String method = 
                            eElement.getElementsByTagName
                                ("method").item(0).getTextContent();
                    String param1 = 
                            eElement.getElementsByTagName
                                ("p1").item(0).getTextContent();
                    String param2 = 
                            eElement.getElementsByTagName
                                ("p2").item(0).getTextContent();
                    
                    urlService += "?";
                    urlService += "action=" + method;
                    urlService += "&number1=" + param1;
                    urlService += "&number2=" + param2;

                    Map params = new HashMap();
                    params.put("action", method);
                    params.put("number1", param1);
                    params.put("number2", param2);

                    try {
                        String resultServiceString = 
                                callRemoteService(urlService, params);
                        writeXML(resultServiceString, temp);
                    } catch (IOException | JSONException e) {
                        e.getMessage();
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException 
                | IOException | DOMException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Write the result of the web service in the XML
     * @param result
     * @param index 
     */
    private void writeXML(String result, int index) {
        try {
           String filepath = "src/robotcalc/ServicesLocation.xml";
		DocumentBuilderFactory docFactory = 
                        DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(filepath);
 
		// Get the staff element by tag name directly
		Node resultNode = doc.getElementsByTagName("result").item(index);                
                resultNode.setTextContent(result);
 
		// write the content into xml file
		TransformerFactory transformerFactory = 
                        TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result1 = new StreamResult(new File(filepath));
		transformer.transform(source, result1);
 
        } catch (ParserConfigurationException | SAXException | IOException 
                    | DOMException | TransformerFactoryConfigurationError 
                        | TransformerException e) {
            e.getMessage();
        }
    }
    
    /**
     * Send the request to the server
     * @param conn_to_server
     * @param params
     * @throws IOException 
     */
    private static void sendRequest(OutputStream conn_to_server, JSONObject params)
            throws IOException {
        try (DataOutputStream printout = new DataOutputStream(conn_to_server)) {
            String body = params.toString();
            // write into the output stream the message to be sent
            printout.writeBytes(body);
            // send the request
            printout.flush();
        }
    }
    
    /**
     * Main method
     * @param args 
     */
    public static void main(String[] args) {
        try {
            RobotCalcServer robotCalcServer = new RobotCalcServer();
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
