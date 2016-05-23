// $Id: FigCircle.java 1281 2009-09-30 07:27:13Z mvw $
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

package org.tigris.gef.presentation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

/**
 * Primitive Fig for displaying circles and ovals.
 * 
 * @author ics125 spring 1996
 */
public class FigCircle extends Fig {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7376986113799307733L;

	/**
	 * Used as a percentage tolerance for making it easier for the user to
	 * select a hollow circle with the mouse. Needs-More-Work: This is bad
	 * design that needs to be changed. Should use just GRIP_FACTOR.
	 */
	public static final double CIRCLE_ADJUST_RADIUS = 0.1;

	protected boolean _isDashed = false;

	// //////////////////////////////////////////////////////////////
	// constructors

	/**
	 * Construct a new FigCircle with the given position, size, and attributes.
	 */
	public FigCircle(int x, int y, int w, int h) {
		super(x, y, w, h);
	}

	/**
	 * Construct a new FigCircle with the given position, size, line color, and
	 * fill color
	 */
	public FigCircle(int x, int y, int w, int h, Color lColor, Color fColor) {
		super(x, y, w, h, lColor, fColor);
	}

	/** Construct a new FigCircle w/ the given position and size. */
	public FigCircle(int x, int y, int w, int h, boolean resizable) {
		super(x, y, w, h);
		setResizable(resizable);
	}

	/**
	 * Construct a new FigCircle w/ the given position, size, line color, and
	 * fill color.
	 */
	public FigCircle(int x, int y, int w, int h, boolean resizable, Color lColor, Color fColor) {
		super(x, y, w, h, lColor, fColor);
		setResizable(resizable);
	}

	// //////////////////////////////////////////////////////////////
	// display methods

	/* Draw this FigCircle. */
	public void paint(Graphics g) {

		final int lineWidth = getLineWidth();

		if (g instanceof Graphics2D) {
			paint((Graphics2D) g);
		} else if (getFilled() && getFillColor() != null) {
			if (lineWidth > 0 && getLineColor() != null) {
				g.setColor(getLineColor());
				g.fillOval(getX(), getY(), getWidth(), getHeight());
			}

			if (!getFillColor().equals(getLineColor())) {
				g.setColor(getFillColor());
				g.fillOval(getX() + lineWidth, getY() + lineWidth, getWidth() - (lineWidth * 2), getHeight() - (lineWidth * 2));
			}
		} else if (lineWidth > 0 && getLineColor() != null) {
			g.setColor(getLineColor());
			g.drawOval(getX(), getY(), getWidth(), getHeight());
		}
	}

	private void paint(Graphics2D g2) {
		final int lineWidth = getLineWidth();
		Stroke oldStroke = g2.getStroke();
		Paint oldPaint = g2.getPaint();
		g2.setStroke(getDefaultStroke(lineWidth));
		if (getFilled() && getFillColor() != null) {
			g2.setPaint(getDefaultPaint(getFillColor(), getLineColor(), getX(), getY(), getWidth(), getHeight()));
			g2.fill(new Ellipse2D.Float(getX() + lineWidth, getY() + lineWidth, getWidth() - (2 * lineWidth), getHeight() - (2 * lineWidth)));
		}

		if (lineWidth > 0 && getLineColor() != null) {
			g2.setPaint(getLineColor());
			g2.draw(new Ellipse2D.Float(getX() + lineWidth / 2, getY() + lineWidth / 2, getWidth() - lineWidth, getHeight() - lineWidth));
		}

		g2.setStroke(oldStroke);
		g2.setPaint(oldPaint);
	}

	public void appendSvg(StringBuffer sb) {
		sb.append("<ellipse id='").append(getId()).append("'");
		appendSvgStyle(sb);
		sb.append("cx='").append(getCenter().x).append("'").append("cy='").append(getCenter().y).append("'")
				.append("rx='").append(getWidth() / 2).append("'").append("ry='").append(getHeight() / 2)
				.append("' />");
	}

	/** Reply true if the given coordinates are inside the circle. */
	public boolean contains(int x, int y) {
		if (!super.contains(x, y)) {
			return false;
		}

		double dx = (double) (getX() + getWidth() / 2 - x) * 2 / getWidth();
		double dy = (double) (getY() + getHeight() / 2 - y) * 2 / getHeight();
		double distSquared = dx * dx + dy * dy;
		return distSquared <= 1.01;
	}

	/**
	 * Calculate the border point of the ellipse that is on the edge between the
	 * center and the point given by the parameter.
	 * <p>
	 * We use a coordinate system with (0, 0) at the center of the ellipse, this
	 * to keep the formulas understandable.
	 * 
	 * rx is the horizontal radius of the ellipse, ry is the vertical radius of
	 * the ellipse.
	 * <p>
	 * The left top of the ellipse is at (_x, _y), hence the center is at (_x +
	 * rx, _y + ry).
	 * <p>
	 * The formula for any point (x,y) on the centered ellipse is:
	 * <p>
	 * x²/rx² + y²/ry² = 1
	 * <p>
	 * 
	 * The given point is at (dx, dy) in the coordinate system with the center
	 * at the center of the ellipse.
	 * <p>
	 * The formula for any point (x, y) on the line from (dx, dy) to the center
	 * is:
	 * <p>
	 * x/y = dx/dy
	 * <p>
	 * 
	 * Some mathematics now leads to the following:
	 * <p>
	 * dd = ry² dx² + rx² dy²
	 * <p>
	 * mu = rx ry sqrt(dd)
	 * <p>
	 * And the result is the point (mu dx, mu dy), which we translate to the
	 * original coordinate system.
	 */
	public Point connectionPoint(Point anotherPt) {
		double rx = getWidth() / 2;
		double ry = getHeight() / 2;
		double dx = anotherPt.x - (getX() + rx);
		double dy = anotherPt.y - (getY() + ry);
		double dd = ry * ry * dx * dx + rx * rx * dy * dy;
		double mu = rx * ry / Math.sqrt(dd);
		Point res = new Point((int) (mu * dx + getX() + rx), (int) (mu * dy + getY() + ry));
		return res;
	}

} /* end class FigCircle */