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

import com.sun.jdi.ThreadReference;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import java.util.List;

class ThreadInfo {
    // This is a list of all known ThreadInfo objects. It survives 
    // ThreadInfo.invalidateAll, unlike the other static fields below. 

    private final ThreadReference thread;
    private int currentFrameIndex = 0;
    ThreadInfoMgr myMgr;

    ThreadInfo(ThreadInfoMgr _mgr, ThreadReference thread) {
        this.thread = thread;
        this.myMgr = _mgr;
        if (thread == null) {
            com.objs.surveyor.probemeister.Log.out.warning("Internal error: null ThreadInfo created");
        }
    }
    
    /**
     * Get the thread from this ThreadInfo object.
     *
     * @return the Thread wrapped by this ThreadInfo.
     */
    ThreadReference getThread() {
        return thread;
    }

    /**
     * Get the thread stack frames.
     *
     * @return a <code>List</code> of the stack frames.
     */
    List getStack() throws IncompatibleThreadStateException {
        return thread.frames();
    }

    /**
     * Get the current stackframe.
     *
     * @return the current stackframe.
     */
    StackFrame getCurrentFrame() throws IncompatibleThreadStateException {
        if (thread.frameCount() == 0) {
            //Env.out.println("No stack");
            return null;
        }
        return thread.frame(currentFrameIndex);
    }

    /**
     * Invalidate the current stackframe index.
     */
    void invalidate() {
        currentFrameIndex = 0;
    }

    /* Throw IncompatibleThreadStateException if not suspended */
    private void assureSuspended() throws IncompatibleThreadStateException {
        if (!thread.isSuspended()) {
            throw new IncompatibleThreadStateException();
        }
    }

    /**
     * Get the current stackframe index.
     *
     * @return the number of the current stackframe.  Frame zero is the
     * closest to the current program counter
     */
    int getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    /**
     * Set the current stackframe to a specific frame.
     *
     * @param nFrame	the number of the desired stackframe.  Frame zero is the
     * closest to the current program counter
     * @exception IllegalAccessError when the thread isn't 
     * suspended or waiting at a breakpoint
     * @exception ArrayIndexOutOfBoundsException when the 
     * requested frame is beyond the stack boundary
     */
    void setCurrentFrameIndex(int nFrame) throws IncompatibleThreadStateException {
        assureSuspended();
        if ((nFrame < 0) || (nFrame >= thread.frameCount())) {
            throw new ArrayIndexOutOfBoundsException();
        }
        currentFrameIndex = nFrame;
    }

    /**
     * Change the current stackframe to be one or more frames higher
     * (as in, away from the current program counter).
     *
     * @param nFrames	the number of stackframes
     * @exception IllegalAccessError when the thread isn't 
     * suspended or waiting at a breakpoint
     * @exception ArrayIndexOutOfBoundsException when the 
     * requested frame is beyond the stack boundary
     */
    void up(int nFrames) throws IncompatibleThreadStateException {
        setCurrentFrameIndex(currentFrameIndex + nFrames);
    }

    /**
     * Change the current stackframe to be one or more frames lower
     * (as in, toward the current program counter).     *
     * @param nFrames	the number of stackframes
     * @exception IllegalAccessError when the thread isn't 
     * suspended or waiting at a breakpoint
     * @exception ArrayIndexOutOfBoundsException when the 
     * requested frame is beyond the stack boundary
     */
    void down(int nFrames) throws IncompatibleThreadStateException {
        setCurrentFrameIndex(currentFrameIndex - nFrames);
    }

}
                            
