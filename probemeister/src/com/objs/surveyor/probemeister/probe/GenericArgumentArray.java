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
package com.objs.surveyor.probemeister.probe;


  public class GenericArgumentArray {

    Argument[] ar;
    int i;
    int size;

    public GenericArgumentArray(int _size) {        
        ar = new Argument[_size];
        i=0;
        size = _size;
        for (int j=0; j<size; j++) ar[j]=new Argument();
    }
    
    public int length() {return size;}
    
    public Object getValue(int index) { 
        if (index > -1 && index < size) 
            return ar[index].value;
        else return null;
    }

    public String getName(int index) { 
        if (index > -1 && index < size) 
            return ar[index].name;
        else return null;
    }

    public String getType(int index) { 
        if (index > -1 && index < size) 
            return ar[index].type;
        else return null;
    }
    
    /* auto increment index ptr */
    void incIndex() {if (i< (size-1)) i++; else i = 0;}

    public void set(Object value, String name) { ar[i].value=value; ar[i].name=name; incIndex();}

    public void set(Object value, String type, String name) { ar[i].value=value; ar[i].type=type; ar[i].name=name; incIndex();}

    public void set(boolean z, String name) { ar[i].value=new Boolean(z); ar[i].name=name; incIndex();}

    public void set(byte b, String name) { ar[i].value=new Byte(b); ar[i].name=name; incIndex();}

    public void set(char c, String name) { ar[i].value=new Character(c); ar[i].name=name; incIndex();}

    public void set(double d, String name) { ar[i].value=new Double(d); ar[i].name=name; incIndex();}

    public void set(float f, String name) { ar[i].value=new Float(f); ar[i].name=name; incIndex();}

    public void set(int x, String name) { ar[i].value=new Integer(x); ar[i].name=name; incIndex();}

    public void set(long l, String name) { ar[i].value=new Long(l); ar[i].name=name; incIndex();}

    public void set(short s, String name) { ar[i].value=new Short(s); ar[i].name=name; incIndex();}


  
     class Argument {        
           String name=null;
           String type=null;
           Object value=null;
    }
          
  }