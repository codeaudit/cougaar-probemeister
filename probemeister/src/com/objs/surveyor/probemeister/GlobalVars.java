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

package com.objs.surveyor.probemeister;

public final class GlobalVars {
    String s;
    private GlobalVars(String _s) { s = _s; }
    public String toString() {return s;}        

    //GLOBAL VARS
    public static final GlobalVars INSTALL_BREAKPOINT_BOOLEAN = new GlobalVars("INSTALL_BREAKPOINT_BOOLEAN");
    public static final GlobalVars LISTEN_PORT_STR = new GlobalVars("LISTEN_PORT_STR");
    public static final GlobalVars SEND_BYTECODE_TO_CONSOLE_BOOLEAN = new GlobalVars("SEND_BYTECODE_TO_CONSOLE_BOOLEAN");
    public static final GlobalVars SAVE_BYTECODE_TO_FILE_BOOLEAN = new GlobalVars("SAVE_BYTECODE_TO_FILE_BOOLEAN");
    public static final GlobalVars BREAKPOINT_CLASS_NAME = new GlobalVars("BREAKPOINT_CLASS_NAME");
    public static final GlobalVars RMI_ACTIVE = new GlobalVars("RMI_ACTIVE");
    public static final GlobalVars RMI_PORT = new GlobalVars("RMI_PORT");
    public static final GlobalVars RMI_FILESERVER_DIR = new GlobalVars("RMI_FILESERVER_DIR");
    public static final GlobalVars RMI_POLICY_FILE = new GlobalVars("RMI_POLICY_FILE");
    public static final GlobalVars RMI_FILESERVER = new GlobalVars("RMI_FILESERVER");
    public static final GlobalVars RMI_GAUGEHISTORY = new GlobalVars("RMI_GAUGEHISTORY");
    public static final GlobalVars RMIREGISTRY = new GlobalVars("RMIREGISTRY");
    public static final GlobalVars ANNOUNCE_PROBES = new GlobalVars("ANNOUNCE_PROBES");
    //To deploy a config file into main() class for probes that should be run once...
    //PM will automatically identify the main class. The config file MUST specify the methods.
    public static final GlobalVars DEPLOY_CONFIG_AT_STARTUP = new GlobalVars("DEPLOY_CONFIG_AT_STARTUP");
    //public static final GlobalVars USE_PM_AS_LOGGER_MONITOR = new GlobalVars("USE_PM_AS_LOGGER_MONITOR");
}
