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

// File: CmdSavePS.java
// Classes: CmdSavePS
// Original Author: wienberg@informatik.uni-hamburg.de

package org.tigris.gef.base;

import java.io.*;
import java.awt.Rectangle;

import org.tigris.gef.persistence.*;

/**
 * Cmd to save a diagram as PostScript in a supplied OutputStream. Requires the
 * CH.ifa.draw.util.PostscriptWriter class. Operates on the diagram in the
 * current editor.
 * 
 * Code loosely adapted from CmdSaveGIF.
 * 
 * @deprecated in 0.12.3 use SavePSAction
 * @author Frank Wienberg, wienberg@informatik.uni-hamburg.de
 */

public class CmdSavePS extends CmdSaveGraphics {

    private static final long serialVersionUID = 2283867876416499471L;

    public CmdSavePS() {
        super("SavePostScript");
    }

    protected void saveGraphics(OutputStream s, Editor ce, Rectangle drawingArea)
            throws IOException {
        PostscriptWriter ps = new PostscriptWriter(s);
        ps.translate(32, 32 + 778);
        double scale = Math.min(535.0 / drawingArea.width,
                778.0 / drawingArea.height);
        if (scale < 1.0) {
            ps.scale(scale, scale);
        }
        ps.translate(-drawingArea.x, -drawingArea.y);
        ps.setClip(drawingArea.x, drawingArea.y, drawingArea.width,
                drawingArea.height);
        // java bug if using Rectangle.shape() ???
        ce.print(ps);
        ps.dispose();
    }

} /* end class CmdSavePS */
