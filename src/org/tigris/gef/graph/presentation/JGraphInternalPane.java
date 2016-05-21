package org.tigris.gef.graph.presentation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ToolTipManager;

import org.tigris.gef.base.Editor;
import org.tigris.gef.base.Globals;

class JGraphInternalPane extends JPanel {

    static final long serialVersionUID = -5067026168452437942L;

    private Editor _editor;

    private boolean registeredWithTooltip;

    public JGraphInternalPane(Editor e) {
        _editor = e;
        setLayout(null);
        setDoubleBuffered(false);
    }

    public void paintComponent(Graphics g) {
        // XXX mesmo comentando, o JTextEditor ainda pinta
        _editor.paint(g);
//        super.paintComponent(g);
//        g.setColor(Color.white);
//        g.fillRect(0, 0, 6000, 6000);
    }

    public Graphics getGraphics() {
        // XXX se retornar null, ele nao pinta o JText, mas fica cursor de texto
        Graphics res = super.getGraphics();
        if (res == null) {
            return res;
        }
        Component parent = getParent();

        if (parent instanceof JViewport) {
            JViewport view = (JViewport) parent;
            Rectangle bounds = view.getBounds();
            Point pos = view.getViewPosition();
            res.clipRect(bounds.x + pos.x - 1, bounds.y + pos.y - 1,
                    bounds.width + 1, bounds.height + 1);
        }
        return res;
    }

    public Point getToolTipLocation(MouseEvent event) {
        event = Globals.curEditor().retranslateMouseEvent(event);
        return (super.getToolTipLocation(event));
    }

    public void setToolTipText(String text) {
        if ("".equals(text)) text = null;
        putClientProperty(TOOL_TIP_TEXT_KEY, text);
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        // if (text != null) {
        if (!registeredWithTooltip) {
            toolTipManager.registerComponent(this);
            registeredWithTooltip = true;
        }
    }

    protected void processMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            requestFocus();
        }

        super.processMouseEvent(e); // XXX só essa cara é responsavel pelo
                                    // clique neste aquivo Java... Onde trata?
    }

    /** Tell Swing/AWT that JGraph handles tab-order itself. */
    public boolean isManagingFocus() {
        return true;
    }

    /** Tell Swing/AWT that JGraph can be tabbed into. */
    public boolean isFocusTraversable() {
        return true;
    }

} /* end class JGraphInternalPane */