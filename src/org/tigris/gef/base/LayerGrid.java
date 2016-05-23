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

// File: LayerGrid.java
// Classes: LayerGrid
// Original Author: jrobbins@ics.uci.edu
// $Id: LayerGrid.java 1172 2008-12-03 12:32:49Z bobtarling $
// $Id: LayerGrid.java 1172 2008-12-03 12:32:49Z bobtarling $

package org.tigris.gef.base;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.UIManager;

import org.tigris.gef.presentation.Fig;

/**
 * Paint a background drawing guide consisting of horizontal and vertical lines
 * in a neutral color. This feature is common to many drawing applications
 * (e.g., MacDraw). LayerGrid is in concept a Layer, just like any other so it
 * can be composed, locked, grayed, hidden, and reordered.
 * <p>
 */
public class LayerGrid extends Layer {

	private static final long serialVersionUID = 4363123625161512763L;

	/** The spacing between the lines. */
	private int _spacing = 16;

	/**
	 * True means paint grid lines, false means paint only dots where the lines
	 * would intersect. Painting dots is about as useful as painting lines and
	 * it looks less cluttered. But lines are more familiar to some people.
	 */
	private boolean _paintLines = false;

	private boolean _paintDots = true;

	/**
	 * The image stamp is used to paint the grid quickly. Basically I make a
	 * pretty big off screen image and paint my grid on it, then I repeatedly
	 * bitblt that image to the screen (or some other off screen image). More
	 * pixels are being drawn this way, but since bitblt is a fast operation it
	 * works much faster.
	 */
	private transient Image _stamp = null;

	/** The size of the image stamp. */
	private int _stampWidth = 128, _stampHeight = 128;

	/** The color of the grid lines or dots. */
	protected Color _color = new Color(180, 180, 180); // Color.gray;

	/** The color of the space between the lines or dots. */
	protected Color _bgColor = Color.lightGray;

	/**
	 * True means to fill in the image stamp or drawing area with the background
	 * color. False means to just paint the lines or dots.
	 */
	protected boolean _paintBackground = true;

	/** The size of the dots. Dots are actually small rectangles. */
	protected int _dotSize = 2;

	/**
	 * As an example of different grid styles 5 are defined. Needs-More-Work: I
	 * should use the property sheet to adjust the grid.
	 * 
	 * @see LayerGrid#adjust
	 */
	private int _style = 2;

	private final int NUM_STYLES = 5;

	// //////////////////////////////////////////////////////////////
	// constructors

	/** Construct a new LayerGrid and name it 'Grid'. */
	public LayerGrid() {
		super("Grid");
		// Use the default background color as the color between the lines and
		// dots
		_bgColor = UIManager.getColor("Panel.background");
		if (_bgColor == null) {
			_bgColor = Color.lightGray;
		}

		// The color of the lines and dots should be slightly darker than the
		// background color
		final float scale = 0.9f;
		_color = new Color((int) (_bgColor.getRed() * scale), (int) (_bgColor.getGreen() * scale),
				(int) (_bgColor.getBlue() * scale));
	}

	/**
	 * Construct a new LayerGrid with the given foreground color, background
	 * color, line spacing, and lines/dots flag.
	 */
	public LayerGrid(Color fore, Color back, int spacing, boolean lines) {
		super("Grid");
		_color = fore;
		_bgColor = back;
		_spacing = spacing;
		_paintLines = lines;
	}

	// //////////////////////////////////////////////////////////////
	// accessors

	public List<Fig> getContents() {
		return Collections.emptyList();
	}

	public Fig presentationFor(Object obj) {
		return null;
	}

	// //////////////////////////////////////////////////////////////
	// painting methods

	/**
	 * Paint the grid lines or dots by repeatedly bitblting a precomputed
	 * 'stamp' onto the given Graphics
	 */
	public synchronized void paintContents(Graphics g) {
		if (_stamp == null) {
			if (_spacing > _stampHeight)
				_stampHeight = _stampWidth = _spacing;
			if (Globals.curEditor() == null) {
				// this is a bad idea, but it works around a very awkward AWT
				// requirement: that only frames can make Image instances
				System.out.println("no editor");
				Frame frame = new Frame();
				frame.setVisible(true);
				_stamp = frame.createImage(_stampWidth, _stampHeight);
				frame.dispose();
			} else {
				_stamp = Globals.curEditor().createImage(_stampWidth, _stampHeight);
			}
			if (_stamp != null) {
				if (_paintLines)
					paintLines(_stamp, _paintBackground);
				else if (_paintDots)
					paintDots(_stamp, _paintBackground);
			}
		}

		Rectangle clip = g.getClipBounds();
		if (clip != null) {
			int x = clip.x / _spacing * _spacing;
			int y = clip.y / _spacing * _spacing;
			int bot = clip.y + clip.height;
			int right = clip.x + clip.width;

			if (_stamp != null) {
				while (x <= right) {
					y = clip.y / _spacing * _spacing;
					while (y <= bot) {
						g.drawImage(_stamp, x, y, null);
						y += _stampHeight;
					}
					x += _stampWidth;
				}
			}
		}
	}

