/* 
 * <copyright> 
 *   
 *  Copyright 1999-2004 Object Services and Consulting, Inc. 
 *  under sponsorship of the Defense Advanced Research Projects 
 *  Agency (DARPA). 
 *  
 *  You can redistribute this software and/or modify it under the 
 *  terms of the Cougaar Open Source License as published on the 
 *  Cougaar Open Source Website (www.cougaar.org). 
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 *   
 * </copyright> 
 */ 

import com.objs.surveyor.probemeister.remote.*;

public class TestRemoteRMI {

    
    
    public static void main(String[] args) {

        String name = null;
        String addr = null;
        String port = null;
        if (args.length == 3) {
            name = args[0];
            addr = args[1];
            port = args[2];
        }
        if (args.length==1) {
            System.out.println("Usage: \n  Attach to new JVM:\n     java TestRemoteRMI <name> <addr> <port>\n,");
            System.out.println("\n  Attach to current JVM:\n     java TestRemoteRMI");
        }
        //Set up connections & environment
        String host = "127.0.0.1"; 
        java.util.Properties props = System.getProperties();
        props.put("java.security.policy","./policy");
        System.setProperties(props);        
      
        // Create and install a security manager 
        if (System.getSecurityManager() == null) { 
	        System.setSecurityManager(new java.rmi.RMISecurityManager()); 
        } 

        //Look for ProbeMeister
        RemotePMInterface pm = null;
        try {             
            String pmname = "//" + host + "/ProbeMeisterRMI";
            pm = (RemotePMInterface)java.rmi.Naming.lookup(pmname);
            
        } catch (Exception e) { 
	        System.out.println("RemotePM RMI err: " + e.getMessage()); 
	        System.out.println("Most likely, the RMI Registry is not running..."); 
	        e.printStackTrace();
        }

        //So far so good...
        try {
            //Just because pm is non-null doesn't yet mean we've got an object
            //We'll get an exception once we try to invoke a method on it
            if (pm != null) {
                System.out.println("Found ProbeMeister!");

                String app = null;
                if (name != null) { //then user enter command line args to call attachToJVM
                    app = attachToApp(pm, name, addr, port);
                    System.out.println("Sent request to attach, waiting for PM to load classes");
                    Thread.sleep(3000); //let PM connect & load classes
                }
                else {
                    app = getExistingApp(pm);
                }
                
                if ( !(app==null) || !app.equals("null")) { //app="null" if we didn't get a name for it from the command line (yet)...
                    //Create list of (one, in this example) potenial deployment locations
                    GaugeRequestLocationInterface loc = new GaugeRequestLocation(app, "SimpleExample2", "instrumentMe", -1);
                    GaugeRequestLocationInterface[] locs = new GaugeRequestLocationInterface[1];
                    locs[0] = loc;
                    
                    //Now create a Map of the stub-specific ("BasicEventStub") parameters
                    java.util.Map params = new java.util.Hashtable();
                    params.put("evtName", "MySienaEvent");
                    params.put("evtSubName", "MySienaSubEvent");
                    params.put("msg", "My very informative message");
                    //create & send request
                    GaugeRequestInterface gr = new GaugeRequest("myGaugeID", "SP0",
                                        "Deploying a cool probe",
                                        "BasicEventStub", 
                                        "com.objs.surveyor.probemeister.probe.siena.PLUG_GenericEventToSiena",
                                        "PP_emitBasicSienaEvent", 
                                        app, locs, GaugeRequestInterface.USER_CHOICE,
                                        params);
//                                        GaugeRequestInterface.REUSE_LAST_CONFIG);
                
                        
                    String result = pm.sendGaugeDeployRequest(gr);
                    // if result == null, then the request MOST LIKELY was accepted OK
                    System.out.println("sendGaugeDeployRequest result = "+result);
                } else
                    System.out.println("App name was null! Exiting...");                
            }
            else
                System.out.println("Did not find ProbeMeister!");
        } catch (Exception e) { 
	        System.out.println("TestRemoteRMI err: " + e.getMessage()); 
	        System.out.println("Found object but encountered error accessing object."); 
	        e.printStackTrace();
        } 
                
        
    }
    private static String attachToApp(RemotePMInterface _pm, String _name, String _addr, String _port)
        throws java.rmi.RemoteException  {

        String addr = _addr;
        if (_addr.equalsIgnoreCase("null"))
            addr = null;
        return _pm.attachToJVM(_name, addr, _port);
    }
    
    private static String getExistingApp(RemotePMInterface _pm) throws java.rmi.RemoteException {
        
        String[] s = _pm.getDataPanelNames();   
        System.out.println("Connected VMs: ["+s.length+"]");
        System.out.println("--------------");
        for (int i=0; i<s.length; i++) 
            System.out.println(s[i]);
                    
        //Just send a gauge request to the first VM
        if (s == null || s.length==0) {
            System.out.println("Did not find any attached VMs! Exiting...");
            System.exit(0); //quit
        }
        return s[0];
    }
    
    
}