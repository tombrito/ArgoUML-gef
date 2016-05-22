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
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

/**
 * A text pane for on screen editing of a FigText.
 * 
 * @author jrobbins
 */
public class FigTextEditor extends JTextPane
		implements PropertyChangeListener, DocumentListener, KeyListener, FocusListener, TextEditor {

	private TextField new_this;

	Group group;

	Scene scene;

	private static FigTextEditor _activeTextEditorFX = null;

	public static void setActiveTextEditorFX(FigTextEditor fte) {
		if (_activeTextEditor != null) {
			_activeTextEditor.endEditing();
		}
		_activeTextEditor = fte;
	}

	/*************************************************************************/

	private static final long serialVersionUID = 7350660058167121420L;

	private static FigTextEditor _activeTextEditor = null;

	public static void setActiveTextEditor(FigTextEditor fte) {
		if (_activeTextEditor != null) {
			_activeTextEditor.endEditing();
		}
		_activeTextEditor = fte;
	}

	/*************************************************************************/

	private static Log LOG = LogFactory.getLog(FigTextEditor.class);

	private static int _extraSpace = 2;

	/*************************************************************************/

	private FigText figText;

	private Container drawingPanel;

	/*************************************************************************/

	public FigTextEditor(final FigText ft, InputEvent ie) {

		if (JavaFXTest.ON) {
			new_this = new TextField();
			group = new Group();
			scene = new Scene(group, Color.YELLOW);
		} else {

		}

		figText = ft;
		final Editor currEditor = Globals.curEditor();

		// o mesmo JGraphFXInternalPane BLZ !!!
		drawingPanel = currEditor.getJComponent();

		UndoManager.getInstance().startChain();
		figText.firePropChange("editing", false, true);
		figText.addPropertyChangeListener(this);

		ft.calcBounds();

		Rectangle bbox = ft.getBounds();
		final double scale = currEditor.getScale();
		bbox.x = (int) Math.round(bbox.x * scale);
		bbox.y = (int) Math.round(bbox.y * scale);

		if (scale > 1) {
			bbox.width = (int) Math.round(bbox.width * scale);
			bbox.height = (int) Math.round(bbox.height * scale);
		}

		final Rectangle rect = SwingUtilities.convertRectangle(drawingPanel, bbox, drawingPanel);

		// bounds will be overwritten later in updateFigText anyway...
		// setBounds(rect.x - _extraSpace, rect.y - _extraSpace,
		// rect.width + _extraSpace * 2, rect.height + _extraSpace * 2);

		// XXX AQUI ADICIONA NA TELA !
		drawingPanel.add(this, JLayeredPane.POPUP_LAYER, 0);
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

		if (ie instanceof java.awt.event.KeyEvent) {
			setSelectionStart(getDocument().getLength());
			setSelectionEnd(getDocument().getLength());
		}

		addFocusListener(this);

	}

	/** everything else **/
	/*************************************************************************/

	public static void configure(final int extraSpace, final Border b, final boolean makeBrighter,
			final java.awt.Color backgroundColor) {
		_extraSpace = extraSpace;
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
		if (JavaFXTest.ON) {
			Platform.runLater(new Runnable() {
				public void run() {
					setActiveTextEditorFX(null);
				}
			});
		} else {
			setActiveTextEditor(null);
		}
	}

	public void cancelEditing() {
		if (figText == null) {
			return;
		}
		cancelEditingInternal();
	}

	/**
	 * Exit editing mode. Code which is common to both cancelEditing() and
	 * endEditing(). It undoes everything which was done in init().
	 */
	private void cancelEditingInternal() {

		System.out.println("cancelEditingInternal()");

		if (JavaFXTest.ON) {
			Platform.runLater(new Runnable() {
				public void run() {

					if (figText == null) {
						System.err.println("cancelEditingInternal() ia cancelar, mas figText está nulo...");
						return;
					}

					figText.endTrans();

					// Container parent = getParent();
					// if (parent != null) {
					// parent.remove(this);
					// }
					group.getChildren().remove(new_this);

					figText.removePropertyChangeListener(FigTextEditor.this);

					// TODO create a memory leak test to check the need of this
					// (swing old code)
					// removeKeyListener(this);

					// drawingPanel.remove(this);
					drawingPanel.requestFocus();

					System.out.println("cancelEditingInternal() (FX) setando figText para NULL");
					figText = null;
				}
			});
		} else {
			removeFocusListener(this);
			setVisible(false);
			figText.endTrans();
			Container parent = getParent();
			if (parent != null) {
				parent.remove(this);
			}
			figText.removePropertyChangeListener(this);
			removeKeyListener(this);
			drawingPanel.remove(this);
			drawingPanel.requestFocus();

			System.out.println("cancelEditingInternal() setando figText para NULL");
			figText = null;
		}
	}

	// //////////////////////////////////////////////////////////////
	// event handlers for KeyListener implementaion
	public void keyTyped(java.awt.event.KeyEvent ke) {
	}

	public void keyReleased(java.awt.event.KeyEvent ke) {
	}

	/**
	 * End editing on enter or tab if configured. Also ends on escape or F2.
	 * This is coded on keypressed rather than keyTyped as keyTyped may already
	 * have applied the key to the underlying document.
	 */
	public void fxKeyPressed(KeyEvent ke) {

		System.out.println("Key Typed: " + ke.getCode());

		if (ke.getCode() == KeyCode.ENTER) {
			if (figText.getReturnAction() == FigText.END_EDITING) {
				endEditing();
				ke.consume();
			}
		} else if (ke.getCode() == KeyCode.TAB) {
			if (figText.getTabAction() == FigText.END_EDITING) {
				endEditing();
				ke.consume();
			}
		} else if (ke.getCode() == KeyCode.F2) {
			endEditing();
			ke.consume();
		} else if (ke.getCode() == KeyCode.ESCAPE) {
			// needs-more-work: should revert to orig text.
			cancelEditing();
			ke.consume();
		}
	}

	/**
	 * End editing on enter or tab if configured. Also ends on escape or F2.
	 * This is coded on keypressed rather than keyTyped as keyTyped may already
	 * have applied the key to the underlying document.
	 */
	public void keyPressed(java.awt.event.KeyEvent ke) {
		if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
			if (figText.getReturnAction() == FigText.END_EDITING) {
				endEditing();
				ke.consume();
			}
		} else if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {
			if (figText.getTabAction() == FigText.END_EDITING) {
				endEditing();
				ke.consume();
			}
		} else if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_F2) {
			endEditing();
			ke.consume();
		} else if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
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
		// XXX nao posso fazer isso só no enter? tem q ser a cada tecla?
		updateFigText();
	}

	// //////////////////////////////////////////////////////////////
	// internal utility methods

	protected void updateFigText() {
		if (figText == null) {
			return;
		}

		if (JavaFXTest.ON) {
			Platform.runLater(new Runnable() {
				public void run() {
					if (figText == null) {
						return;
					}
					String text;
					text = new_this.getText();
					figText.setTextFriend(text);
				}
			});
		} else {
			String text;
			text = getText();
			figText.setTextFriend(text);
		}

		if (figText.getReturnAction() == FigText.INSERT && figText.isWordWrap()) {
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

		bbox = SwingUtilities.convertRectangle(drawingPanel, bbox, drawingPanel);

		// XXX needed for FX?
		setBounds(bbox.x - _extraSpace, bbox.y - _extraSpace, bbox.width + _extraSpace * 2,
				bbox.height + _extraSpace * 2);
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
