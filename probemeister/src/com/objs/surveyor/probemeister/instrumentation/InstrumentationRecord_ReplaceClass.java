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

package com.objs.surveyor.probemeister.instrumentation;

import com.objs.surveyor.probemeister.TargetVMConnector;
import com.objs.surveyor.probemeister.probe.DehydratedProbe;
import com.objs.surveyor.probemeister.runtime.RuntimeClassManager;

//This class is holds the instrumentation details for a
//given modification event of a target VM.
public class InstrumentationRecord_ReplaceClass implements InstrumentationRecord {

    private long time=0;
    private String className=null;
    private String fileLocation=null; // the file it was replaced with
    
    public InstrumentationRecord_ReplaceClass() {}
    public void setClassName(String _c) {className = _c;} 
    public void setTime(long _t) {time = _t;}    
    
    private String error = null;
    public String getError() {return error;}
    public void setError(String _s) {error = _s;}

    public String getID() {return fileLocation+className;}
    
    //Pass in name/path of replacement file and name of class that was replaced
    public InstrumentationRecord_ReplaceClass(String _loc, String _class) {
        fileLocation = _loc; //location of file used to replace class
        className = _class;  //full name of class
        time = getTimeNow();
    }
            
    protected long getTimeNow() { return System.currentTimeMillis(); }
    
    public String getType() {return "Class Replacement";}
    public String getClassName() {return className;} //at least we should know this for every type
    public long getTime() {return time;}

    public String getFileLocation() {return fileLocation;} //at least we should know this for every type

    public boolean reapplyAction(TargetVMConnector _tvmc) {  
        try {
            return RuntimeClassManager.getMgr().redefineClass(_tvmc, className, fileLocation);
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Exception while reapplying class redefinition in InstrumentationRecord_ReplaceClass: "+e);
            return false;
        }
    }

    //Add subtype-specific attrs to InstrumentationRecordSerialized
    public void serializeAttrs(InstrumentationRecordSerialized _irs) {
        _irs.addAttrValPair("File", fileLocation);
    }

    public void processAttribute(String _name, String _value) {
        if (_name.equals("File"))
            fileLocation = _value;
        else
            com.objs.surveyor.probemeister.Log.out.warning("InstrumentationRecord_ReplaceClass.processAttribute() saw attrName it did not recognize.");
    }

    public void processGroup(String _name, java.util.Map _values) {
        //if (_name.equals("Params")) this.probeParams = _values;
    }

    public String toString() {
        return this.getClass().getName()+":: class="+getClassName()+"  file="+this.getFileLocation();
    }

}