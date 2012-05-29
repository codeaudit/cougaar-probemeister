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

package com.objs.surveyor.probemeister.probe;

import com.objs.surveyor.probemeister.bytecoder.*;

import java.util.Vector;
import java.util.Enumeration;

/*
 * This is the catalog for storing & searching for probe types. Probe types 
 * should be registered in the ProbeCatalog. The probe catalog will invoke
 * the register() method on each ProbeType it is made aware of. 
 *
 * 
 *
 */
public class ProbeCatalog {
    
    static Vector entries;
    static ProbeCatalog pc;
    
    static {
        pc = new ProbeCatalog();
    }
    
    ProbeCatalog() {
    
        entries = new Vector();
        //load default probe types
        //ZTestProbeType.getStub().register(this);
        Probe_InstallLogger.getStub().register(this);
        Probe_LoggerProbe.getStub().register(this);
        PrintStringProbeType.getStub().register(this);
        CallMethodProbeType.getStub().register(this);
        Stub_CallMethod.getStub().register(this);
        Stub_PassMethodArgs.getStub().register(this);
        Stub_BasicEvent.getStub().register(this);
        Stub_PassMethodArgsEvent.getStub().register(this);
        Stub_PassObjectEvent.getStub().register(this);
        CallMethodByNameProbeType.getStub().register(this);
        ReturnInstructionProbeType.getStub().register(this);
        Stub_PassMethodArgsAndString.getStub().register(this);
        Stub_PassMethodArgsThisAndString.getStub().register(this);
    }
    
    /* 
     * This method is called (e.g. by a ProbeType) to register a probe type.
     */
    //A Class object is used rather than an instance object since the former is easier
    //to serialize (e.g. to a simple string) to a db.
    public void registerType(ProbeType _pt, String _desc, boolean _stub, boolean _reconfig,
                         boolean _install, boolean _removable, boolean _burst) {

        if (_pt == null) {
            com.objs.surveyor.probemeister.Log.out.warning("Error: Attempt to add null probe type...with desc:"+_desc);
            return;                            
        }
        entries.add(new Entry( _pt, _desc, _stub, _reconfig, _install, _removable, _burst));
        
    }
 
    public static ProbeType getProbeTypeByName(String _pt) {
        
//System.out.println("ProbeCatalog -- Looking for: "+_pt);

        
        Enumeration enm = entries.elements();
        while (enm.hasMoreElements()) {
            Entry e = (Entry)enm.nextElement();
            ProbeType pt = e.pt;
//System.out.println("    ProbeCatalog -- Saw: "+pt.getName());
            if ( pt.getName().equals(_pt))
                return pt;
        }
        return null;
    }
 
    public static boolean isStub(String _pt) {
        
        Enumeration enm = entries.elements();
        while (enm.hasMoreElements()) {
            Entry e = (Entry)enm.nextElement();
            ProbeType pt = e.pt;
            if ( pt.getName().equals(_pt)) 
                return e.stub;
        }
        return false;
    }
 
    public static String[] getProbeTypeNames() {        
        String[] p = new String[entries.size()];
        Enumeration enm = entries.elements();
        int i=0;
        while (enm.hasMoreElements()) {
            Entry e = (Entry)enm.nextElement();
            ProbeType pt = e.pt;
            p[i++] = pt.getName();
        }
        return p;
    }

    class Entry {
    
        ProbeType pt;
        String desc;
        boolean stub;
        boolean reconfig;
        boolean install;
        boolean removable;
        boolean burstMode;
        
        Entry(ProbeType _pt, String _desc, boolean _stub, boolean _reconfig,
                         boolean _install, boolean _removable, boolean _burst) {
                            
            pt = _pt;
            desc = _desc;
            stub = _stub;
            reconfig = _reconfig;
            install = _install;
            removable = _removable;
            burstMode = _burst;                
        }
        
        
    }
}