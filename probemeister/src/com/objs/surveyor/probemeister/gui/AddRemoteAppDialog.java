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

package com.objs.surveyor.probemeister.gui;


import java.awt.*;
import javax.swing.*;

public class AddRemoteAppDialog extends javax.swing.JDialog
{
    private boolean pressedOK = false;
    boolean pressedOK() {return pressedOK;}
    
    String getAddress() {return tfAddress.getText();}
    String getPort() {return tfPort.getText();}
    String getAppName() {return tfName.getText();}
    
    HelpDialog help = null;
    
	private AddRemoteAppDialog(Frame parent)
	{
		super(parent);
		
		//help = HelpConnectingDialog.getDialog();
		tfAddress.setText("");
		tfPort.setText("");
		this.setModal(true);
		
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		getContentPane().setLayout(null);
		setSize(405,179);
		setVisible(false);
		lTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		lTitle.setText("Attach to Remote Application");
		getContentPane().add(lTitle);
		lTitle.setForeground(java.awt.Color.black);
		lTitle.setFont(new Font("Dialog", Font.BOLD, 18));
		lTitle.setBounds(54,13,297,25);
		lHost.setText("Host Address:");
		getContentPane().add(lHost);
		lHost.setBounds(19,54,90,21);
		lPort.setText("Port:");
		getContentPane().add(lPort);
		lPort.setBounds(298,54,33,21);
		getContentPane().add(tfAddress);
		tfAddress.setBounds(103,56,178,19);		
		getContentPane().add(tfPort);
		tfPort.setBounds(329,56,61,17);
		lName.setText("Application Name:");
		getContentPane().add(lName);
		lName.setBounds(19,94,110,21);
		getContentPane().add(tfName);
		tfName.setBounds(132,96,160,19);		
		bOK.setText("OK");
		getContentPane().add(bOK);
		bOK.setBounds(114,144,82,20);
		bCancel.setText("Cancel");
		getContentPane().add(bCancel);
		bCancel.setBounds(204,144,82,20);
		bHelp.setBounds(320,144,62,20);
		bHelp.setText("Help");
		getContentPane().add(bHelp);
		lLeaveBlank.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		lLeaveBlank.setText("(Leave blank if local)");
		getContentPane().add(lLeaveBlank);
		lLeaveBlank.setForeground(java.awt.Color.black);
		lLeaveBlank.setFont(new Font("Dialog", Font.PLAIN, 10));
		lLeaveBlank.setBounds(103,76,177,13);
		//}}
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		bOK.addActionListener(lSymAction);
		bCancel.addActionListener(lSymAction);
		bHelp.addActionListener(lSymAction);
		//}}
	}

	public AddRemoteAppDialog(JFrame gui, HelpDialog _help)
	{
		this(gui);
		help = _help;
	}

	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JLabel lTitle = new javax.swing.JLabel();
	javax.swing.JLabel lHost = new javax.swing.JLabel();
	javax.swing.JLabel lPort = new javax.swing.JLabel();
	javax.swing.JTextField tfAddress = new javax.swing.JTextField();
	javax.swing.JTextField tfPort = new javax.swing.JTextField();
	javax.swing.JLabel lName = new javax.swing.JLabel();
	javax.swing.JTextField tfName = new javax.swing.JTextField();
	javax.swing.JButton bOK = new javax.swing.JButton();
	javax.swing.JButton bCancel = new javax.swing.JButton();
	javax.swing.JButton bHelp = new javax.swing.JButton();
	javax.swing.JLabel lLeaveBlank = new javax.swing.JLabel();
	//}}


	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == bOK)
				bOK_actionPerformed(event);
			else if (object == bCancel)
				bCancel_actionPerformed(event);
			else if (object == bHelp)
				help.setVisible(true, "Connecting");
		}
	}

	void bOK_actionPerformed(java.awt.event.ActionEvent event)
	{
		pressedOK=true;
		this.setVisible(false);
			 
	}

	void bCancel_actionPerformed(java.awt.event.ActionEvent event)
	{
		pressedOK=false;
		this.setVisible(false);
			 
	}
}