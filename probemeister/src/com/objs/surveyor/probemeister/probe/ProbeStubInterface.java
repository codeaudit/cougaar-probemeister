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


/*
 * This interface describes probes whose implementations do not
 * have instances per se. Instead, each probe has a 
 * surrogate instance (of probe stub) that is used to communicate with the
 * probe & get a description of it. 
 *
 * Simple probes also use surrogate instances because probes exist as
 * insrted code into the application & do not exist as independent instances.
 * @see ProbePlugInterface for probes that do have instances.
 */
public interface ProbeStubInterface extends ProbeInterface, CallableProbeInterface {
    
    public void attachProbe(ProbePlug _plug);
    public void detachProbe();

    public ProbePlug getProbe();
    
    /* Returns an array of Class objects that represent the formal 
     * parameter types, in declaration order, of the method represented 
     * by this ProbeStub. <b>This is used as the validation mechanism to
     * ensure that only ProbePlugs with the same signature can be attached
     * to a given stub.</b> If there are not parameters, the length of this
     * array will be 0.
     */
    public Class[] getParameterTypes();

    /* Returns a Class object that represents the formal return 
     * type of the method represented by this ProbeStub. 
     * <b>This is used as the validation mechanism to
     * ensure that only ProbePlugs with the same return value can 
     * be attached to a given stub.</b> In most cases, a plug will
     * NOT return a value, so this value will be an array of length 0.
     */
    public Class getReturnType();

}