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
 * @(#)ThreadInfo.java	1.19 01/05/02
 *
 * Copyright 1998-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
/*
 * Copyright (c) 1997-1999 by Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package com.objs.surveyor.probemeister.event;
import com.objs.surveyor.probemeister.TargetVMConnector;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.io.*;

class ThreadInfoMgr implements ThreadEventNotifier {
    // This is a list of all known ThreadInfo objects. It survives 
    // ThreadInfo.invalidateAll, unlike the other static fields below. 
    private List threads = Collections.synchronizedList(new ArrayList());
    private boolean gotInitialThreads = false;

    private ThreadInfo current = null;
    private ThreadGroupReference group = null;

//    private final ThreadReference thread;
//    private int currentFrameIndex = 0;
    
    private TargetVMConnector myTargetVM;

    public ThreadInfoMgr(TargetVMConnector _vmc, EventHandler _eh) {
        myTargetVM = _vmc;
        _eh.setThreadEventNotifier(this);
    }

    private void initThreads() {
        if (!gotInitialThreads) {
            Iterator iter = myTargetVM.vm().allThreads().iterator();
            while (iter.hasNext()) {
                ThreadReference thread = (ThreadReference)iter.next();
                threads.add(new ThreadInfo(this,thread));
//System.out.println("ThreadInfoMgr - adding thread: "+thread.name());                
/*
System.out.println("ThreadInfoMgr - adding thread: "+thread.name());                
if (thread.name().equals("main")) {//"Reference Handler"
    List classes = myTargetVM.vm().allClasses();
    Iterator iter2 = classes.iterator();
    while (iter2.hasNext()){
        com.sun.jdi.ReferenceType rt = (com.sun.jdi.ReferenceType)iter2.next();
        if (rt.name().equals("java.lang.System")) {
            com.sun.jdi.ClassType ct = (com.sun.jdi.ClassType)rt;
            com.sun.jdi.Method meth = ct.concreteMethodByName("getProperty","(Ljava/lang/String;)Ljava/lang/String;");
            if (meth != null) {
                //thread.interrupt();
                myTargetVM.vm().resume();
                java.util.Vector v = new java.util.Vector();
                v.add(myTargetVM.vm().mirrorOf("vm_id"));
                
                com.sun.jdi.Value result = null;
                try {
                    ct.invokeMethod(thread,meth,v,0);
                    //System.out.println("   ----> result type [o] is "+o.getClass().getName());
                } catch(Exception e) {System.out.println("ThreadInfoMgr -- exception:\n"+e);
                        e.printStackTrace();}
                //thread.resume();
                System.out.println("ThreadInfoMgr - result = "+result);
            }else System.out.println("ThreadInfoMgr - Could not find getProperty method...");
        }
        
    }
}
*/
            }
            gotInitialThreads = true;
        }
    }

    void addThread(ThreadReference thread) {
        synchronized (threads) {
            initThreads();
            ThreadInfo ti = new ThreadInfo(this,thread);
            // Guard against duplicates. Duplicates can happen during 
            // initialization when a particular thread might be added both
            // by a thread start event and by the initial call to threads()
            if (getThreadInfo(thread) == null) {
                threads.add(ti);
            }
        }
    }

    void removeThread(ThreadReference thread) {
        if (thread.equals(this.current)) {
            // Current thread has died.

            // Be careful getting the thread name. If its death happens
            // as part of VM termination, it may be too late to get the 
            // information, and an exception will be thrown.
            String currentThreadName;
            try {
               currentThreadName = " \"" + thread.name() + "\"";
            } catch (Exception e) {
               currentThreadName = "";
            }
                 
            setCurrentThread(null);

            
            com.objs.surveyor.probemeister.Log.out.fine("Current thread" + currentThreadName + 
                        " died. Execution continuing...");
        }
        threads.remove(getThreadInfo(thread));
    }

    List threads() {
        synchronized(threads) {
            initThreads();
            // Make a copy to allow iteration without synchronization
            return new ArrayList(threads);
        }
    }

    void invalidateAll() {
        current = null;
        group = null;
        synchronized (threads) {
            Iterator iter = threads().iterator();
            while (iter.hasNext()) {
                ThreadInfo ti = (ThreadInfo)iter.next();
                ti.invalidate();
            }
        }
    }

    void setThreadGroup(ThreadGroupReference tg) {	
        group = tg;
    }
    
    void setCurrentThread(ThreadReference tr) {
        if (tr == null) {
            setCurrentThreadInfo(null);
        } else {
            ThreadInfo tinfo = getThreadInfo(tr);
            setCurrentThreadInfo(tinfo);
        }
    }

    void setCurrentThreadInfo(ThreadInfo tinfo) { 
        current = tinfo;
        if (current != null) {
            current.invalidate();
        }
    }

    /**
     * Get the current ThreadInfo object.
     *
     * @return the ThreadInfo for the current thread.
     */
    ThreadInfo getCurrentThreadInfo() {
        return current;
    }
    

    ThreadGroupReference group() {
        if (group == null) {
                // Current thread group defaults to the first top level
                // thread group.
	        setThreadGroup((ThreadGroupReference)
                                myTargetVM.vm().topLevelThreadGroups().get(0));
        }
        return group;
    }
    
    ThreadInfo getThreadInfo(long id) {
        ThreadInfo retInfo = null;

        synchronized (threads) {
            Iterator iter = threads().iterator();
            while (iter.hasNext()) {
                ThreadInfo ti  = (ThreadInfo)iter.next();
                if (ti.getThread().uniqueID() == id) {
                   retInfo = ti;
                   break;
                }
            }
        }
        return retInfo;
    }

    ThreadInfo getThreadInfo(ThreadReference tr) {
        return getThreadInfo(tr.uniqueID());
    }

    ThreadInfo getThreadInfo(String idToken) {
        ThreadInfo tinfo = null;
        if (idToken.startsWith("t@")) {
            idToken = idToken.substring(2);
        }
        try {
            long threadId = Long.decode(idToken).longValue();
            tinfo = getThreadInfo(threadId);
        } catch (NumberFormatException e) {
            tinfo = null;
        }
        return tinfo;
    }

    // *************
    // THREAD EVENTS
    // *************

    public void threadDeathEvent(ThreadDeathEvent _evt, boolean suspended, boolean lastEvent) {
        removeThread(_evt.thread());
    }

    public void threadStartEvent(ThreadStartEvent _evt, boolean suspended, boolean lastEvent) {
        addThread(_evt.thread());
    }

    public void initNotifier() {}


}
                            
