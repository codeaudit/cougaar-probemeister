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
 

package com.objs.surveyor.probemeister.runtime;

import com.objs.surveyor.probemeister.coverage.CoverageManager;
import com.objs.surveyor.probemeister.*;
import com.objs.surveyor.probemeister.bytecoder.*;
import com.objs.surveyor.probemeister.probe.ProbeCatalog;
import com.objs.surveyor.probemeister.probe.ProbeType;
import com.objs.surveyor.probemeister.probe.ProbeInterface;
import com.objs.surveyor.probemeister.probe.DehydratedProbe;
import com.objs.surveyor.probemeister.probe.ProbeInfoDialog;
import com.objs.surveyor.probemeister.bytecoder.BytecodeLocation;
import com.objs.surveyor.probemeister.probe.NoSuchProbeException;
import com.objs.surveyor.probemeister.bytecoder.UnsupportedFunctionException;
import javax.swing.JOptionPane;

import com.sun.jdi.*;
//import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
//import com.sun.jdi.connect.*;
import java.util.*;
import java.io.*;

public class RuntimeClassManager {
     
    static RuntimeClassManager mgr;
    
    //No need for more than one of these...
    static {
        mgr = new RuntimeClassManager();
    }
    public static RuntimeClassManager getMgr() { return mgr; }


    public PMClass getPMClass(Object _o, TargetVMConnector _tvmc) {        
        
        return PMClass.wrapClass((ClassType)_o, _tvmc);                
    }


    /* Get list of classes - should only be called once, as all
     * previous ClassType references from previous calls will be invalidated
     */
    public PMClass[] getClassList(TargetVMConnector _tvmc) {

        //_tvmc.updateClassesList();
	    List cos = _tvmc.getLoadedEditableClasses();
        if (cos == null) return null;
        
        return PMClass.wrapClassList(cos, _tvmc);        
    }
/*    
    public List getVarsForMethod(TargetVMConnector _tvcm, PMClass _rt, PMMethod _meth) {

        try {
            ClassObject co = null;
            co = _tvcm.getClassMgr().getClassObject(_rt.getReferenceType());
            MethodObject mo = co.getMethodObject(_meth.getMethod());
            return mo.getLocalVars();
                
        } catch (Exception e) {
            Vector v = new Vector(1);
            v.add("Variables Not Accessible");
            return v;
        }
    }
    

    public List getProbesForMethod(TargetVMConnector _tvcm, PMClass _rt, PMMethod _meth) 
        throws NotAvailableException {
        try {
            ClassObject co = null;
            co = _tvcm.getClassMgr().getClassObject(_rt.getReferenceType());
            MethodObject mo = co.getMethodObject(_meth.getMethod());
            if (mo != null)
                return (List) mo.getDehydratedProbeList();
            else               
                return null;
        } catch (Exception e) {
            throw new NotAvailableException();
        }
    }
*/
    public String[] getProbeTypeList() {
        return ProbeCatalog.getProbeTypeNames();
    }
    
