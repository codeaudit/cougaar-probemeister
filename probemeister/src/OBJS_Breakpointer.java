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
import java.util.Hashtable;
//package com.objs.surveyor.probemeister.bytecoder.util;
//Moved into top level package to ease obfuscation

/* This class is intended to be migrated to a target VM to
 * enable ProbeMeister to set breakpoints at will.
 */
public class OBJS_Breakpointer extends Thread {
    
    static final int sleepTime = 10000;
    //Define struct to hold on/off states of probes.
    static final java.util.Hashtable PROBE_STATES = new Hashtable(20); 
    static OBJS_Breakpointer obj;
    public static void init() { 
        obj = new OBJS_Breakpointer();
        System.out.println("Breakpointer started...");
        obj.start();    
    }
    
    private OBJS_Breakpointer() { super("Breakpointer Thread"); }  
    
    public void run() {        
        while (true) {
            try {
                breakpoint();
            } catch(InterruptedException ie) {}
        }
    }
    
    public void breakpoint() throws InterruptedException {
//        System.out.println("Breakpointer running...");
        sleep(sleepTime);
    }
}