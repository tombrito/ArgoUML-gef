// $Id: FigTextEditor.java 1153 2008-11-30 16:14:45Z bobtarling $
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

package org.tigris.gef.presentation;

import java.awt.Container;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.tigris.gef.JavaFXTest;
import org.tigris.gef.base.Editor;
import org.tigris.gef.base.Globals;
import org.tigris.gef.graph.presentation.JGraphFXInternalPane;
import org.tigris.gef.undo.UndoManager;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * A text pane for on screen editing of a FigText.
 * 
 * @author jrobbins
 */
public class FigTextEditor extends JTextPane
        implements PropertyChangeListener, DocumentListener, KeyListener,
        FocusListener, TextEditor {

    private TextField new_this;

    /*************************************************************************/
    
    private static final long serialVersionUID = 7350660058167121420L;

    private static TextEditor _activeTextEditor = null;

    public static void setActiveTextEditor(TextEditor fte) {
        if (_activeTextEditor != null) {
            _activeTextEditor.endEditing();
        }
        _activeTextEditor = fte;
    }
    
    /*************************************************************************/
    
    private static Log LOG = LogFactory.getLog(FigTextEditor.class);

    private static int _extraSpace = 2;

    private static Border _border = BorderFactory.createLineBorder(java.awt.Color.gray);

    private static boolean _makeBrighter = false;

    private static java.awt.Color _backgroundColor = null;

    /*************************************************************************/

    private FigText figText;

    private JComponent drawingPanel;

    private Container diagramPane;

    /*************************************************************************/

    public FigTextEditor(FigText ft, InputEvent ie) {

        if (JavaFXTest.ON) {
            new_this  =  new TextField();
        }
        
        setVisible(true);
        figText = ft;
        final Editor currEditor = Globals.curEditor();

        // o mesmo JGraphFXInternalPane BLZ !!!
        drawingPanel = (JComponent) currEditor.getJComponent();

        UndoManager.getInstance().startChain();
        figText.firePropChange("editing", false, true);
        figText.addPropertyChangeListener(this);

        // XXX Why not using the drawingPanel?
        // walk up and add to glass pane
//        Component awtComp = drawingPanel;
//        while (!(awtComp instanceof RootPaneContainer) && awtComp != null) {
//            awtComp = awtComp.getParent();
//        }
//        if (!(awtComp instanceof RootPaneContainer)) {
//            LOG.warn("no RootPaneContainer");
//            return;
//        }
//        diagramPane = ((RootPaneContainer) awtComp).getLayeredPane();
        diagramPane = drawingPanel;

        ft.calcBounds();

        final java.awt.Color figTextBackgroundColor = ft.getFillColor();
        final java.awt.Color myBackground;
        if (_makeBrighter && !figTextBackgroundColor.equals(java.awt.Color.white)) {
            myBackground = figTextBackgroundColor.brighter();
        } else if (_backgroundColor != null) {
            myBackground = _backgroundColor;
        } else {
            myBackground = figTextBackgroundColor;
        }

        setBackground(myBackground);

        setBorder(_border);

        Rectangle bbox = ft.getBounds();
        final double scale = currEditor.getScale();
        bbox.x = (int) Math.round(bbox.x * scale);
        bbox.y = (int) Math.round(bbox.y * scale);

        if (scale > 1) {
            bbox.width = (int) Math.round(bbox.width * scale);
            bbox.height = (int) Math.round(bbox.height * scale);
        }

        final Rectangle rect = SwingUtilities.convertRectangle(drawingPanel, bbox, diagramPane);

        // bounds will be overwritten later in updateFigText anyway...
//        setBounds(rect.x - _extraSpace, rect.y - _extraSpace,
//                rect.width + _extraSpace * 2, rect.height + _extraSpace * 2);
        
        // XXX AQUI ADICIONA NA TELA !
        if (JavaFXTest.ON) {

            Platform.runLater(new Runnable() {
                public void run() {
                    // This method is invoked on the JavaFX thread
                    Group  root  =  new  Group();
                    Scene  scene  =  new  Scene(root, Color.YELLOW);
                    scene.setFill(Color.TRANSPARENT);
                    
//                  text.setX(40);
//                  text.setY(100);
                  int w = rect.width + _extraSpace * 2;
                  int h = rect.height + _extraSpace * 2;
                  new_this.setMinWidth(w);
                  new_this.setMaxWidth(w);
                  new_this.setMinHeight(h);
                  new_this.setMaxHeight(h);
                  new_this.setFont(new javafx.scene.text.Font(12));
                  new_this.setText("Welcome JavaFX!");
                  root.getChildren().add(new_this);

                  // TODO get rid of the casting and reduce class visibility
                  JFXPanel fxPanel = ((JGraphFXInternalPane)diagramPane);
                  fxPanel.setScene(scene);

                  System.out.println(w);
                  System.out.println(new_this.getWidth());
                  System.out.println(h);
                  System.out.println(new_this.getHeight());

                  System.out.println("JavaFX started!");
                    
                }
            });
            
        } else {
            diagramPane.add(this, JLayeredPane.POPUP_LAYER, 0);
        }
        
        setText(ft.getTextFriend());

        addKeyListener(this);
        requestFocus();
        getDocument().addDocumentListener(this);
        setActiveTextEditor(this);
        setSelectionStart(0);
        setSelectionEnd(getDocument().getLength());

        // XXX refazer no FX
         MutableAttributeSet attr = new SimpleAttributeSet();
         if (ft.getJustification() == FigText.JUSTIFY_CENTER)
         StyleConstants.setAlignment(attr, StyleConstants.ALIGN_CENTER);
         if (ft.getJustification() == FigText.JUSTIFY_RIGHT)
         StyleConstants.setAlignment(attr, StyleConstants.ALIGN_RIGHT);
         final Font font = ft.getFont();
         StyleConstants.setFontFamily(attr, font.getFamily());
         StyleConstants.setFontSize(attr, font.getSize());
         setParagraphAttributes(attr, true);

        if (ie instanceof KeyEvent) {
            setSelectionStart(getDocument().getLength());
            setSelectionEnd(getDocument().getLength());
        }
        addFocusListener(this);
    }

    /**everything else**/
    /*************************************************************************/

    public static void configure(final int extraSpace, final Border b,
            final boolean makeBrighter, final java.awt.Color backgroundColor) {
        _extraSpace = extraSpace;
        _border = b;
        _makeBrighter = makeBrighter;
        _backgroundColor = backgroundColor;
    }

    public void propertyChange(final PropertyChangeEvent pve) {
        updateFigText();
    }

    public void endEditing() {
        if (figText == null) {
            return;
        }
        updateFigText();
        final FigText t = figText;

        cancelEditingInternal();

        t.firePropChange("editing", true, false);
        // This will cause a recursive call back to us, but things
        // are organized so it won't be infinite recursion.
        setActiveTextEditor(null);
    }

    public void cancelEditing() {
        if (figText == null) {
            return;
        }
        cancelEditingInternal();
        // TODO: Should this be firing a property to tell listeners
        // that we cancelled editing? - tfm
    }

    /**
     * Exit editing mode. Code which is common to both cancelEditing() and
     * endEditing(). It undoes everything which was done in init().
     */
    private void cancelEditingInternal() {
        removeFocusListener(this);
        setVisible(false);
        figText.endTrans();
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
        }
        figText.removePropertyChangeListener(this);
        removeKeyListener(this);
        diagramPane.remove(this);
        drawingPanel.requestFocus();
        figText = null;
    }

    // //////////////////////////////////////////////////////////////
    // event handlers for KeyListener implementaion
    public void keyTyped(KeyEvent ke) {
    }

    public void keyReleased(KeyEvent ke) {
    }

    /**
     * End editing on enter or tab if configured. Also ends on escape or F2.
     * This is coded on keypressed rather than keyTyped as keyTyped may already
     * have applied the key to the underlying document.
     */
    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            if (figText.getReturnAction() == FigText.END_EDITING) {
                endEditing();
                ke.consume();
            }
        } else if (ke.getKeyCode() == KeyEvent.VK_TAB) {
            if (figText.getTabAction() == FigText.END_EDITING) {
                endEditing();
                ke.consume();
            }
        } else if (ke.getKeyCode() == KeyEvent.VK_F2) {
            endEditing();
            ke.consume();
        } else if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
            // needs-more-work: should revert to orig text.
            cancelEditing();
            ke.consume();
        }
    }

    // //////////////////////////////////////////////////////////////
    // event handlers for DocumentListener implementaion

    public void insertUpdate(DocumentEvent e) {
        updateFigText();
    }

    public void removeUpdate(DocumentEvent e) {
        updateFigText();
    }

    public void changedUpdate(DocumentEvent e) {
        updateFigText();
    }

    // //////////////////////////////////////////////////////////////
    // internal utility methods

    protected void updateFigText() {
        if (figText == null) {
            return;
        }

        String text = getText();

        figText.setTextFriend(text, getGraphics());

        if (figText.getReturnAction() == FigText.INSERT
                && figText.isWordWrap()) {
            return;
        }

        Rectangle bbox = figText.getBounds();
        Editor ce = Globals.curEditor();
        double scale = ce.getScale();
        bbox.x = (int) Math.round(bbox.x * scale);
        bbox.y = (int) Math.round(bbox.y * scale);

        if (scale > 1) {
            bbox.width = (int) Math.round(bbox.width * scale);
            bbox.height = (int) Math.round(bbox.height * scale);
        }

        bbox = SwingUtilities.convertRectangle(drawingPanel, bbox, diagramPane);

        setBounds(bbox.x - _extraSpace, bbox.y - _extraSpace,
                bbox.width + _extraSpace * 2, bbox.height + _extraSpace * 2);
        setFont(figText.getFont());
    }

    /**
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    public void focusGained(FocusEvent e) {
    }

    /**
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    public void focusLost(FocusEvent e) {
        endEditing();
    }

    public FigText getFigText() {
        return figText;
    }
}
