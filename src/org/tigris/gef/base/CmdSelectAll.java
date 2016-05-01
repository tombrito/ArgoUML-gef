// Copyright (c) 1996-99 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

// File: CmdSelectAll.java
// Classes: CmdSelectAll
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdSelectAll.java 1153 2008-11-30 16:14:45Z bobtarling $

package org.tigris.gef.base;

import java.util.Collection;

/**
 * Cmd to select all the Figs in the editor's current view.
 * 
 * @deprecated in 0.12.3 use SelectAllAction
 */

public class CmdSelectAll extends Cmd {

    private static final long serialVersionUID = 4897805406236093681L;

    public CmdSelectAll() {
        super("SelectAll");
    }

    public void doIt() {
        Editor ce = Globals.curEditor();
        Collection diagramContents = ce.getLayerManager().getContents();
        ce.getSelectionManager().select(diagramContents);
    }

    public void undoIt() {
        System.out.println("Undo does not make sense for CmdSelectAll");
    }
} /* end class CmdSelectAll */
