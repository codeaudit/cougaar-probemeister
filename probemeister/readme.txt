=========================================================================
ProbeMeister 1.0
=========================================================================

=========================================================================
Copyright and License
=========================================================================

Copyright 1999-2004 Object Services and Consulting, Inc. 
under sponsorship of the Defense Advanced Research Projects 
Agency (DARPA). 
  
You can redistribute this software and/or modify it under the 
terms of the Cougaar Open Source License as published on the 
Cougaar Open Source Website (www.cougaar.org). 
  
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 

=========================================================================
Acknowledgement
=========================================================================

ProbeMeister was developed primarily as part of research sponsored 
by the Defense Advanced Research Projects Agency and managed by the 
U.S. Air Force Research Laboratory under contract F30602-98-C-0159. 
The views and conclusions contained in this document are those of 
the authors and should not be interpreted as representing the official 
policies, either expressed or implied, of the Defense Advanced Research 
Projects Agency, U.S. Air Force Research Laboratory, or the United 
States Government.

=========================================================================
Developers
=========================================================================

Developed by:

    Paul Pazandak (pazandak@objs.com)

With Contributions by:

    David Wells (wells@objs.com)
    Steve Ford (ford@objs.com)

=========================================================================
Supported Environment
=========================================================================

ProbeMeister 1.0 has been tested with:

1) Java Version 1.4.2_03 
   http://java.sun.com/j2se/1.4.2/index.jsp
2) JReversePro 1.4.2a 
   http://prdownloads.sourceforge.net/jrevpro/jreversepro-1.4.2a-src.zip
3) Windows XP Pro SP1
4) Apache Ant version 1.6.2
   http://ant.apache.org

JReversePro 1.4.2a is required, but the others are just the versions 
we've tested with - others should work.

=========================================================================
Installation
=========================================================================

1) Confirm that the versions of Java and JReversePro above are installed.

2) Modify build.xml, line 3, to reference your JReversePro installation.

3) Build ProbeMeister (cd to the probemeister directory and run ant).

4) Modify bin/runPM.bat to reference your Java, JReversePro, and 
   ProbeMeister installations.

=========================================================================
Test your Installation
=========================================================================

1) Start up ProbeMeister.   cd to bin and execute runPM.bat.  
   Initialization is complete once the mostly blank ProbeMeister window 
   is displayed and "INFO: Listening at address: <host>:9876" is printed 
   to the console.

2) Now start the SimpleExample2 sample application (the application you
   will probe with ProbeMeister). cd to examples and execute runApp.bat.

3) A button labelled "Add Available VM" should immediately be highlighted
   at the bottom of the ProbeMeister window.  Press it.  The ProbeMeister 
   window should now display a partially populated tab for probing the 
   SimpleExample application, which is paused at a breakpoint waiting to
   enter its main method.

4) Press the "Resume" button to proceed past the breakpoint.  Class names
   should now be displayed in the "Loaded Classes" list and the 
   SimpleExample2 window should be displayed.

5) Select "Auto-Load?" to decompile the code on the fly.

6) Use the SimpleExample app for a bit to ensure that the app's classes
   have been loaded, then Select "Exclude JDK Packages" (on the right).
   Only the following classes should now be displayed:

   SimpleExample2
   SimpleExample2$RadioListener
   SimpleExample2$1

7) Display each classes methods by double-clicking the class name.  Do
   that to SimpleExample2.

8) Drag the "PrintString" Probe from "Available Probes" and drop it on 
   the "instrumentMe" method. A text box will pop up.  Enter anything in
   it and press "OK".
 
9) Now use the app again & you'll see that string emitted to the app's 
   console.
 
If this all worked, your installation is working and you can try probing
other applications.  Any Java application can be probed as long as it is 
started with all the options in examples\runApp.bat below:

    java -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=n,address=9876,suspend=y -DpmAPPNAME=SampleExample2 SimpleExample2

=========================================================================
Support
=========================================================================

Please submit bug reports to CougaarForge at 
http://cougaar.org/projects/probemeister/. We'll respond as quick as we
can.  

Additional support is available on a fee basis if required.  
Please contact pazandak@objs.com or ford@objs.com.
