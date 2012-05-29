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
 * Used by TargetVMDataPanel class tree list  
 */

package com.objs.surveyor.probemeister.gui;

import com.objs.surveyor.probemeister.PMClass;
import com.objs.surveyor.probemeister.PMMethod;
import com.objs.surveyor.probemeister.PMProbe;

import java.util.*;
import javax.swing.tree.*;

public class ClassTreeNode extends DefaultMutableTreeNode  {

  public final static int CLASS=0;
  public final static int METH=1;
  public final static int PROBE=2;
  public final static int ROOT=3;
  
  private boolean isNodeVisible = true; 
  private int nodeType;
  public int nodeType() {return nodeType; }
  /* Right now the filter state is assigned at the start, so runtime
   * modifications to the filter (if the class supported it) would not
   * effect filtering. This is more efficient than checking the filter
   * each time the node is rendered.
   */
  static ClassTreeNode createClassNode(PMClass _cls) {
    ClassTreeNode ct = new ClassTreeNode(_cls, true);
    ct.nodeType = ClassTreeNode.CLASS;
    return ct; 
  }    

  static ClassTreeNode createMethodNode(PMMethod _meth) {
    ClassTreeNode ct = new ClassTreeNode(_meth, true);
    ct.nodeType = ClassTreeNode.METH;
    return ct;
  }    

  static ClassTreeNode createProbeNode(PMProbe _probe) {
    ClassTreeNode ct = new ClassTreeNode(_probe, false);
    ct.nodeType = ClassTreeNode.PROBE;    
    return ct;
  }    

  static ClassTreeNode createRootNode(Object _o) {
    ClassTreeNode ct = new ClassTreeNode(_o, true);
    ct.nodeType = ClassTreeNode.ROOT;    
    return ct;
  }    


  public ClassTreeNode() {
    this(null);
  }

  public ClassTreeNode(Object userObject) {
    this(userObject, true);
  }

  public ClassTreeNode(Object userObject, boolean allowsChildren) {
    super(userObject, allowsChildren);
  }


  //Returns a node only if it is visible.
  public TreeNode getChildAt(int index,boolean filterIsActive, int _filterState) {
    if (! filterIsActive ) {
      return super.getChildAt(index);
    }
    if (children == null) {
      throw new ArrayIndexOutOfBoundsException("node has no children");
    }
      
    int realIndex    = -1;
    int visibleIndex = -1;
    Enumeration enm = children.elements();      
    while (enm.hasMoreElements()) {
        ClassTreeNode node = (ClassTreeNode)enm.nextElement();

        if (this.nodeType() == ClassTreeNode.ROOT || _filterState == ClassTreeModel.FILTER_PROBES ) {           
            if (!node.hideWhenFilterIsOn(_filterState)) 
                visibleIndex++;
        } else //filter names is on. THis request won't happen unless the parent is visible
               //so this node must be visible.
        if (_filterState == ClassTreeModel.FILTER_OUT_NAMES || 
            _filterState == ClassTreeModel.FILTER_IN_NAMES) 
            visibleIndex++;
      
        realIndex++;
        if (visibleIndex == index) {
	        return (TreeNode)children.elementAt(realIndex);
        }
    }        


/*
    Enumeration enm = children.elements();      
    while (enm.hasMoreElements()) {
      ClassTreeNode node = (ClassTreeNode)enm.nextElement();
      if (!node.hideWhenFilterIsOn(_filterState)) {
    	visibleIndex++;
      }
      realIndex++;
      if (visibleIndex == index) {
	      return (TreeNode)children.elementAt(realIndex);
      }
    }
*/
      
      
      
      
      
    throw new ArrayIndexOutOfBoundsException("index unmatched");
    //return (TreeNode)children.elementAt(index);
  }

  public int getChildCount(boolean filterIsActive, int _filterState) {
    if (! filterIsActive) {
      return super.getChildCount();
    }
    if (children == null) {
      return 0;
    }
      
    //If root then the children are classes, check each one, or
    //if filter probes, then independently check each node
    if (this.nodeType() == ClassTreeNode.ROOT || _filterState == ClassTreeModel.FILTER_PROBES ) {           
        int count = 0;
        Enumeration enm = children.elements();      
        while (enm.hasMoreElements()) {
            ClassTreeNode node = (ClassTreeNode)enm.nextElement();
            if (!node.hideWhenFilterIsOn(_filterState)) {
                node.setVisibility(true);
	            count++;
            } else
                node.setVisibility(false);
        }
        return count;
    }
    
    //Else method or probe nodes
    //if filter on class names (only), show methods & probes if parent is visible
    if (_filterState == ClassTreeModel.FILTER_OUT_NAMES ||
        _filterState == ClassTreeModel.FILTER_IN_NAMES) {
        //if class, just return child count, if class is visible
        if (this.nodeType() == ClassTreeNode.CLASS) {           
            return this.getChildCount();
        } else if (this.nodeType() == ClassTreeNode.METH) {
            return this.getChildCount();
        }
    } 
    
    return this.getChildCount();
  }

//  public void setVisible(boolean visible) {
//    this.isVisible = visible;
//  }
  
  public boolean hideWhenFilterIsOn(int _filterState) {
    
    if (_filterState == ClassTreeModel.FILTER_OUT_NAMES) {
        return ClassTreeNodeFilter.filterOutNames(this);
    } else
    if (_filterState == ClassTreeModel.FILTER_IN_NAMES) {
        return ClassTreeNodeFilter.filterInNames(this);
    } else
    if (_filterState == ClassTreeModel.FILTER_PROBES) {
        return ClassTreeNodeFilter.filterUsingList(this);
    }
    //else error state
    com.objs.surveyor.probemeister.Log.out.warning("ClassTreeNode.hideWhenFilterIsOn error state = "+_filterState);
    return false; //show all classes
  }
  
  void setVisibility(boolean _b) {
    isNodeVisible = _b;
  }
  boolean isVisible() {
    return isNodeVisible;
  }

}

