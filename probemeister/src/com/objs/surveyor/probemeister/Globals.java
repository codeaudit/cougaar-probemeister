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

package com.objs.surveyor.probemeister;
import java.util.Hashtable;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

/* A place to store values needed elsewhere. Var names must be defined in GlobalVars. */
public class Globals {
    
    static Globals globals;
    Hashtable hash;
    
    static {
        globals = new Globals();  
        
        //At some pt these should be set by a prefs file...
        globals.put(GlobalVars.INSTALL_BREAKPOINT_BOOLEAN, Boolean.TRUE);
        globals.put(GlobalVars.SAVE_BYTECODE_TO_FILE_BOOLEAN, Boolean.TRUE);
        globals.put(GlobalVars.SEND_BYTECODE_TO_CONSOLE_BOOLEAN, Boolean.FALSE);  
//        globals.put(GlobalVars.BREAKPOINT_CLASS_NAME, "projectFiles/com/objs/surveyor/probemeister/bytecoder/util/Breakpointer.class");
        globals.put(GlobalVars.BREAKPOINT_CLASS_NAME, "OBJS_Breakpointer.class");
        globals.put(GlobalVars.RMI_PORT, new Integer(-1));
        globals.put(GlobalVars.RMI_ACTIVE, Boolean.FALSE);
        globals.put(GlobalVars.RMI_FILESERVER, "http://127.0.0.1:1100");
        globals.put(GlobalVars.RMI_FILESERVER_DIR, "RMISTUBS");
        globals.put(GlobalVars.RMI_POLICY_FILE, "./policy");
        globals.put(GlobalVars.RMIREGISTRY, "127.0.0.1");
        globals.put(GlobalVars.ANNOUNCE_PROBES, Boolean.TRUE);
        globals.put(GlobalVars.DEPLOY_CONFIG_AT_STARTUP, "");
        //globals.put(GlobalVars.USE_PM_AS_LOGGER_MONITOR, Boolean.FALSE);
        
    }
    
    
    /* Will output the modified method bytecode to the
     * DOS Console (even if the modification is corrupt)
     */
    static public boolean sendBytecodeModsToDOSConsole() { return false;}

    /* If true, will save each modified class to a new file, even if
     * there is an error modifying the code.
     */
    static public boolean saveBytecodeModsToFile() { return true; }
    
    private Globals() { hash = new Hashtable(); }    
    static public Globals globals() { return globals; }
    
    public GlobalVars globalForName(String _name) {
        java.util.Enumeration keys = hash.keys();
        while (keys.hasMoreElements()) {
            GlobalVars gv = (GlobalVars)keys.nextElement();
            if (gv.toString().equals(_name))
                return gv;
        }
        return null;
    }
    
    public Object get(GlobalVars _o) { return hash.get(_o); }
    
    public void put(GlobalVars _o1, Object _o2) { 
        hash.put(_o1, _o2); 
        com.objs.surveyor.probemeister.Log.out.info("Globals::Added "+_o1+"="+_o2);
    }

    public void importGlobalSettings(String _file) {
        
        readInSettings(_file);

    }


    private void readInSettings(String _file) {
    
        //Load settings file
        File initFile = new File(_file);
        try {

            //Open file
            FileReader reader = new FileReader(initFile);
            com.objs.surveyor.probemeister.Log.out.info("Globals::===> Global Settings Being Applied: "+initFile.getAbsolutePath());
                        
            //Read data
            //1. Set up BufferedReader which has a readLine() method
            BufferedReader bReader = new BufferedReader(reader);  
            for (;;) { 
                String data = bReader.readLine();
                if (data != null) { // non-null until EOF
                    if (data.length() < 5 || data.startsWith("#")) {
                        com.objs.surveyor.probemeister.Log.out.finest("Globals::ignoring line in file - comment or length <5");
                        continue;   //essentially empty line
                    }
                    //System.out.println("Read:      "+ String.valueOf(data));
                    //Parse & Add to the list of plugCatalog 
                    if (!processEntry(data)) { //Error processing data, try to keep processing
                        com.objs.surveyor.probemeister.Log.out.severe("Globals::Error in Global settings file: "+initFile.getAbsolutePath());            
                        com.objs.surveyor.probemeister.Log.out.severe("Globals::=> Each line should contain <GlobalName>=<value>");
                    } else
                        com.objs.surveyor.probemeister.Log.out.finest("Globals::=> Global settings "+data+" has been imported.");
                } else { //no more data, EOF
                    break; 
                }
            }
            //All done, close file.
            bReader.close();

        } catch (java.io.FileNotFoundException fnf) {
            //Assume that the file never existed, so we have no plugCatalog to load. OK to stop.
            com.objs.surveyor.probemeister.Log.out.severe("Globals::No Global settings found. Looking for: "+initFile);
        } catch (java.io.IOException ioe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE, "Globals::IO Exception reading file...", ioe);
        } catch (NullPointerException npe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE, "Globals::IO Exception reading file...", npe);            
        }
    }
    
    
    
    /**
      * This routine extracts the var name & value from the text line.
      * It locates the correct global variable & converts the value
      * accordingly.
      */
    private boolean processEntry(String _entry) {
        
        String globalVarName = null;
        String value = null;

        //Delimiter is a '='
        java.util.StringTokenizer st = new java.util.StringTokenizer(_entry, "=");
        
        //Process Data - extract var name & value
        if (st.hasMoreTokens()) 
           globalVarName = st.nextToken();

        if (globalVarName == null) //no go, stop processing this entry
            return false;
        
        if (st.hasMoreTokens()) { //then get the value
            value = st.nextToken();
        }

        if (value == null) //no go, stop processing this entry
            return false;
        
                
        //Now, find the GlobalVar

        GlobalVars gv = globalForName(globalVarName);
        if (gv == null)  // no such gv
            return false;
            
        Object objValue = null; //will hold the value cast into the correct typed 
            
        //Now find out the type of the current value to determine
        //how to cast the read in value.
        Object vt = this.get(gv);
        if (vt instanceof String)
            objValue = value; 
        else
        if (vt instanceof Integer) {
            try {
                objValue = new Integer(value);
            } catch(NumberFormatException nfe) {
                com.objs.surveyor.probemeister.Log.out.warning("Argument in globals file is not a number=> "+globalVarName+"="+value);            
            }
        }
        else
        if (vt instanceof Boolean) {
            objValue = new Boolean(value);
        }
       
        if (objValue == null) 
            return false;
        else {
            this.put(gv, objValue);
            return true;
        }    
    }
}

