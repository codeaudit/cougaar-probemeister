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

import com.objs.surveyor.probemeister.probe.NoSuchProbeException;
import com.objs.surveyor.probemeister.bytecoder.*;
import com.objs.surveyor.probemeister.probe.*;

import com.sun.jdi.Method;
import com.sun.jdi.ClassType;
import java.io.*;
import java.util.Iterator;
import java.util.List;

import jreversepro.reflect.JMethod;
import jreversepro.reflect.JClassInfo;
import jreversepro.parser.ClassParserException;
import jreversepro.parser.JClassParser;

public class PMMethod {
    
    static jreversepro.revengine.JSerializer serializer;
    static {
        serializer = new jreversepro.revengine.JSerializer();
    }
    static JClassParser parser = new JClassParser();
    
    private Method meth;
    private PMClass cls;
    private boolean reloadSource = false; //True if method has been modified 
                                          //since source last loaded
    private boolean modified = false; //True if method has ever been modified            
    private JClassInfo jClassInfo = null;
    
    public PMMethod (Method _meth, PMClass _cls) {
        meth = _meth;
        cls = _cls;
    }

    public PMClass getPMClass() { return cls; }
    public Method getMethod() { return meth; }
    public String name() { return meth.name(); }
    public String nameNSig() {         
        try {
            
            String args = buildArgString(false);
            return meth.name()+args; 
        } catch (Exception e) {
            return "Not Available";
        }
    }
    public String toString() { 
        try {
            
            String args = buildArgString(true);
            return meth.name()+args; 
        } catch (Exception e) {
            return "Not Available";
        }
    }
    
    /* builds arg list. if _pretty is TRUE, adds spaces for legibility
     * If false, builds string that can be used for method equivalence
     */
    String buildArgString(boolean _pretty) {
        String sp = (_pretty ? " " : ""); 
        java.util.List l = meth.argumentTypeNames();
        if (l==null) return sp+"("+sp+")";
        String s=sp+"("+sp;
        for (int i=0; i<l.size(); i++)
            s += l.get(i) + ((i==l.size()-1)?"":", "+sp);
        
        return s +=sp+")";         
    }
  
    public PMProbe[] getProbes()         
        throws NotAvailableException, CodeNotAccessibleException {
        try {
            ClassObject co = null;
            co = cls.getTargetVM().getClassMgr().getClassObject(cls.getReferenceType());
            MethodObject mo = co.getMethodObject(this.getMethod());
            PMProbe[] ps = null;
            if (mo != null) {
                List l = mo.getDehydratedProbeList();
                if (l != null) {
                    ps = new PMProbe[l.size()];
                    for (int i=0; i<l.size(); i++) 
                        ps[i] = new PMProbe((DehydratedBytecodeProbe)l.get(i), this);
                }
            }
            return ps;
        } catch (CodeNotAccessibleException cna) {
            throw(cna);  
        } catch (Exception e) {
            throw new NotAvailableException();
        }        
    }    

    /* Prepend probe at offset = 0 */
    public ProbeInterface addProbe(String _probeType) throws NotAvailableException { 
        return addProbe(_probeType, 0); }        

    /* Prepend probe at offset = 0 */
    public ProbeInterface addProbe(String _probeType, int _loc) throws NotAvailableException { 
        return addProbe(_probeType, _loc, 0); }        
    
    /* Add probe at specified offset, PREPENDing (BytecodeLocation.PREPEND) or 
     * APPENDING (BytecodeLocation.APPEND)
     */
    public ProbeInterface addProbe(String _probeType, int _loc, int aprepend)        
        throws NotAvailableException {

        try {
            //create probe
            ProbeType pt = ProbeCatalog.getProbeTypeByName(_probeType);
            if (pt != null) { 

                com.objs.surveyor.probemeister.Log.out.fine("PMMethod: Creating loc object");
                BytecodeLocation loc = new BytecodeLocation( cls.getTargetVM().getClassMgr(), 
                                    (ClassType)cls.getReferenceType(), meth, _loc);                 

                if (loc == null) {
                    com.objs.surveyor.probemeister.Log.out.warning("PMMethod: No location, cannot insert probe");
                    return null;
                }
                loc.setAprepend(aprepend);
                
                com.objs.surveyor.probemeister.Log.out.fine("PMMethod: Creating probe of type: "+pt.getClass().getName());
                ProbeInterface pi = pt.generateProbe(loc);
                
                methodModified();
                return pi;

            } else {
                com.objs.surveyor.probemeister.Log.out.warning("Error: could not instantiate probe type. Cannot add probe.");
                return null;   
            }        
        } catch (Exception e) {
            e.printStackTrace();
            throw new NotAvailableException();
        }
        
    }

