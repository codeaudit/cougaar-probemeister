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

import com.objs.surveyor.probemeister.instrumentation.InstrumentationRecordSet;
import javax.swing.tree.*;


public class ClassTreeModel extends DefaultTreeModel {

  protected boolean filterIsActive;
  protected String[] userFilters = null;
  protected boolean showOnlyProbedMethods = false;
  protected int filterState = 0;

  protected static final int FILTER_OUT_NAMES = 0;
  protected static final int FILTER_PROBES = 1;
  protected static final int FILTER_IN_NAMES = 2;
    

  public ClassTreeModel(TreeNode root) {
    this(root, false);
  }

  public ClassTreeModel(TreeNode root, boolean asksAllowsChildren) {
    this(root, false, false);
  }

  public ClassTreeModel(TreeNode root, boolean asksAllowsChildren
			    , boolean filterIsActive) {
    super(root, asksAllowsChildren);
    this.filterIsActive = filterIsActive;
  }

  public void deactivateFilter() {
    filterIsActive = false;
  }
  public void activateFilter() {
    filterIsActive = true;
  }
  public boolean isFilterOn() {
    return filterIsActive;
  }


  public void activateJDKFilter(String[] _otherFilters) {
    filterIsActive = true;
    filterState = ClassTreeModel.FILTER_OUT_NAMES;
    userFilters = _otherFilters;
    ClassTreeNodeFilter.setUserFilters(_otherFilters);
  }

  public void activateFilterInFilter(String[] _otherFilters) {
    filterIsActive = true;
    filterState = ClassTreeModel.FILTER_IN_NAMES;
    userFilters = _otherFilters;
    ClassTreeNodeFilter.setUserFilters(_otherFilters);
  }

  //activates probe filter, filtering on method name & signature
  public void activateProbeFilter(MethodFilterList _list, boolean _probedMethsOnly) {
      activateProbeFilter(_list, _probedMethsOnly, true);
  }

  //activates probe filter
  //if _filterOnMethodNameNSig is FALSE, filtering is on name only
  public void activateProbeFilter(MethodFilterList _list, boolean _probedMethsOnly,
                                  boolean _filterOnMethodNameNSig) {
    filterIsActive = true;
    filterState = ClassTreeModel.FILTER_PROBES;
    showOnlyProbedMethods = _probedMethsOnly;
    ClassTreeNodeFilter.filterOnMethodNameNSig = _filterOnMethodNameNSig;
    ClassTreeNodeFilter.setProbeFilter(_list, showOnlyProbedMethods);
  }




  public Object getChild(Object parent, int index) {
    ClassTreeNode ctn = (ClassTreeNode)parent;
    if (filterIsActive) {
        try {
            synchronized(ctn.getRoot()) {
        	    return ((ClassTreeNode)parent).getChildAt(index,filterIsActive, filterState);
        	}
    	} catch (ArrayIndexOutOfBoundsException aiob) {
    	    com.objs.surveyor.probemeister.Log.out.warning("ArrayIndexOutOfBoundsException occurred at index = "+index);
    	    return null;
    	}
    }
    synchronized(ctn.getRoot()) {
        return ((TreeNode)parent).getChildAt(index);
    }
  }

  public int getChildCount(Object parent) {
    ClassTreeNode ctn = (ClassTreeNode)parent;
    if (filterIsActive) {
        synchronized(ctn.getRoot()) {
    	    return ((ClassTreeNode)parent).getChildCount(filterIsActive, filterState);
    	}
    }
    synchronized(ctn.getRoot()) {
        return ((TreeNode)parent).getChildCount();
    }
  }

}



