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
import com.objs.surveyor.probemeister.probe.ProbeInterface;
import com.objs.surveyor.probemeister.probe.ProbePlugEntry;
import com.objs.surveyor.probemeister.bytecoder.BytecodeProbeType;
import com.objs.surveyor.probemeister.bytecoder.BytecodeLocation;
import com.objs.surveyor.probemeister.bytecoder.Stub_BytecodeSkeleton;
import com.sun.jdi.Method;
import com.sun.jdi.ClassType;
import java.util.Map;
import java.util.Iterator;

//This class is holds the instrumentation details for a
//given bytecode probe stub modification event of a target VM.
//It has all nec. data to reconstruct a Stub_BytecodeSkeleton object 
//used to reinsert a given probe.
public class InstrumentationRecord_Stub implements InstrumentationRecord {

    private long time=0;
    private String targetClass=null; 
    //private TargetVMConnector tvmc=null;
    private String probeID;
    private String desc; 
    private String probeTypeClassName; 
    private String targetMethod=null; 
    private int byteOffset=0;
    private int aprepend=0; //default to prepend
    private String targetMethodSig=null;
    private BytecodeLocation loc=null; 
    private Map probeParams = null;;
    private Map plugParams = null;
       
    public InstrumentationRecord_Stub() {
        
    }
    public void setClassName(String _c) {targetClass = _c;} 
    public void setTime(long _t) {time = _t;}

    private String error = null;
    public String getError() {return error;}
    public void setError(String _s) {error = _s;}


    //Pass in probe info and params used by probe
    public InstrumentationRecord_Stub(String _probeID, String _desc, BytecodeProbeType _pt, BytecodeLocation _loc, Map _probeParams, Map _plugParams) {

        probeID = _probeID;
        desc = _desc;
        probeTypeClassName = _pt.getClass().getName();
        loc = _loc;
        probeParams = _probeParams;
        plugParams = _plugParams;
        
        targetClass = loc.getClassName();
        //time = getTimeNow();
    }

    protected long getTimeNow() { return System.currentTimeMillis(); }
    
    public String getID() {return probeID;}
    public String getType() {return "Probe Insertion";}
    public String getClassName() {return targetClass;} //at least we should know this for every type
    public long getTime() {return time;}

    public String getMethodName() {
        if (loc != null)
            return loc.getMethodName();
        else return null;
    }

    public String getMethodSig() { return this.targetMethodSig; }
    public String getMethodNSig() {
        String mName = targetMethod;
        int i = targetMethod.indexOf('(');
        if (i>0)
            mName = targetMethod.substring(0, i);
        return mName+targetMethodSig;
    } 
    
    //apply instrumentation to specified VM
    public boolean reapplyAction(TargetVMConnector _tvmc) {
        
        //Recreate Location --------------------
        loc = new BytecodeLocation();
        //Make sure to set target VM Connector in loc before proceeding...
        loc.setClassMgr(_tvmc.getClassMgr());
        loc.setOffset(byteOffset);
        loc.setAprepend(aprepend);
        //initialize loc with Class, Method, and sig info
      
        try {
            if (!loc.setClassAndMethodAsStrings(targetClass, targetMethod, targetMethodSig)) {
                com.objs.surveyor.probemeister.Log.out.warning("Could not find method and/or class. Cannot apply configuration file.");
                return false;
            }
        }
        catch (NoClassDefFoundError cd) { //maybe the class was not loaded yet, 
                                          //queue this record for later processing
            _tvmc.getLoadtimeManager().addRecordToApply(this);
            com.objs.surveyor.probemeister.Log.out.warning("Instrumentation Exception while applying action: Class not found ("+targetClass+"), queueing for later processing");
            return true; // return true because we really didn't fail...yet.
        }
        catch (NoSuchMethodError e) {
            com.objs.surveyor.probemeister.Log.out.warning("Instrumentation Exception while applying action: No Such Method: "+targetClass+":"+targetMethod+":"+targetMethodSig);
            return false;
        }
        catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Instrumentation Exception while applying action: "+e);
            return false;
        }
        
        //Recreate ProbePlugEntry ---------------------
        ProbePlugEntry plug = new ProbePlugEntry();
        plug.setParamsMap(plugParams);                        
        
        //Do it (create & insert probe) ------------------------------
        ProbeInterface pi = null;
        //Create new ProbeType object
        try {
            if (probeTypeClassName != null) {
                Class ptClass = this.getClass().forName(probeTypeClassName);
                java.lang.reflect.Method getStubMethod = ptClass.getDeclaredMethod("getStub", null);
                Stub_BytecodeSkeleton pt = (Stub_BytecodeSkeleton) getStubMethod.invoke(null, null) ;
                pi = pt.generateProbe(probeID, desc, loc, probeParams, plug);                            
                if (pi== null) {
                    com.objs.surveyor.probemeister.Log.out.warning("Instrumentation Exception while applying action: There were problems generating this probe.");
                    return false;
                }
            } else {
                com.objs.surveyor.probemeister.Log.out.warning("Instrumentation Exception while applying action: No ProbeType specified.");
                return false;
            }
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Instrumentation Exception while applying action: "+e);
            return false;
        }
        
        if (pi == null)     
            return false;
        else
            return true;
    }

    //Add subtype-specific attrs to InstrumentationRecordSerialized
    public void serializeAttrs(InstrumentationRecordSerialized _irs) {
        
        _irs.addAttrValPair("probeID", probeID);
        _irs.addAttrValPair("probeDesc", desc);
        _irs.addAttrValPair("probeType", probeTypeClassName);
        
        _irs.addAttrValPair("targetMethod", InstrumentationRecorder.xmlizeString(loc.getMethodName()));
        _irs.addAttrValPair("targetMethodSig", loc.getMethod().signature());
        _irs.addAttrValPair("byteOffset", loc.getOffsetAsString());
        _irs.addAttrValPair("aprepend", loc.getAprependAsString());
        
        //Add probe specific params
        _irs.addGroup("Params", probeParams);            
        _irs.addGroup("Plug",   plugParams);            
        
    }

    //Called when this object is being deserialized
    public void processAttribute(String _name, String _value) {
        if (_name.equals("probeID")) probeID = _value;
        else
        if (_name.equals("probeDesc")) desc = _value;
        else
        if (_name.equals("probeType")) probeTypeClassName = _value;
        else
        if (_name.equals("targetMethod")) targetMethod = _value;
        else
        if (_name.equals("targetMethodSig")) targetMethodSig = _value;
        else
        if (_name.equals("byteOffset")) byteOffset = Integer.valueOf(_value).intValue();
        else
        if (_name.equals("aprepend")) aprepend = Integer.valueOf(_value).intValue();
        else
            com.objs.surveyor.probemeister.Log.out.warning("InstrumentationRecord_Stub saw attr type it doesn't know: "+_name);
    }

    public void processGroup(String _name, Map _values) {
        if (_name.equals("Params")) this.probeParams = _values;
        else if (_name.equals("Plug")) this.plugParams = _values;
    }

    public String toString() {
        return this.getClass().getName()+":: loc="+getClassName()+"."+this.getMethodName()+"  probeType="+probeTypeClassName;
    }

}