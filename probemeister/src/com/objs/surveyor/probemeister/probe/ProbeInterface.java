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

import com.objs.surveyor.probemeister.bytecoder.StatementList;

/*
 * This is the core interface for probes and probe stubs
 */
public interface ProbeInterface {
    
    public static final String startMarker = "**OBJS-START**";   
    public static final String endMarker   = "**OBJS-END**";   
    public static final String probeIDMarker = "pmID=";   
    public static final String probeDescMarker = "pmDESC=";   
    public static final String probeTypeMarker = "pmTYPE=";   
                
                    
    public String getProbeID();
    public void   setProbeID(String _pid);

    public String getProbeDesc();
    public void   setProbeDesc(String _pd);
         
    public boolean isStub();

    public Location getProbeLocation();
    public void     setProbeLocation(Location _loc);

    public boolean isBytecodeProbe();
    void setBytecodeStmts(StatementList _sl);
    StatementList getBytecodeStmts();    

    public boolean isSourceProbe();
    void setSource(String[] _str);
    String[] getSource();    

    public ProbeType getProbeType();
    public void setProbeType(ProbeType _pt);

    public String getProbeTypeStr();
    public void setProbeTypeStr(String _ptStr);

}