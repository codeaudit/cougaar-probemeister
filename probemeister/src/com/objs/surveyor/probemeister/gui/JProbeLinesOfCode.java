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
 * JProbeLinesOfCode.java
 *
 * Created on October 28, 2002, 11:39 AM
 * JReversePro - Java Decompiler / Disassembler.
 * Copyright (C) 2002 pazandak@objs.com
 * EMail: pazandak@objs.com
 */

package com.objs.surveyor.probemeister.gui;
import jreversepro.reflect.method.*;

/**
 *
 * @author  Administrator
 */
public class JProbeLinesOfCode extends jreversepro.reflect.JLineOfCode {
    
    /** Creates a new instance of JProbeLinesOfCode 
     * @param sbo - start byte offset
     * @param ebo - end byte offset
     * @param fromI - from instruction # (index into instruction array)
     * @param toI - to instruction #
     * @param stmt - stringified line of code
     */
    public JProbeLinesOfCode(int sbo, int ebo, int fromI, int toI, String stmt) {
        super(sbo, ebo, fromI, toI, stmt);
     }        

    /** Creates a new instance of JProbeLinesOfCode
     *  This JLineOfCode is either block entry or block exit code
     *  since no offsets are provided.
     *  @param stmt Stringified line of code
     *  @param jbo  the JBlockObject this line is associated with
     *  @param state ENTRY | EXIT
     */
    public JProbeLinesOfCode(String stmt, JBlockObject jbo, int state) {
        super(stmt, jbo, state);
    }
    
}
