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

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import java.util.List;

public class PMClass {
    
    private ReferenceType rt;
    private boolean modifiable;
    PMMethod[] mlist=null;
    TargetVMConnector tvmc;
    String name;
    
    public PMClass (ReferenceType _rt, TargetVMConnector _tvmc) {
        rt = _rt;
        tvmc = _tvmc;
        modifiable = (rt instanceof ClassType);
        name = rt.name();
    }

    public TargetVMConnector getTargetVM() { return tvmc; }

    public static PMClass[] wrapClassList(List _l, TargetVMConnector _tvmc) {
        
        PMClass[] clist = new PMClass[_l.size()];
        for (int i=0; i<clist.length; i++)
            clist[i] = new PMClass((ReferenceType)_l.get(i), _tvmc);
            
        return clist;        
    }

    public static PMClass wrapClass(ClassType _ct, TargetVMConnector _tvmc) {
        
        return (new PMClass(_ct, _tvmc));            
    }
    
    
    /* not for general use due to exceptions thrown if unavailable */
    public ReferenceType getReferenceType() { 
        validateRefType();
        return rt; 
    }

    public boolean isModifiable() { return modifiable; }
    //Inner classes have '$' in their names
    public boolean isInnerStaticClass() throws NotAvailableException { 
        try {
            return rt.isStatic(); 
        } catch(Exception e) {
            throw new NotAvailableException();
        }
    }
    
    public PMMethod[]  getMethods() throws NotAvailableException {
 
        validateRefType();
    
        if (mlist != null) return mlist;
        //else retrieve the methods (once)
        try {
            if (rt.isPrepared()) { //can only get methods if the class is prepared
                List ms = rt.methods();

                mlist = new PMMethod[ms.size()];
                for (int i=0; i<mlist.length; i++)
                    mlist[i] = new PMMethod((Method)ms.get(i), this);
                    
                return mlist;        
            }
            else
                throw new NotAvailableException();
        } catch (Exception e) {
            throw new NotAvailableException();
        }
    }
    
    public String name() { return name; }
    public String toString() { return name(); }
     
    ReferenceType lookAgain() {
        return tvmc.findRefType(name);        
    }
 
 
    /* Tries to access the refType to see if it is still around.
     * if not, it reloads it. Do this before every rt operation.
     */
    void validateRefType() {
        try {
            String s = rt.name();
        } catch (Exception e) { //assumes ref type is no longer available
            rt = lookAgain();   
        }
   }
    
    byte[] getBytes() {
        com.objs.surveyor.probemeister.bytecoder.ClassObject co = tvmc.getClassMgr().getClassObject(rt);
        if (co != null) {
            if (co.prepareToEdit()) {
                return co.getBytes();
            } else {
                System.out.println("Reverse Engineering method -- class is not prepared");                            
                return null;
            }
        } else {
            System.out.println("Reverse Engineering method -- classObject is null");                            
            return null;
        }
    }
    
}