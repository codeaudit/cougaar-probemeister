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
 * Used by TargetVMDataPanel class tree list to filter class list
 * for user viewing
 */
package com.objs.surveyor.probemeister.gui;

import java.util.Vector;
import java.util.Hashtable;

public class MethodFilterList {
    
    Hashtable classes; //class names will be unique
    
    public MethodFilterList() {
        classes = new Hashtable();
    }
    
    /* Adds a class and method to the list. 
     * The method name must include the signature, e.g. main(java.lang.String[]),
     * the method name concatentated with it's argument types in parentheses.
     */
    public void addMethod(String _clsName, String _meth) { 
        
        Vector v = (Vector)classes.get(_clsName);
        if (v == null) { // create a new entry with vector to hold method names
            v = new Vector();
            v.addElement(_meth);
            classes.put(_clsName, v);                                
        } else { //just add method name to list of methods
            v.addElement(_meth);
        }    
    }
    
    public Vector getMethods(String _clsName) {
        return (Vector)classes.get(_clsName);
    }
    
    public java.util.Enumeration getClassList() {
        return classes.keys();
    }

    public boolean containsClass(String _clsName) {
        return classes.containsKey(_clsName);
    }

    public boolean containsMethod(String _clsName, String _methName) {
        
        Vector meths = getMethods(_clsName);
        if (meths != null) {
            //loop thru looking for meth
            java.util.Enumeration e = meths.elements();
            while (e.hasMoreElements())
                if (_methName.equals((String)e.nextElement()))
                    return true;
        }        
        return false;
    }
   
}