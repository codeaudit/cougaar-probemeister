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
// Object Services & Consulting, Inc. All Rights Reserved. 2001
// Author: Paul Pazandak
// Date:   25-Apr-01
package com.objs.surveyor.probemeister.probe.util;
import java.io.*;
import java.util.Vector;

//This class is used to parse and return a structure representing 
//a stack trace. This is platform-specific apparently as different
//OS versions output the stacktrace differently. This code could be 
//extended to support other OSs in addition to WinOS.

//This is superceded by support in JDK 1.4... update all affected 
//code as needed

public class StackTraceParser {
    
    static final String AT = "\tat "; // precedes each line of trace
    
    //Test code
    public static void main(String[] args) {
        Throwable g = new Throwable();
        parseStackTrace(g);
        (new StackTraceParser()).foo(g);
    }
    void foo(Throwable g0){
        Throwable g1 = new Throwable();
        parseStackTrace(g1);
        parseStackTrace(g0);
    }
    //end Test code
    
    public static StackTrace parseStackTrace(Throwable t) {    
    
        StringWriter out = new StringWriter();
        t.printStackTrace(new PrintWriter(out));
        StackTrace st = parseTrace(out.toString());
            
    /*        System.out.println("Exception: "+st.getExceptionDesc());
            Vector v = st.getTraces();
            if (v!= null) {
                TraceItem[] ti = new TraceItem[v.size()];
                v.copyInto(ti);
                for (int ii=0; ii<v.size(); ii++) {
                System.out.println("\tItem: "+ti[ii].getClassName()+ " -- " + ti[ii].getMethodName());    
                }
            }
    */        
        return st;    
    }

    static StackTrace parseTrace(String trace) {
        
        String exceptStr = "";
        
        Vector traces = new Vector();
        //Locate start of first trace - before is exception desc.
        int start = trace.indexOf(AT); 
        
        if (start > 0) 
            exceptStr = trace.substring(0, start-2);
        else
            return (new StackTrace(traces,"ERROR")); //no traces found.
        
        while (true) {
            String cls=null;
            String meth=null;
            
            //Move ptr past "    at "
            start += AT.length();
            trace = trace.substring(start);
            //find end of class/method name
            int end = trace.indexOf('(', 0);
            //extract this line into new string
            if (end < 0) { //dead line... stop
                //System.out.println("#Cannot find '(' in trace\n:"+trace);
                break;
            }
            String traceLine = trace.substring(0, end);
            //locate end of classname
            int endClsName = traceLine.lastIndexOf('.');
            if (endClsName >0) { //then we found one
                cls = traceLine.substring(0, endClsName);
                if (endClsName < traceLine.length()-1)  //then we found method name
                    meth = traceLine.substring(endClsName+1, traceLine.length());
            }
            if (meth!=null && cls!=null)
                traces.add(new TraceItem(cls,meth));
            
            //update string ptrs, locate next "    at "
            start = trace.indexOf(AT);        
            if (start < 0)
                    break;                
        } //end while(true)
        
        return (new StackTrace(traces,exceptStr));
        
    }
        
}
