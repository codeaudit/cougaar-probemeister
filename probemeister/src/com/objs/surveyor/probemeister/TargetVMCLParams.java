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

 */
 
 
package com.objs.surveyor.probemeister;
import java.util.Hashtable; 
 
/* Commandline parameters of each TargetVM 
 * All command line parameters must be defined in this
 * class. After the TargetVM starts, if started suspended=y,
 * the values of the parameters may be retrieved from this class.
 */
public class TargetVMCLParams {
 
    Hashtable params; //not static - each TargetVMConnector has its own
    //Define all possible commandline parameters here.
    public static final String APPNAME  = "pmAPPNAME";
    public static final String APPADDR  = "pmADDR";
    public static final String LOCALJAR = "pmHASJAR";

    //All statics defined above must be defined here too
    final static String[] pNames = {APPNAME, APPADDR, LOCALJAR};
    
    public TargetVMCLParams() {        
        params = new Hashtable(5);  
        //Populate with default values
        params.put(APPNAME, "Unknown");
        params.put(APPADDR, "Unknown");
        params.put(LOCALJAR, "FALSE");
    }
 
    public void put(String _k, String _v) { 
        params.put(_k, _v); 
        com.objs.surveyor.probemeister.Log.out.finer("TargetVMCLParams:: put "+_k+" - "+_v);    
    }
    public String get(String _k) { return (String)params.get(_k); }
    public String[] getParamNames() { return pNames; }
}