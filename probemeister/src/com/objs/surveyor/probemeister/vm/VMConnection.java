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
 * @(#)VMConnection.java	1.36 01/04/26
 *
 * Copyright 1998-2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
/*
 * Copyright (c) 1997-2001 by Sun Microsystems, Inc. All Rights Reserved.
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

package com.objs.surveyor.probemeister;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.ThreadStartRequest;
import com.sun.jdi.request.ThreadDeathRequest;

import java.util.*;
import java.util.regex.*;
import java.io.*;

class VMConnection {

    private VirtualMachine vm;    
    private Process process = null;
    private int outputCompleteCount = 0;

    private final Connector connector;
    private final Map connectorArgs;
    private final int traceFlags;

    synchronized void notifyOutputComplete() {
        outputCompleteCount++;
        notifyAll();
    }

    synchronized void waitOutputComplete() {
        // Wait for stderr and stdout
        if (process != null) {
            while (outputCompleteCount < 2) {
                try {wait();} catch (InterruptedException e) {}
            }
        }
    }

    private Connector findConnector(String name) {
        List connectors = Bootstrap.virtualMachineManager().allConnectors();
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector connector = (Connector)iter.next();
            if (connector.name().equals(name)) {
                return connector;
            }
        }
        return null;
    }

    private Map parseConnectorArgs(Connector connector, String argString) {
        Map arguments = connector.defaultArguments();

        /*
         * We are parsing strings of the form:
         *    name1=value1,[name2=value2,...]
         * However, the value1...valuen substrings may contain
         * embedded comma(s), so make provision for quoting inside
         * the value substrings. (Bug ID 4285874)
         */
        String regexPattern =
            "(quote=[^,]+,)|" +           // special case for quote=.,
            "(\\w+=)" +                   // name=
            "(((\"[^\"]*\")|" +           //   ( "l , ue"
            "('[^']*')|" +                //     'l , ue'
            "([^,'\"]+))+,)";             //     v a l u e )+ ,
        Pattern p = Pattern.compile(regexPattern);
        Matcher m = p.matcher(argString);
        while (m.find()) {
            int startPosition = m.start();
            int endPosition = m.end();
            if (startPosition > 0) {
                /*
                 * It is an error if parsing skips over any part of argString.
                 */
                throw new IllegalArgumentException("Illegal connector argument: " + argString);
            }

            String token = argString.substring(startPosition, endPosition);
            int index = token.indexOf('=');
            String name = token.substring(0, index);
            String value = token.substring(index + 1,
                                           token.length() - 1); // Remove comma delimiter

            Connector.Argument argument = (Connector.Argument)arguments.get(name);
            if (argument == null) {
                throw new IllegalArgumentException("Argument " + name + 
                                               "is not defined for connector: " +
                                               connector.name());
            }
            argument.setValue(value);

            argString = argString.substring(endPosition); // Remove what was just parsed...
            m = p.matcher(argString);                     //    and parse again on what is left.
        }
        if (argString.length() > 0) {
            /*
             * It is an error if any part of argString is left over.
             */
            throw new IllegalArgumentException("Illegal connector argument: " + argString);
        }
        return arguments;
    }

    VMConnection(VirtualMachine _vm) {
        //Do we nned to supply values for runtime operation?
        connector = null;
        connectorArgs = null;
        traceFlags= 0;
        vm = _vm;
        
//do we need the connector args at this point?        
    }

    VMConnection(String connectSpec, int traceFlags) {
        String nameString;
        String argString;
        int index = connectSpec.indexOf(':');
        if (index == -1) {
            nameString = connectSpec;
            argString = "";
        } else {
            nameString = connectSpec.substring(0, index);
            argString = connectSpec.substring(index + 1);
        }

        connector = findConnector(nameString);
        if (connector == null) {
            throw new IllegalArgumentException("No connector named: " + 
                                               nameString);
        } 

        connectorArgs = parseConnectorArgs(connector, argString);
        this.traceFlags = traceFlags;
    }
        
    synchronized VirtualMachine open() {
        try {
            if (connector instanceof LaunchingConnector) {
                vm = launchTarget();
            } else if (connector instanceof AttachingConnector) {
                vm = attachTarget();
            } else if (connector instanceof ListeningConnector) {
                vm = listenTarget();
            } else {
                throw new InternalError("Invalid connect type");
            }
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Exception:: VM Connection cannot be made:" + e.getClass().getName());   
            return null;
        }
        if (vm ==null)
            return null;
            
        vm.setDebugTraceMode(traceFlags);
        //setEventRequests(vm);
        //resolveEventRequests();

        /*
         * Now that the vm connection is open, fetch the debugee
         * classpath and set up a default sourcepath.
         * (Unless user supplied a sourcepath on the command line)
         * (Bug ID 4186582)
         */
/*
        if (Env.getSourcePath().length() == 0) {
            if (vm instanceof PathSearchingVirtualMachine) {
                PathSearchingVirtualMachine psvm =
                    (PathSearchingVirtualMachine) vm;
                Env.setSourcePath(psvm.classPath());
            } else {
                Env.setSourcePath(".");
            }
        }
*/
//          Env.out.println("Connected to " + target);
        return vm;
    }

    boolean setConnectorArg(String name, String value) {
        /*
         * Too late if the connection already made
         */
        if (vm != null) {
            return false;
        }

        Connector.Argument argument = (Connector.Argument)connectorArgs.get(name);
        if (argument == null) {
            return false;
        }
        argument.setValue(value);
        return true;
    }

    String connectorArg(String name) {
        Connector.Argument argument = (Connector.Argument)connectorArgs.get(name);
        if (argument == null) {
            return "";
        }
        return argument.value();
    }

    public synchronized VirtualMachine vm() {
        if (vm == null) {
            throw new VMNotConnectedException();
        } else {
            return vm;
        }
    }         

    boolean isOpen() {
        return (vm != null);
    }

    boolean isLaunch() {
        return (connector instanceof LaunchingConnector);
    }

    public void disposeVM() {
        try {
            if (vm != null) {
                vm.dispose();
                vm = null;
            }
        } finally {
            if (process != null) {
                process.destroy();
                process = null;
            }
            waitOutputComplete();
        }
    }

    private void setEventRequests(VirtualMachine vm) {
        EventRequestManager erm = vm.eventRequestManager();
        // want all uncaught exceptions
        ExceptionRequest excReq = erm.createExceptionRequest(null, 
                                                             false, true); 
        excReq.enable();

        ThreadStartRequest tsr = erm.createThreadStartRequest();
        tsr.enable();
        ThreadDeathRequest tdr = erm.createThreadDeathRequest();
        tdr.enable();
    }

