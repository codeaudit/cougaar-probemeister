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

/*
 * This class is used to hold the descriptions of ProbePlugs that have been
 * imported from the ProbePlugCatalog DB. ProbePlugs which have the same signature
 * as a given ProbeStub are compatible & can therefore be connected.
 */
public class ProbePlugEntry {
 
    String className;
    String methName;
    String desc;
    String sig;
    
    //Called when deserializing
    public ProbePlugEntry() {}
    
    ProbePlugEntry(String  _probeClassName, String _methodName, String _desc, String _sig) {
        
        className = _probeClassName;        
        methName = _methodName;
        desc = _desc;
        sig = _sig;
    }
    
    public String getClassName() {return className;}
    public String getMethodName() {return methName;}
    public String getDesc() {return desc;}
    public String getSig() {return sig;}
    
    public java.util.Map getParamsMap() {
        
       java.util.Hashtable h = new java.util.Hashtable(4);
       h.put("className", className);
       h.put("methName", methName);
       h.put("desc", desc);
       h.put("sig", sig);
       return h;
    }
        
    public void setParamsMap(java.util.Map _map) {
        
        if (_map == null) {
            com.objs.surveyor.probemeister.Log.out.warning("ProbePlugEntry.setParamsMap:: map is null.");
            return;
        }
        
        className = (String)_map.get("className");
        methName = (String)_map.get("methName");
        desc     = (String)_map.get("desc");
        sig      = (String)_map.get("sig");
    }

}