    public boolean isProbeAStub(String _probeName) {
        return ProbeCatalog.isStub(_probeName);  
    }
    
    
/*
    public boolean removeProbe(TargetVMConnector _tvcm, PMClass _rt, PMMethod _meth, Object _probe) 
        throws NotAvailableException {
        try {

            ClassObject co = null;
            co = _tvcm.getClassMgr().getClassObject(_rt.getReferenceType());
            MethodObject mo = co.getMethodObject(_meth.getMethod());
            try {
                return mo.removeProbe((DehydratedProbe)_probe);
            } catch (NoSuchProbeException nsp) {
                System.out.println("Error: No such probe found. Cannot remove probe.");                
               return false;
            } 
        } catch (Exception e) {
            throw new NotAvailableException();
        }
    }

    public boolean addProbe(TargetVMConnector _tvcm, PMClass _rt, PMMethod _meth, String _probeType) 
        throws NotAvailableException {
        try {

        System.out.println("RuntimeClassManager: Called to add probe.");
           
            //create probe
            ProbeType pt = ProbeCatalog.getProbeTypeByName(_probeType);
            if (pt != null) { 
                System.out.println("RuntimeClassManager: Creating loc object");
                BytecodeLocation loc = new BytecodeLocation(_tvcm.getClassMgr() , (com.sun.jdi.ClassType)_rt, 
                                                        (com.sun.jdi.Method)_meth, 0); 

                System.out.println("RuntimeClassManager: Creating probe of type: "+pt.getClass().getName());
                ProbeInterface pi = pt.generateProbe(loc);

                if (pi != null) //then the probe was successfully created
                    return true;                                                                        
                else
                    return false;
            } else {
                System.out.println("Error: could not instantiate probe type. Cannot add probe.");
                return false;   
            }        
        } catch (Exception e) {
            throw new NotAvailableException();
        }
    }
*/
    //A class in the debugee VM is replaced with local definition of the class.
    //The class is a local file.
    public boolean redefineClass(TargetVMConnector _tvmc, String className, String filename)  
        throws ClassNotFoundException, java.io.IOException, MultipleClassNamesException,
        OperationNotSupported {

        //Check to see if the VM supports this capability
        if (!_tvmc.vm().canRedefineClasses())
            throw new OperationNotSupported();

        ReferenceType refType = _tvmc.findRefType(className);
        if (refType == null) {
            com.objs.surveyor.probemeister.Log.out.warning("No class named '" + className + "' found.");
            throw new java.lang.ClassNotFoundException();
        }
            
        File phyl = new File(filename);
        byte[] bytes = new byte[(int)phyl.length()];
        try {
            InputStream in = new FileInputStream(phyl);
            in.read(bytes);
            in.close();
        } catch (Exception exc) {
            com.objs.surveyor.probemeister.Log.out.warning("Error reading '" + filename + 
                        "' - " + exc);
            throw new java.io.IOException(exc.toString());
        }
        Map map = new HashMap();
        map.put(refType, bytes);
        try {
            _tvmc.vm().redefineClasses(map);
            return true;
        } catch (UnsupportedOperationException uoe) {
            throw new OperationNotSupported();        
        } catch (Throwable exc) {
            com.objs.surveyor.probemeister.Log.out.warning("Error redefining " + className + 
                        " to " + filename + " - " + exc);
            return false;                        
            //throw RedefineClassException(exc.toString());
                
            //Can throw:
                //ClassFormatError
                //ClassCircularityError 
                //VerifyError 
                //UnsupportedClassVersionError
        }
    }

    public String getClassNameFromFile(String filename) {
        
        return Bytecoder.getClassNameFromClassFile(filename);
    }
    
/*
    public void displayProbeInfo(java.awt.Window _window, Object _probe) {        
        
        if (_probe instanceof DehydratedProbe) {
            DehydratedProbe dp = (DehydratedProbe)_probe;
            
            //Process parameters embedded in probe for viewing
            ProbeType pt = ProbeCatalog.getProbeTypeByName(dp.getType());
            String[] params = dp.getEmbeddedParameters();
            if (pt !=null && params!=null) 
                params = pt.prettyPrintParamList(params);
            
            //Create dialog for viewing
            ProbeInfoDialog dialog = new ProbeInfoDialog("Probe Information");
            BytecodeLocation bLoc = (BytecodeLocation)dp.getLocation();
            
            //Populate dialog with values
            dialog.configure(dp.getID(), dp.getType(), dp.getDesc(), 
	                      bLoc.getClassName(), bLoc.getMethodName(), bLoc.getOffsetAsString(),
	                      dp.getEmbeddedParameters());
            
            dialog.setVisible(true);            
        } else {
    	    JOptionPane.showMessageDialog(_window, 
	            "No information can be displayed at this time.", "Error", JOptionPane.INFORMATION_MESSAGE);		    
            System.out.println("RTClassMgr::Cannot display probe info -- not a dehydrated probe!");
        }
    }
*/


}