//    private void resolveEventRequests() {
//        Env.specList.resolveAll();
//    }

    private void dumpStream(InputStream stream) throws IOException {
        PrintStream outStream = System.out;
        BufferedReader in = 
            new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = in.readLine()) != null) {
            outStream.println(line);
        }
    }

    /**	
     *	Create a Thread that will retrieve and display any output.
     *	Needs to be high priority, else debugger may exit before
     *	it can be displayed.
     */
    private void displayRemoteOutput(final InputStream stream) {
	Thread thr = new Thread("output reader") { 
	    public void run() {
                try {
                    dumpStream(stream);
                } catch (IOException ex) {
                    com.objs.surveyor.probemeister.Log.out.warning("Failed reading output of child java interpreter.");
                } finally {
                    notifyOutputComplete();
                }
	    }
	};
	thr.setPriority(Thread.MAX_PRIORITY-1);
	thr.start();
    }

    private void dumpFailedLaunchInfo(Process process) {
        try {
            dumpStream(process.getErrorStream());
            dumpStream(process.getInputStream());
        } catch (IOException e) {
            com.objs.surveyor.probemeister.Log.out.warning("Unable to display process output: " +
                               e.getMessage());
        }
    }

    /* launch child target vm */
    private VirtualMachine launchTarget() {
        LaunchingConnector launcher = (LaunchingConnector)connector;
        try {
            VirtualMachine vm = launcher.launch(connectorArgs);
            process = vm.process();
            displayRemoteOutput(process.getErrorStream());
            displayRemoteOutput(process.getInputStream());
            return vm;
        } catch (IOException ioe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "Unable to launch target VM.", ioe);
        } catch (IllegalConnectorArgumentsException icae) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "Internal debugger error.", icae);
        } catch (VMStartException vmse) {
            com.objs.surveyor.probemeister.Log.out.warning(vmse.getMessage());
            dumpFailedLaunchInfo(vmse.process());
            com.objs.surveyor.probemeister.Log.out.warning("\n Target VM failed to initialize.");
        }
        return null; // Shuts up the compiler
    }

    /* attach to running target vm */
    private VirtualMachine attachTarget() {
        AttachingConnector attacher = (AttachingConnector)connector;
        try {
            return attacher.attach(connectorArgs);
        } catch (IOException ioe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE,"Unable to attach to target VM.", ioe);
        } catch (IllegalConnectorArgumentsException icae) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE,"Internal debugger error.", icae);
        }
        return null; // Shuts up the compiler
    }

    private VirtualMachine listenTarget() {
        ListeningConnector listener = (ListeningConnector)connector;
        try {
            String retAddress = listener.startListening(connectorArgs);
            com.objs.surveyor.probemeister.Log.out.info("Listening at address: " + retAddress);
            vm = listener.accept(connectorArgs);
            listener.stopListening(connectorArgs);
            return vm;
        } catch (IOException ioe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE,"Unable to attach to target VM.", ioe);
        } catch (IllegalConnectorArgumentsException icae) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE,"Internal debugger error.", icae);
        }
        return null; // Shuts up the compiler
    }

}
