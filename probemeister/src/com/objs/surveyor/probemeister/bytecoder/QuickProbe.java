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

package com.objs.surveyor.probemeister.bytecoder;

import com.objs.surveyor.probemeister.*;
import com.objs.surveyor.probemeister.probe.*;

import com.sun.jdi.*;

import java.util.*;
import java.io.*;


public class QuickProbe {
	// The following function is a placeholder for control initialization.
	// You should call this function from a constructor or initialization function.
	public void vcInit() {
		//{{INIT_CONTROLS
		//}}
	}
	


    public static boolean installSimplePrintProbe(Object _refType, Object _method, String _s, boolean reload) {

/* taken out of service -         
        //Get handle on objects / vars we need (ClassObject & method name)
        ClassObject co  = ClassObject.getClassObject((com.sun.jdi.ReferenceType)_refType);
        if (reload) //reload class -- removes all past inserted probes
            co.reloadClass();
        MethodObject mo = co.getMethodObject((com.sun.jdi.Method)_method);
              
        //Create bytecode
        StatementList sl = co.createStatementList();
        try {
            StatementFactory.createPrintlnStmt(sl, _s);
        } catch (Exception e) {}
        
        //Now modify the method - insert the bytecode
        System.out.println("Length of stmt list = "+sl.list().size());
        SimpleProbe sp = new SimpleProbe("SP-ID", "A quick probe");
        mo.insertStatementList(sp, sl, (com.objs.surveyor.probemeister.probe.Location)null);        
	    boolean success = co.postUpdates();
	    if (success) 
	        System.out.println("Simple Probe Installed.");
	    else        
            System.out.println("Simple Probe NOT Installed.");
        
        
        //*************************
        //***Will likely need to capture exceptions... ***
        return success;
*/
    return false;
    }
	//{{DECLARE_CONTROLS
	//}}
}