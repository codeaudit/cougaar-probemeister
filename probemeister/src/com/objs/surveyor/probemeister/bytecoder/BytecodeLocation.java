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


package com.objs.surveyor.probemeister.bytecoder;
import com.objs.surveyor.probemeister.probe.Location;

import com.sun.jdi.Method;
import com.sun.jdi.ClassType;
import com.sun.jdi.ReferenceType;
import java.util.Iterator;
import java.util.List;

/* Should there be several subclasses of location?
 * e.g. sourceLoc, bytecodeLoc, etc?
 *
 *
 *
 */
public class BytecodeLocation implements Location {
    
    static final int PREPEND = 0; //insert before offset
    static final int APPEND = 1;  //insert after offset
    
    
    Method meth = null;
    ClassType cls = null;
    int offset = 0; //bytecode instruction # (index into code array)
    int aprepend = 0; //default - insert AT offset
    
    ClassObject co = null;
    MethodObject mo = null;

    //persistent
    String appName = null;
    String host = null;
    String port = null;
    
    //runtime location
    TargetVMConnector_ClassMgr classMgr = null;
    public TargetVMConnector_ClassMgr getClassMgr() { return classMgr; }

    //Used when deserializing. 
    public BytecodeLocation() {}
    
    //General instantiation routine
    public BytecodeLocation(ClassType _cls, Method _meth, int _off) {
        
        cls = _cls;
        meth = _meth;
        offset = _off;        
    }
    
    public BytecodeLocation(TargetVMConnector_ClassMgr _mgr, ClassType _cls, Method _meth, int _off) {
        cls = _cls;
        meth = _meth;
        offset = _off; 
        classMgr = _mgr;        
        offset = _off;        
    }
    
    public void setClassMgr(TargetVMConnector_ClassMgr _cm) { classMgr = _cm; }
    
    public Method getMethod() {return meth;}
    public void setMethod(Method _meth) {meth = _meth;}

    public ClassType getClassType() {return cls;}
    public void setClassType(ClassType _cls) {cls = _cls;}

    /* <b>0</b> means place as early as possible in the method. <b>-1</b>
     * means place at the end of the method, and <b>any positive valid int</b> is
     * taken as a literal instruction number. If invalid (not in range), instead of
     * throwing a nasty exception, we just place the probe at the start!
     */
    public void setOffset(int _off) { offset = _off;}

    /* Returns the bytecode instruction number - the point at which the probe is or will
     * be placed. A valid range is from 0 (the first instruction) to (# of instructions)-1.
     */
    public int  getOffset() { return offset; }

    /* <b>PREPEND</b> means insert at specified offset, all instructions from offset
     * on move down. <b>APPEND</b> means insert after specified offset.
     */
    public void setAprepend(int _ap) { 
        if (_ap==PREPEND || _ap==APPEND) aprepend = _ap;
        else
          com.objs.surveyor.probemeister.Log.out.warning("Illegal Value: Tried to set Aprepend to "+_ap);                        
    }

    /* Returns the manner in which the probe is inserted. AT the offset (PREPEND) or 
     * AFTER the offset (APPEND).
     */
    public int  getAprepend() { return aprepend; }
    public String getAprependAsString() { return ""+aprepend;}    
    
    
    public String getClassName() { return this.cls.name();}    
    public String getMethodName() { 
        String name = this.meth.toString();
        String sname = name.substring(this.cls.name().length()+1);
        int i = sname.indexOf('(');
        if (i>0)
            sname = sname.substring(0, i);
        return sname;
    }    
    public String getMethNSig() {
        String mns = meth.name() + meth.signature();
//System.out.println("BytecodeLocation getMethNSig = "+mns);
        return mns;
    }
    
    public String getOffsetAsString() { return ""+offset;}    
    
    /*
     * Get the method object for this method/location
     */
    MethodObject getMethodObject(TargetVMConnector_ClassMgr _mgr) {    
        if (mo == null) {
            mo = getClassObject(_mgr).getMethodObject(meth);                    
        }
        return mo;
    }
    
    /*
     * Get the method object for this ClassType/location
     */
    ClassObject getClassObject(TargetVMConnector_ClassMgr _mgr) {        
        if (co == null) {
            co  = _mgr.getClassObject(cls);              
            //classMgr = _mgr;
        }
        return co;
    }

