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
import java.util.Vector;
import java.util.Enumeration;

import com.textuality.lark.Attribute;

//This may be superceded by support in JDK 1.4... update all affected 
//code as needed. JDK 1.4 supports StackTrace objects

    public class StackTrace extends com.textuality.lark.Element {
        
        String exceptionLine = null;
        Vector traces = null;
        public StackTrace() {}
        public StackTrace(Vector t, String e) {
            exceptionLine = e;
            traces = t;
        }    
        public String getExceptionDesc() {return exceptionLine;}
        public Vector getTraces() {return traces;}
        public String toXML() {
            
            String quote = "\"";
            String xml = "<StackTrace desc="+quote+exceptionLine+quote+" >";
            
            if (traces!= null) {
                Enumeration e = traces.elements();
                while (e.hasMoreElements()) {
                    TraceItem ti = (TraceItem)e.nextElement();
                    xml += "<TraceItem  class=" +quote+ ti.getClassName() + quote+ 
                                     " method=" +quote+ ti.getMethodName()+ quote+" />";
                }
            }
            xml += "</StackTrace>";
            return xml;   
        }
        public boolean stackTraceContains(String pkg) {

            Enumeration e = traces.elements();
            e.nextElement();
            while (e.hasMoreElements()) {
                TraceItem ti = (TraceItem)e.nextElement();
                if (ti.getClassName().startsWith(pkg) || 
                    ti.getClassName().startsWith("sun.misc.Launcher") || 
                    ti.getClassName().startsWith("javax.swing.plaf") || 
                    ti.getMethodName().startsWith("getBootstrapResource")) 
                    return true;
                
                //Filter out JDK core class file loading activity
                //This may also filter out appl. related activity as well
                if (ti.getClassName().equals("java.util.ResourceBundle")) {
//                    e.nextElement(); //skip one
//                    ti = (TraceItem)e.nextElement();                
//                    if (ti.getClassName().startsWith("java.awt.Toolkit") ||
//                        ti.getClassName().startsWith("javax.swing.plaf"))
                        return true;
                }
                    //getBundleImpl() loadBundle()
                    //javax.swing.plaf.basic.BasicLookAndFeelloadResourceBundle 
                    //java.awt.Toolkit$3
            }
            return false;
        }

        /*
         * Called to process deserialized xml
         */
        public void process() {
            this.exceptionLine = attributeVal("desc");            
            //now process children
            this.traces = this.children();  
            Enumeration e = traces.elements();
            e.nextElement();
            while (e.hasMoreElements()) {
                TraceItem ti = (TraceItem)e.nextElement();
                ti.process();
            }
        }
            
 }    
