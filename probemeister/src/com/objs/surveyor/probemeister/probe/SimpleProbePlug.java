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


public class SimpleProbePlug extends ProbePlug {

    public static void PP_printLocation() {
        Throwable thr = new Throwable();
        StackTraceElement[] callstack = thr.getStackTrace();
        if (callstack.length >1) { //grab calling method
            String cls = callstack[1].getClassName();
            String meth = callstack[1].getMethodName();
            System.out.println("SimpleProbePlug called with no args by: "+cls+":"+meth);    
        } else
            System.out.println("SimpleProbePlug called with no args by: <stack trace problem>");    
   }
    
    public static void PP_printLocationAndArgs(GenericArgumentArray o) {
        String len = (o != null) ? ""+o.length() : "null";
        System.out.println("SimpleProbePlug called with "+len+" Args!! Probed Method's Arguments:");
        String[][] args = new String[0][0];
        if (o.length()>0) {
            args = new String[o.length()][2];
            for (int i=0; i<o.length(); i++) {
                //System.out.println("\n *** Object "+i+" ***", Print.D0);        
                String o1 = o.getName(i);
                o1=o1.replace('@', '_');
                o1 = "Arg"+i+"_"+o1;        
                String o2 = (o.getValue(i) != null) ? o.getValue(i).toString() : "null";
                o2=o2.replace('@', '_');
                //args[i][1] = o2;
                System.out.println(o1 + " = " + o2);        
            }
        } 
        
        //Calling Probe plug in... should be dynamic (not statically assigned probe)
        //com.objs.dasada.siena.geoworlds.probes.ProbeManager.handleEvent("ComponentCalledEvent","JBCI_Inserted_Probe", Thread.currentThread(), args);
        
    }
    
}