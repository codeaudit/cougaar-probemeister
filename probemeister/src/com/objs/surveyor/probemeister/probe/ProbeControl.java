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

// Object Services & Consulting, Inc. All Rights Reserved. 2001
// Author: Paul Pazandak
// Date:   16-Oct-01
package com.objs.surveyor.probemeister.probe;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.util.StringTokenizer;

/*
 *  This class is used to centrally manage a set of flags that control the 
 *  de/activation of probes. Flags are added for each controllable probe
 *  when the probe is installed.
 */
public class ProbeControl {
    
    static final String g_flagDBFileName = "ProbeMeister.ProbeControlFlagDB.txt";
    
    // store for flags -- stored as flagobject - state pairs 
    private java.util.Hashtable flags; 
    
    //Attribute holds the one instance of ProbeControl
    private static ProbeControl controller;
    
    static { 
        controller = new ProbeControl();
    }
    
    private ProbeControl() {       
        flags = new java.util.Hashtable();
        
        // read in state (flags) from file, if they exist.
        readInDB();
    }        
        
    /* Returns an instance of ProbeControl (there is only one) */
    public static ProbeControl getController() {return controller;}

    /* Adds a flag object with the specified initial state */
    public void addFlag(String id, boolean state) { 
        flags.put(id, (state ? Boolean.TRUE : Boolean.FALSE)); }
    
    /* Removes the specified flag, if found.
     * @return false if not found.
     */    
    public boolean removeFlag(String id) {
        if (flags.containsKey(id)) {
            flags.remove(id);
            return true;
        } else
            return false;
    }

    /* Activate the flag, if found. If not found, then adds & activates flag.
     */    
    public void activateFlag(String id) { //, boolean addIfNotExists) {
        flags.put(id, Boolean.TRUE);
    }
    
    /* Deactivate the flag, if found. If not found, then adds & deactivates flag.
     */    
    public void deactivateFlag(String id) {
        flags.put(id, Boolean.FALSE);
    }
    
    /* @return the state of the flag as a Boolean object, or false if flag name not found. 
     * @see getState() method, which is not a static method.
     */
    public static Boolean getProbeState(String flag) {
        Boolean state = (Boolean) ProbeControl.getController().flags.get(flag);
        return ((state != null) ? state : Boolean.FALSE);
    }
    
    /* @return the state of the flag, or false if flag name not found. */
    public boolean getState(String flag) {
        Boolean state = (Boolean) flags.get(flag);
        return ((state != null) ? state.booleanValue() : false);
    }

    /* Write out state to file */
    public void finalize() {
        
        writeOutDB();
    }
    
    private void readInDB() {
    
        //Load DB of flags, if one.        
        try {
            File initFile = new File(g_flagDBFileName);

            //Open file
            FileReader reader = new FileReader(initFile);
            //InputStreamReader reader = new InputStreamReader(new FileInputStream(initFile));
            com.objs.surveyor.probemeister.Log.out.fine("===> Flag DB File Found.");
                        
            //Read data
            //1. Set up BufferedReader which has a readLine() method
            BufferedReader bReader = new BufferedReader(reader);  
            for (;;) { 
                String data = bReader.readLine();
                if (data != null) { // non-null until EOF
                    if (data.length() >0) {
                        //System.out.println("Read:      "+ String.valueOf(data));
                        //Parse & Add to the list of flags 
                        if (!importDBFileEntry(data)) { //Error processing data, try to keep processing
                            com.objs.surveyor.probemeister.Log.out.warning("Error in flag DB file, unpaired attribute found");            
                            com.objs.surveyor.probemeister.Log.out.warning("<Attr-Value Pairs should be '=' delimited, one pair to a line.>");
                        }
                    }
                } else { //no more data, EOF
                    break; 
                }
            }
            //All done, close file.
            bReader.close();

        } catch (java.io.FileNotFoundException fnf) {
            //Assume that the file never existed, so we have no flags to load. OK to stop.
            com.objs.surveyor.probemeister.Log.out.warning("ProbeControl::No flag db found... OK");
        } catch (java.io.IOException ioe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "ProbeControl::IO Exception reading file...", ioe);
        }

    }
    
    /**
      * This routine takes a string and extracts '=' delimited attribute-value
      * pairs & stores them into the flag db
      */
    private boolean importDBFileEntry(String data) {
        
        //Init structs
        StringTokenizer st = new StringTokenizer(data, "= ");
        String flagName  = null;
        String state = null;
        boolean stateVal = false;
        
        //Process Data
        while (st.hasMoreTokens()) {
           flagName = st.nextToken();
           if (st.hasMoreTokens()) {
                state = st.nextToken();
                //Set state to TRUE (on) if string starts with 'T' (as opposed to F(alse))
                stateVal = (state.startsWith("T")|| state.startsWith("t"))? true : false;
           }
           else //error -- no paired value!
                return false;
           this.addFlag(flagName, stateVal);
           com.objs.surveyor.probemeister.Log.out.finer("ProbeControl::Imported Flag: attr = "+flagName +" value = "+state);
        }
        return true;
    
    }
        
    private void writeOutDB() {
        
        //Make sure that there is something to store. If not, delete the DB file.
        if (flags.size() < 1 ) {
            File dbFile = new File(g_flagDBFileName);
            if (dbFile.exists())
                dbFile.delete();                
            return;
        }
        
        //Load DB of flags, if one.        
        try {
            File dbFile = new File(g_flagDBFileName);
            
            //Delete file & create new one...
            if (dbFile.exists())
                dbFile.delete();                
            dbFile.createNewFile();
           
            //Open file for writing
            FileWriter writer = new FileWriter(dbFile);

            PrintWriter toDB
            = new PrintWriter(new BufferedWriter(writer));

            java.util.Enumeration e = flags.keys();
            while (e.hasMoreElements()) { 
                String flag = (String)e.nextElement();
                String state= (String)flags.get(flag);
                toDB.println(flag+"="+state);            
            }
            //All done, close file.
            toDB.close();

//        } catch (java.io.FileNotFoundException fnf) {
            //Assume that the file never existed, so we have no flags to load. OK to stop.
//            System.out.println("ProbeControl::No flag db found... OK");
        } catch (java.io.IOException ioe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "ProbeControl::IO Exception writing file...", ioe);
        }
        
    }
    
}
