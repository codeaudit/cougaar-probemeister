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
 * PMData.java
 *
 * Created on July 22, 2002, 1:07 PM
 */

/**
 *
 * @author  Administrator
 */

package com.objs.probemeister;
public abstract class PMData {  

    /* Name to display */
    public static String getKeyName() { return "Event Name"; }
    
    /* Deserialize data stream into an instance of PMData */
    public static PMData deserializeMsg(Object _o) { return (PMData) null; }

    
    /* Returns the value under which this instance should be indexed in the web server gui 
     * E.g. For Cougaar, it should return the msg sender if it's a msg, otherwise return
     * the msg receiver if it's an ack -- this way the msg & ack get filed under the same
     * identifier.
     */
    public String getKey() {return null;}
    
    /* Output data as HTML */
    public String outputHTMLString() { return null; }

    /* Output data as XML */
    public String outputXMLString() { return null; }
}
