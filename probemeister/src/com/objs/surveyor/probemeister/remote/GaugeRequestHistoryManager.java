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
/*
 * GaugeRequestDialog.java
 *
 * Created on April 25, 2002, 9:24 AM
 */

/**
 *
 * @author  Administrator
 */
 
 //com.objs.surveyor.probemeister.remote.GaugeRequestDialog

package com.objs.surveyor.probemeister.remote;

import com.objs.surveyor.probemeister.PMMethod;
import com.objs.surveyor.probemeister.PMClass;
import com.objs.surveyor.probemeister.Globals;
import com.objs.surveyor.probemeister.GlobalVars;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.StringTokenizer;

public class GaugeRequestHistoryManager {
    
    //Default file name
    private static String g_GaugeHistoryDBFile = "gaugeHistoryDB.txt";
    private static File dbFile=null;
    private static Vector history=null;
    
    private GaugeRequestHistoryManager() {
        
        //See if default has been overridden
        String db = (String)Globals.globals().get(GlobalVars.RMI_GAUGEHISTORY);
        if (db==null) //No, so write it there
            Globals.globals().put(GlobalVars.RMI_GAUGEHISTORY, g_GaugeHistoryDBFile);
        else //Yes, so use it
            g_GaugeHistoryDBFile = db;

        history = new Vector();
            
        try {
            dbFile = new File(g_GaugeHistoryDBFile);
            //If db doesn't exist, create one
            if (!dbFile.exists()) {
                dbFile.createNewFile();
            } else //read it in                        
                readInDB();
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE, "GaugeRequestHistoryManager::IO Exception accessing history file...", e);                        
            dbFile = null;
        }
    }

    private static GaugeRequestHistoryManager mgr = null;
    
    public static GaugeRequestHistoryManager mgr() {
        if (mgr == null)
            mgr = new GaugeRequestHistoryManager();
        return mgr;
    }
    
   /* Add request to history. Write out DB after each call to ensure
    * persistent in case of app crash.
    */
   public void add(GaugeRequest _gr, PMMethod _meth, int _loc) {
    
        try {
            String id = _gr.getDeployID();
            String cls = _meth.getPMClass().name();
            String sig = _meth.nameNSig();
        
            Entry e = new Entry(id, cls, sig, _loc);    
            addRecord(e);
            
        } catch(java.rmi.RemoteException re) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE, "GaugeRequestHistoryManager::Remote Exception accessing GaugeRequest object...", re);            
        }
   }

    
   /* 
    * Lookup all past locations where this gauge request
    * was deployed.
    */
   public GaugeRequestLocation[] lookup(GaugeRequest _gr) {
 
        GaugeRequestLocation[] results= null;

        try {
            Vector dbVector = history;
            if (dbVector.size() == 0) return null;
            Vector hits = new Vector();
            
            String id = _gr.getDeployID();
            if (id==null) return null;
            
            //Find the index of each match
            for (int i=0; i<dbVector.size(); i++) {
                Entry e = (Entry)dbVector.get(i);
                if (e.deployID.equals(id))
                    hits.add(new Integer(i));
            }
            
            //Create results array & copy hits into it
            results = new GaugeRequestLocation[hits.size()]; 
            for (int j=0; j<hits.size(); j++) {
                int pos = ((Integer)hits.get(j)).intValue();
                Entry ee = (Entry)dbVector.get(pos);
                results[j] = new GaugeRequestLocation(ee.deployID,ee.className,ee.methodName,ee.location);
            }
        } catch(java.rmi.RemoteException re) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE, "GaugeRequestHistoryManager::Remote Exception accessing GaugeRequest object...", re);            
        }
        return results;
   }

    /* 
    * Lookup last entry for a given DeployID
    */
   public Entry lookupLast(Entry _e) {
 
        Entry last = null;

        Vector dbVector = history;
        if (dbVector.size() == 0) return null;
        Vector hits = new Vector();
            
        String id = _e.deployID;
        if (id==null) return null;
            
        //Find the index of each match
        for (int i=0; i<dbVector.size(); i++) {
            Entry e = (Entry)dbVector.get(i);
            if (e.deployID.equals(id))
                last = e;
        }
            
        return last;
   }

 
 

    /* Do this once, then only inspect in-memory data. */
    private void readInDB() {
    
        try {
            //Open file
            FileReader reader = new FileReader(g_GaugeHistoryDBFile);
            
            com.objs.surveyor.probemeister.Log.out.finer("GaugeRequestHistoryManager::===> Gauge History catalog File Found.");
                        
            //Read data
            //1. Set up BufferedReader which has a readLine() method
            BufferedReader bReader = new BufferedReader(reader);  
            for (;;) { 
                String data = bReader.readLine();
                if (data != null) { // non-null until EOF
                    if (data.length() < 5 || data.startsWith("#")) {
                        com.objs.surveyor.probemeister.Log.out.finest("GaugeRequestHistoryManager::ignoring line in file - comment or length <5");
                        continue;   //essentially empty line
                    }
                    //System.out.println("Read:      "+ String.valueOf(data));
                    //Parse & Add to the list of plugCatalog 
                    if (!importDBFileEntry(data)) { //Error processing data, try to keep processing
                        com.objs.surveyor.probemeister.Log.out.severe("GaugeRequestHistoryManager::Error in Gauge History  catalog file");            
                        com.objs.surveyor.probemeister.Log.out.severe("GaugeRequestHistoryManager::=> Each line should contain one class name with no addition punctuation.");
                    } else
                        com.objs.surveyor.probemeister.Log.out.info("GaugeRequestHistoryManager::=> Gauge History  "+data+" has been imported.");
                } else { //no more data, EOF
                    break; 
                }
            }
            //All done, close file.
            bReader.close();

        } catch (java.io.FileNotFoundException fnf) {
            //Assume that the file never existed, so we have no plugCatalog to load. OK to stop.
            com.objs.surveyor.probemeister.Log.out.severe("GaugeRequestHistoryManager::No Gauge History  catalog found. Looking in jar for: "+g_GaugeHistoryDBFile);
        } catch (java.io.IOException ioe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE, "GaugeRequestHistoryManager::IO Exception reading file...", ioe);
        } catch (NullPointerException npe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE, "GaugeRequestHistoryManager::IO Exception reading file...", npe);            
        }
    }
    
    /**
      * This routine converts the strings to BCEL java_class objects, and
      * then examines the class & extracts all probe plugs. The plugs are
      * added to this.plugsCatalog 
      */
    private boolean importDBFileEntry(String _unparsed) {
        
        String className = null;
        String methName  = null;
        String deployID  = null;
        int location = 0;
        String sLoc = "0";

        //Delimiter is a ':'
        StringTokenizer st = new StringTokenizer(_unparsed, ":");
        
        //Process Data - extract probe plug class name & description
        if (st.hasMoreTokens()) 
           deployID = st.nextToken();

        if (st.hasMoreTokens()) { //then get the description
            className = st.nextToken();
        }
        if (st.hasMoreTokens()) { //then get the description
            methName = st.nextToken();
        }
        if (st.hasMoreTokens()) { //then get the description
            sLoc = st.nextToken();
            try {location = Integer.parseInt(sLoc);}
            catch(Exception e) {
                com.objs.surveyor.probemeister.Log.out.warning("Could not parse offset, setting to 0.");
            }                
        }

        if (deployID != null && className != null && methName != null) {
            //Add entry to history
            addRecord(new Entry(deployID, className, methName, location));
            return true;
        } else
            return false;
    }
    
    /* Write entry to memory & dump to history file */
    private void addRecord(Entry _e) {
     
        //if not a duplicate of the last one, then update history 
        Entry last = this.lookupLast(_e);
        if (last != null && last.className.equals(_e.className) && last.methodName.equals(_e.methodName))
            return; //we have a match so don't add it again...
        
        history.add(_e); 
            
        try {
            FileWriter out = new FileWriter(dbFile);
            PrintWriter toDB
                = new PrintWriter(new BufferedWriter(out));
                    
            toDB.println("# Gauge Deployment Request History File");
            for (int i=0; i<history.size(); i++) {
                Entry e = (Entry)history.get(i);
                toDB.println(e.deployID+":"+e.className+":"+e.methodName+":"+e.location);
            }
            toDB.close();
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE, "GaugeRequestHistoryManager::IO Exception writing history file...", e);            
        }
    }
    
    private class Entry {
        
        String deployID;
        String className;
        String methodName;
        int location;
        
        Entry(String _id, String _cls, String _meth, int _loc) {
            
            deployID = _id;
            className = _cls;
            methodName = _meth;            
            location = _loc;
        }
    }
    
    
}
