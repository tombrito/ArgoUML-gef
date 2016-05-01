// Copyright (c) 1996-2009 The Regents of the University of California. All
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

// File: ModeCreateFigCircle.java
// Classes: ModeCreateFigCircle
// Original Author: ics125 spring 1996
// $Id: ModeCreateFigCircle.java 1259 2009-08-18 06:53:37Z mvw $

package org.tigris.gef.base;

import java.awt.event.MouseEvent;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigCircle;
import org.tigris.gef.util.Localizer;

/**
 * A Mode to interpret user input while creating a FigCircle. All of the actual
 * event handling is inherited from ModeCreate. This class just implements the
 * differences needed to make it specific to circles.
 */

public class ModeCreateFigCircle extends ModeCreate {

    private static final long serialVersionUID = 2100068733524473429L;

    public String instructions() {
        return Localizer.localize("GefBase", "ModeCreateFigCircleInstructions");
    }

    // //////////////////////////////////////////////////////////////
    // ModeCreate API

    /**
     * Create a new FigCircle instance based on the given mouse down event and
     * the state of the parent Editor.
     */
    public Fig createNewItem(MouseEvent me, int snapX, int snapY) {
        return new FigCircle(snapX, snapY, 0, 0);
    }
} /* end class ModeCreateFigCircle */