    public ProbeInterface addProbe(String _probeType, String _probeID, String _probeDesc, java.util.Map _params, ProbePlugEntry _plug, int _loc, int _aprepend )        
        throws NotAvailableException {

        try {
            //create probe
            ProbeType pt = ProbeCatalog.getProbeTypeByName(_probeType);
            if (pt != null) { 

                com.objs.surveyor.probemeister.Log.out.fine("PMMethod: Creating loc object");
                BytecodeLocation loc = new BytecodeLocation( cls.getTargetVM().getClassMgr(), 
                                    (ClassType)cls.getReferenceType(), meth, _loc); 

                if (loc == null) return null;
                com.objs.surveyor.probemeister.Log.out.fine("PMMethod: Creating probe of type: "+pt.getClass().getName());
                
                loc.setAprepend(_aprepend);
                
                ProbeInterface pi = null;
                if (pt instanceof Stub_BytecodeSkeleton) { //then pass in plug info
                    Stub_BytecodeSkeleton stub = (Stub_BytecodeSkeleton)pt;                    
                    pi = stub.generateProbe(_probeID, _probeDesc, loc, _params, _plug);
                } else //shouldn't occur. If so, just add as usual.
                    pi = pt.generateProbe(loc);

                methodModified();
                return pi;
            } else {
                com.objs.surveyor.probemeister.Log.out.warning("Error: could not instantiate probe type. Cannot add probe.");
                return null;   
            }        
        } catch (Exception e) {
            e.printStackTrace();
            throw new NotAvailableException();
        }
        
    }
    
    
//TargetVMConnector _tvcm, PMClass _rt, PMMethod _meth, Object _probe
    public boolean removeProbe(PMProbe _probe) 
        throws NotAvailableException {
/*        try {
            String pType = _probe.getDehydratedProbe().getType();
            BytecodeProbeType pt = (BytecodeProbeType)ProbeCatalog.getProbeTypeByName(pType);
            return pt.removeProbe(this, _probe.getDehydratedProbe().getID());
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.severe("Error: No probe type found. Cannot remove probe!!!");                
            return false;
        }
    }
*/    
        try {
            ClassObject co = null;
            com.sun.jdi.ReferenceType rt = cls.getReferenceType();
            co = cls.getTargetVM().getClassMgr().getClassObject(rt);
            MethodObject mo = co.getMethodObject(this.getMethod());
            try {
///Orig                return mo.removeProbe(_probe.getDehydratedProbe());
                String pid = _probe.getDehydratedProbe().getID();
                boolean success = mo.removeProbe(mo.findDehydratedProbe(pid));
                if (success) {//then remove the Instrumentation entry for this probe.                
                    if (!cls.getTargetVM().getClassMgr().vmConnector().recorder().removeAction(pid))
                        com.objs.surveyor.probemeister.Log.out.warning("Error. Could not remove instrumentation record. Probe was removed.");                
                }                
                methodModified();
                return success;
            } catch (NoSuchProbeException nsp) {
                com.objs.surveyor.probemeister.Log.out.warning("Error: No such probe found. Cannot remove probe.");                
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NotAvailableException();
        }
    }    
            
    /* Retrieves the local variable names defined within the method.
     * Doesn't seem too useful at this point as in most cases the
     * names have been lost.
     */
    public List getVarsForMethod() {

        try {
            ClassObject co = null;
            co = cls.getTargetVM().getClassMgr().getClassObject(cls.getReferenceType());
            MethodObject mo = co.getMethodObject(this.getMethod());
            return mo.getLocalVars();
                
        } catch (Exception e) {
            java.util.Vector v = new java.util.Vector(1);
            v.add("Variables Not Accessible");
            return v;
        }
    }
    
    //Used to track state of method (at least for optimization purposes)
    private void methodModified() {      
        reloadSource = true; //source needs to be reloaded after each modification
        modified = true;        
    }
    private boolean reloadSource() { return reloadSource; }
    private void sourceReloaded() { reloadSource = false; }
    
    public JMethod getSource() {
        
        byte[] bytes = cls.getBytes();
        if (bytes != null) {
            try {
                //Load each time as probes change the bytes
                //Optimization -- only loadClass again if probes have been added
                //since last load
                if (jClassInfo == null || reloadSource()) {
                    //sjf jClassInfo = serializer.loadClass(bytes, cls.name());
                    parser.parse((InputStream)new ByteArrayInputStream(bytes), 
				 bytes.length, 
				 cls.name());
                    jClassInfo = parser.getClassInfo();
                    jClassInfo.reverseEngineer(true);
                    sourceReloaded();
                }
                System.out.println("Reverse Engineering method = "+name());
                System.out.println("                             "+getMethod().signature());
                //sjf JMethod pMeth = jClassInfo.getMethod(name(), getMethod().signature());
                JMethod pMeth = null;
                List methods = jClassInfo.getMethods();
                Iterator i = methods.iterator();
                String name = name();
                String sig = getMethod().signature();
		while (i.hasNext()) {
		    JMethod meth = (JMethod)i.next();
		    if (meth.getName().equals(name) && meth.getSignature().equals(sig)) {
			pMeth = meth;
			break;
		    }
		}
                if (pMeth != null)
                    return pMeth;
            } catch (Exception cpe) {
                System.out.println("Reverse Engineering parsing exception "+cpe);
                return null;
            }
        } else {
            System.out.println("Reverse Engineering method -- bytes = null");
            return null;
        }
        return null;
    }
    
}
