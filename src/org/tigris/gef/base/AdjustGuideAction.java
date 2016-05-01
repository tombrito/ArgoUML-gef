// Copyright (c) 1996-06 The Regents of the University of California. All
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

// File: AdjustGuideAction.java
// Classes: AdjustGuideAction
// Original Author: andrea.nironi@gmail.com

package org.tigris.gef.base;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.tigris.gef.util.Localizer;

/**
 * An Action to modify the way that the Guides constrain the mouse points
 * entered by the user. This does not change the apperance of the LayerGrid.
 * Needs-More-Work: Should put up a grid preference dialog box or use the
 * property sheet.
 */
public class AdjustGuideAction extends AbstractAction {

    private static final long serialVersionUID = -7779055134388973203L;

    public AdjustGuideAction() {
        super();
    }

    /**
     * Creates a new AdjustGuideAction
     * 
     * @param name The name of the action
     */
    public AdjustGuideAction(String name) {
        this(name, false);
    }

    /**
     * Creates a new AdjustGuideAction
     * 
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public AdjustGuideAction(String name, Icon icon) {
        this(name, icon, false);
    }

    /**
     * Creates a new AdjustGuideAction
     * 
     * @param name The name of the action
     * @param localize Whether to localize the name or not
     */
    public AdjustGuideAction(String name, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
    }

    /**
     * Creates a new AdjustGuideAction
     * 
     * @param name The name of the action
     * @param icon The icon of the action
     * @param localize Whether to localize the name or not
     */
    public AdjustGuideAction(String name, Icon icon, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
    }

    public void actionPerformed(ActionEvent event) {
        Editor ce = Globals.curEditor();
        Guide guide = ce.getGuide();
        if (guide != null) {
            guide.adjust();
        }
    }
}
