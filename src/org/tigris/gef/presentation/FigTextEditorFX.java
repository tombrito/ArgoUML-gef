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
import java.awt.Rectangle;

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
 * @author Tom Brito
 */
public class FigTextEditorFX extends TextField implements TextEditor {

	private TextField new_this;
	Group group;
	Scene scene;

	/*************************************************************************/

	private static FigTextEditorFX _activeTextEditor = null;

	public static void setActiveTextEditor(FigTextEditorFX fte) {
		if (_activeTextEditor != null) {
			_activeTextEditor.endEditing();
		}
		_activeTextEditor = fte;
	}

	/*************************************************************************/

	private static final Log LOG = LogFactory.getLog(FigTextEditorFX.class);

	/*************************************************************************/

	private FigText figText;

	private Container drawingPanel;

	/*************************************************************************/

	public FigTextEditorFX(final FigText ft) {

		System.out.println("new FigTextEditorFX()");

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

		ft.calcBounds();

		Rectangle bounds = ft.getBounds();
		final double scale = currEditor.getScale();
		bounds.x = (int) Math.round(bounds.x * scale);
		bounds.y = (int) Math.round(bounds.y * scale);

		if (scale > 1) {
			bounds.width = (int) Math.round(bounds.width * scale);
			bounds.height = (int) Math.round(bounds.height * scale);
		}

		// XXX AQUI ADICIONA NA TELA !

		Platform.runLater(new Runnable() {
			public void run() {
				scene.setFill(Color.TRANSPARENT);

				// text.setX(40);
				// text.setY(100);
				int w = bounds.width;
				int h = bounds.height;
				new_this.setMinWidth(w);
				new_this.setMaxWidth(w);
				new_this.setMinHeight(h);
				new_this.setMaxHeight(h);
				new_this.setFont(new javafx.scene.text.Font(12));

				System.out.println("Creating new_this w:" + w + " h:" + h);

				group.getChildren().add(new_this);

				// TODO get rid of the casting and reduce class visibility
				JFXPanel fxPanel = ((JGraphFXInternalPane) drawingPanel);
				fxPanel.setScene(scene);

				new_this.setOnKeyPressed(new EventHandler<KeyEvent>() {

					public void handle(KeyEvent event) {
						fxKeyPressed(event);

					}
				});

				setText(ft.getTextFriend());
				requestFocus();
				// getDocument().addDocumentListener(FigTextEditor.this);
				{
					new_this.textProperty().addListener((obsevable, newValue, oldValue) -> {
						updateFigText();
					});
				}

				setActiveTextEditor(FigTextEditorFX.this);

				new_this.focusedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {

						if (newValue) {
							System.out.println("Textfield on focus");
						} else {
							System.out.println("Textfield out focus (but not exiting, for testing)");
							// endEditing();
						}

					}
				});

				System.out.println("JavaFX started!");

			}
		});

	}

	/** everything else **/
	/*************************************************************************/

	public void endEditing() {
		if (figText == null) {
			return;
		}
		updateFigText();
		final FigText t = figText;

		endEditingInternal();

		t.firePropChange("editing", true, false);

		// This will cause a recursive call back to us, but things
		// are organized so it won't be infinite recursion.
		if (JavaFXTest.ON) {
			Platform.runLater(new Runnable() {
				public void run() {
					setActiveTextEditor(null);
				}
			});
		} else {
			setActiveTextEditor(null);
		}
	}

	public void cancelEditing() {
		System.out.println("cancelEditing()");
		if (figText == null) {
			return;
		}
		endEditingInternal();
	}

	/**
	 * Exit editing mode. Code which is common to both cancelEditing() and
	 * endEditing(). It undoes everything which was done in init().
	 */
	private void endEditingInternal() {

		System.out.println("endEditingInternal()");

		LOG.trace(this.getClass().getName() + " endEditingInternal()");
		LOG.debug(this.getClass().getName() + " endEditingInternal()");
		LOG.info(this.getClass().getName() + " endEditingInternal()");
		LOG.error(this.getClass().getName() + " endEditingInternal()");

		Platform.runLater(new Runnable() {
			public void run() {

				if (figText == null) {
					System.err.println("endEditingInternal() ia cancelar, mas figText está nulo...");
					return;
				}

				figText.endTrans();

				// Container parent = getParent();
				// if (parent != null) {
				// parent.remove(this);
				// }
				group.getChildren().remove(new_this);

				// TODO create a memory leak test to check the need of this
				// (swing old code)
				// removeKeyListener(this);

				// drawingPanel.remove(this);
				drawingPanel.requestFocus();

				System.out.println("endEditingInternal() (FX) setando figText para NULL");
				figText = null;
			}
		});
	}

	// //////////////////////////////////////////////////////////////
	// event handlers for KeyListener implementaion

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
			ke.consume();
		} else if (ke.getCode() == KeyCode.ESCAPE) {
			// needs-more-work: should revert to orig text.
			cancelEditing();
			ke.consume();
		}

	}

	// //////////////////////////////////////////////////////////////
	// internal utility methods

	protected void updateFigText() {
		if (figText == null) {
			return;
		}

		Platform.runLater(new Runnable() {
			public void run() {
				if (figText == null) { //XXX uso esse ou o de cima?
					return;
				}
				String text;
				text = new_this.getText();
				figText.setTextFriend(text);
			}
		});
	}

	public FigText getFigText() {
		return figText;
	}
}
