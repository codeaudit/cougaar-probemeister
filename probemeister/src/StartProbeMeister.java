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

public class StartProbeMeister {
    
    static public void main(String[] args) {
        
        if (args.length > 1) {
            for (int i=0; i<args.length; i++) {
                if (args[i].equals("-listen")) {                
                    if (++i < args.length) 
//                        com.objs.surveyor.probemeister.Globals.globals().put(com.objs.surveyor.probemeister.GlobalVars._fldcase, args[i]);
//                        com.objs.surveyor.probemeister.Globals.globals().put(com.objs.surveyor.probemeister.GlobalVars.LISTEN_PORT_STR, args[i]);
;
                    else 
                        usage();
                } else
                if (args[i].equals("-gFile")) {                
                    if (++i < args.length) 
//                        com.objs.surveyor.probemeister.Globals.globals().importGlobalSettings(args[i]);
;
                    else 
                        usage();
                }
                else 
                if (args[i].equals("-rmi")) {                
                    if (++i < args.length) {
                        int port = Integer.parseInt(args[i]);
//                        com.objs.surveyor.probemeister.Globals.globals().put(com.objs.surveyor.probemeister.GlobalVars.RMI_ACTIVE, Boolean.TRUE);
//                        com.objs.surveyor.probemeister.Globals.globals().put(com.objs.surveyor.probemeister.GlobalVars.RMI_PORT, new Integer(port));
//                        com.objs.surveyor.probemeister.Globals.globals().put(com.objs.surveyor.probemeister.GlobalVars._fldtry, Boolean.TRUE);
//                        com.objs.surveyor.probemeister.Globals.globals().put(com.objs.surveyor.probemeister.GlobalVars._flddo, new Integer(port));
                    }
                    else 
                        usage();
                }
                else 
                    usage();
            }
        }


        Object[] options = { "I Accept", "Quit"};
        int retVal = javax.swing.JOptionPane.showOptionDialog(null, "Blah Blah?", "Blah",
            javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.INFORMATION_MESSAGE,
            null, options, options[1]);				

        if (retVal==1) System.exit(0); //user pressed cancel
		if (retVal==0) {//Start up ProbeMeister
//            com.objs.surveyor.probemeister.gui.ProbeMeisterGUI pm = 
//                new com.objs.surveyor.probemeister.gui.ProbeMeisterGUI();        
            com.objs.surveyor.probemeister.gui.ProbeMeisterGUI.main(args); 
//                new com.objs.surveyor.probemeister.gui.ProbeMeisterGUI();        
        }
    }

    public static void usage() {
        
        System.out.println("Usage: Optional parameters");
        System.out.println("^t-listen <port number>  = to allow JVMs to connect to ProbeMeister");   
        System.out.println("^t-rmi    <port number>  = to allow rmi connections over the specified port");   
        System.out.println("^t-gFile  <filename>     = specify a global settings file to inport");   
    }


}    
    
    
    