    /*
     * Get the method object for this method/location
     */
    MethodObject getMethodObject() {    
        if (mo == null && classMgr != null) {
            mo = getClassObject(classMgr).getMethodObject(meth);                    
        }
        return mo;
    }
    
    /*
     * Get the class object for this ClassType/location
     */
    ClassObject getClassObject() {        
        if (co == null && classMgr != null) {
            co  = classMgr.getClassObject(cls);              
            //classMgr = _mgr;
        }
        return co;
    }

    //Returns false if not successful
    public boolean setClassAndMethodAsStrings(String _class, String _method, String _sig) 
        throws NoTargetManagerException, com.sun.jdi.ClassNotPreparedException,
        NoClassDefFoundError, NoSuchMethodException {

            if (classMgr == null)
                throw new NoTargetManagerException();
                
            //Clean up method name if nec.
            int paren = _method.indexOf('('); //getMethodName returns method+"()" appended
            if (paren >= 0)                   //so we need to remove it
                _method = _method.substring(0, paren);
            
            List classes = classMgr.vmConnector().vm().allClasses();
            Iterator iter = classes.iterator();
            boolean foundClass = false;
            while (iter.hasNext()) {
                ReferenceType rt = (com.sun.jdi.ReferenceType)iter.next();
                if (rt.name().equals(_class)) {
                    if (rt instanceof ClassType) {
                        this.setClassType((ClassType)rt);
                        foundClass = true;
                        break;
                    }
                    else
                        continue;
                }
            }
            //if we didn't find it, maybe it hasn't been loaded yet.
            //Try to cause JVM to load it...
            if (!foundClass) {
                throw new NoClassDefFoundError();
            }
/*            
System.out.println("***Force Loading");                        
                if (classMgr.vmConnector().addClassToForceLoad(_class)) {
                    try {
                        //Now wait for breakpoint to occur & force loading of class
                        Thread.currentThread().sleep(6000); 
                        Boolean done = classMgr.vmConnector().wasClassForceLoaded(_class);
                        if (done == null) { // not done yet, sleep one more time
                            Thread.currentThread().sleep(6000); 
                            done = classMgr.vmConnector().wasClassForceLoaded(_class);
                        }
                        if (done == null) { //still, then give up
                            com.objs.surveyor.probemeister.Log.out.warning("Cannot locate class ("+_class+"). Force load wait timed out.");                        
                            return false;
                        }
                        if (!done.booleanValue()) { //still, then give up
                            com.objs.surveyor.probemeister.Log.out.warning("Cannot locate class ("+_class+"). Force load was unsuccessful.");                        
                            return false;
                        }
                        classes = classMgr.vmConnector().vm().allClasses();
                        iter = classes.iterator();
                        while (iter.hasNext()) {
                            rt = (com.sun.jdi.ReferenceType)iter.next();
                            if (rt.name().equals(_class)) {
                                if (rt instanceof ClassType) {
                                    this.setClassType((ClassType)rt);
                                    break;
                                }
                                else
                                    continue;
                            }
                        }
                        if (getClassType()==null) {
                            com.objs.surveyor.probemeister.Log.out.warning("Cannot locate class ("+_class+"). Force load was unsuccessful. Class not found in list");                        
                            return false;
                        }
                        com.objs.surveyor.probemeister.Log.out.info("Force loading of "+_class+" was successful!");                        
                                                                                            
                                                            
                    } catch (InterruptedException ie) {
                        com.objs.surveyor.probemeister.Log.out.warning("Cannot locate class ("+_class+"). May not be accessible & cannot force load (thread interrupted exception).");                        
                        return false;
                    }
                } else { //cannot force load so return false
                    com.objs.surveyor.probemeister.Log.out.warning("Cannot locate class ("+_class+"). May not be accessible & cannot force load (breakpointing is not enabled).");                        
                    return false;
                }
            }
*/                        
            Method m = this.getClassType().concreteMethodByName(_method, _sig);
            if (m==null)
                throw new NoSuchMethodError(_method+_sig);
            
            this.setMethod(m);

             
            return true;
        }


}