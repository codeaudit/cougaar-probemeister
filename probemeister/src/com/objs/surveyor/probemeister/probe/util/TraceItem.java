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
    
import com.textuality.lark.Attribute;

    public class TraceItem extends com.textuality.lark.Element {
        String clsname = null;
        String methname= null;
        
        public TraceItem(){}
        public TraceItem(String c, String m) {
            clsname=c; 
            methname=m;        
            if (m.charAt(0)=='<') { //strip off < and > from method name
                methname=m.substring(1, m.length()-1);
            }
        }
        public String getClassName() {return clsname;}
        public String getMethodName() {return methname;}

        /*
         * Called to process deserialized xml
         */
        public void process() {
            clsname = attributeVal("class");            
            methname = attributeVal("method");
        }
        
    }

