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


package com.objs.probemeister;

import com.objs.surveyor.probemeister.gui.MultiJVM;
import java.awt.Color;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;

public class LoggerManager {
    
    MultiJVM gui = null;
    static LoggerManager logMgr = null;

    public MultiJVM gui() { return gui; }
    static public LoggerManager loggerMgr() { return logMgr; }
    
    Hashtable loggers = null;
    HashSet loggerNames = null;
    
    
    //Instantiated by MultiJVM if global logging var is set to true
    public LoggerManager(MultiJVM _gui) {
        
        gui = _gui;
        logMgr = this;
        loggers = new Hashtable(5);
        loggerNames = new HashSet(5);
    }
    
    public LoggerWindow addLogger(String _port, String _name, String _logName) {
     
        //See if we already have this port registered.
        LoggerWindow win = (LoggerWindow)loggers.get(new Integer(_port));

        if (win == null) { //create a new logger window
            win = new LoggerWindow(_port, _name, gui);
            gui.addLoggerMenuItem(_port, _name, win);
            loggers.put(new Integer(_port), win); //store
            loggerNames.add(_logName);
        }
        
        return win;
    }
    
    public void addLoggerName(String _n) {        
        loggerNames.add(_n);        
    }
    
    public Iterator getLoggerNames() {
        return loggerNames.iterator();
    }
    
}