	/** Paint lines on the given stamp Image. */
	private void paintLines(Image i, boolean paintBackground) {
		Graphics g = i.getGraphics();
		g.clipRect(0, 0, i.getWidth(null), i.getHeight(null));
		paintLines(g, paintBackground);
	}

	/** Paint dots on the given stamp Image. */
	private void paintDots(Image i, boolean paintBackground) {
		Graphics g = i.getGraphics();
		g.clipRect(0, 0, i.getWidth(null), i.getHeight(null));
		paintDots(g, paintBackground);
	}

	/** Paint lines on the given Graphics. */
	private void paintLines(Graphics g, boolean paintBackground) {
		Rectangle clip = g.getClipBounds();
		if (paintBackground) {
			g.setColor(_bgColor);
			g.fillRect(clip.x, clip.y, clip.width, clip.height);
		}
		int x = clip.x / _spacing * _spacing - _spacing;
		int y = clip.y / _spacing * _spacing - _spacing;
		int stepsX = clip.width / _spacing + 2;
		int stepsY = clip.height / _spacing + 2;
		int right = clip.x + clip.width;
		int bot = clip.y + clip.height;
		g.setColor(_color);

		while (stepsX > 0) {
			g.drawLine(x, 0, x, bot);
			x += _spacing;
			--stepsX;
		}
		while (stepsY > 0) {
			g.drawLine(0, y, right, y);
			y += _spacing;
			--stepsY;
		}
	}

	/** Paint dots on the given Graphics. */
	protected void paintDots(Graphics g, boolean paintBackground) {
		Rectangle clip = g.getClipBounds();
		if (paintBackground) {
			g.setColor(_bgColor);
			g.fillRect(clip.x, clip.y, clip.width, clip.height);
		}
		int x = clip.x / _spacing * _spacing - _spacing;
		int y = clip.y / _spacing * _spacing - _spacing;
		int right = clip.x + clip.width;
		int bot = clip.y + clip.height;

		g.setColor(_color);
		while (x <= right) {
			y = 0;
			while (y <= bot) {
				g.fillRect(x, y, _dotSize, _dotSize);
				y += _spacing;
			}
			x += _spacing;
		}
	}

	// //////////////////////////////////////////////////////////////
	// user interface

	/**
	 * Eventually this will open a dialog box to let the user adjust the grid
	 * line spacing, colors, and whether liens or dots are shown. For now it
	 * just cycles among 5 predefined styles.
	 */
	public void adjust() {
		_style = (_style + 1) % NUM_STYLES;
		_stamp = null;
		setHidden(false);
		switch (_style) {
		case 0:
			_paintLines = true;
			_paintDots = true;
			_spacing = 16;
			break;
		case 1:
			_paintLines = true;
			_paintDots = true;
			_spacing = 8;
			break;
		case 2:
			_paintLines = false;
			_paintDots = true;
			_spacing = 16;
			break;
		case 3:
			_paintLines = false;
			_paintDots = true;
			_spacing = 32;
			break;
		case 4:
			_paintLines = false;
			_paintDots = false;
			break;
		}
		refreshEditors();
	}

	/**
	 * This function allows to adjust various properties of this Layer.
	 * <p>
	 * 
	 * Supported are:
	 * <ul>
	 * <li>"spacing", Integer : the size of the grid
	 * <li>"paintLines", Boolean : shows grid as lines (overrules dots)
	 * <li>"paintDots", Boolean : shows grid as dots
	 * 
	 * @see org.tigris.gef.base.Layer#adjust(java.util.HashMap)
	 */
	public void adjust(HashMap map) {
		super.adjust(map);
		Object m;
		_stamp = null;
		setHidden(false);

		m = map.get("spacing");
		if (m instanceof Integer)
			_spacing = ((Integer) m).intValue();

		m = map.get("paintLines");
		if (m instanceof Boolean)
			_paintLines = ((Boolean) m).booleanValue();

		m = map.get("paintDots");
		if (m instanceof Boolean)
			_paintDots = ((Boolean) m).booleanValue();

		refreshEditors();
	}

} /* end class LayerGrid */
