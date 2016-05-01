// Copyright (c) 1996-2011 The Regents of the University of California. All
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

// File: CmdCreateNode.java
// Classes: CmdCreateNode
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdCreateNode.java 1339 2011-08-29 14:00:30Z mvw $

package org.tigris.gef.base;

import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.tigris.gef.graph.GraphFactory;
import org.tigris.gef.graph.GraphModel;
import org.tigris.gef.graph.GraphNodeHooks;
import org.tigris.gef.graph.MutableGraphModel;
import org.tigris.gef.graph.presentation.NetNode;
import org.tigris.gef.presentation.FigNode;
import org.tigris.gef.util.Localizer;

/**
 * This Cmd is executed it makes the new objects as per its arguments, and then
 * it sets the global next mode to ModePlace so that the user can place the new
 * node in any editor window.
 * 
 * @see ModePlace
 * @see NetNode
 * @see FigNode
 */

public class CmdCreateNode extends Cmd implements GraphFactory {

    private static final long serialVersionUID = -4746215260464595235L;

    public static Class<?> DEFAULT_NODE_CLASS =
        org.tigris.gef.graph.presentation.NetNode.class;

    private static Log LOG = LogFactory.getLog(CmdCreateNode.class);

    /**
     * Construct a new Cmd with the given arguments for node class.
     */
    public CmdCreateNode(Hashtable args, String resource, String name) {
        super(args, resource, name);
    }

    public CmdCreateNode(Hashtable args, String name) {
        super(args, "GefBase", name);
    }

    /**
     * Construct a new Cmd with the given classes for the NetNode and its
     * FigNode.
     */
    public CmdCreateNode(Class nodeClass, String resource, String name) {
        this(new Hashtable(), resource, name);
        setArg("className", nodeClass);
    }

    public CmdCreateNode(Class nodeClass, String name) {
        this(new Hashtable(), name);
        setArg("className", nodeClass);
    }

    public CmdCreateNode(Object nodeClass, String name, ImageIcon icon) {
        super(new Hashtable(), name, icon);
        setArg("className", nodeClass);
    }

    /**
     * Construct a new Cmd with the given classes for the NetNode and its
     * FigNode, and set the global sticky mode boolean to the given value. This
     * allows the user to place several nodes rapidly.
     */
    public CmdCreateNode(Class<?> nodeClass, boolean sticky, String resource,
            String name) {
        this(nodeClass, resource, name);
        setArg("shouldBeSticky", sticky ? Boolean.TRUE : Boolean.FALSE);
    }

    public CmdCreateNode(Class<?> nodeClass, boolean sticky, String name) {
        this(nodeClass, name);
        setArg("shouldBeSticky", sticky ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Actually instantiate the NetNode and FigNode objects and set the global
     * next mode to ModePlace
     */
    public void doIt() {
        Editor ce = Globals.curEditor();
        GraphModel gm = ce.getGraphModel();
        if (!(gm instanceof MutableGraphModel))
            return;
        setArg("graphModel", gm);

        String instructions = null;
        Object actionName = getValue(javax.swing.Action.NAME);
        if (actionName != null) {
            instructions = Localizer.localize("GefBase", "ClickToPlace") + " " + actionName.toString();
        }
        Mode placeMode = new ModePlace(this, instructions);

        Object shouldBeSticky = getArg("shouldBeSticky");
        Globals.mode(placeMode, shouldBeSticky == Boolean.TRUE);
        if (LOG.isDebugEnabled())
            LOG.debug("Mode set to ModePlace with sticky mode "
                    + shouldBeSticky);
    }

    public void undoIt() {
        LOG.warn("undo is not implemented");
    }

    // //////////////////////////////////////////////////////////////
    // GraphFactory implementation

    public GraphModel makeGraphModel() {
        return null;
    }

    public Object makeEdge() {
        return null;
    }

    /**
     * Factory method for creating a new NetNode from the className argument.
     * TODO This returns null on error. We need to define some basic exception
     * classes.
     */
    public Object makeNode() {
        Object newNode;
        Object nodeType = getArg("className", DEFAULT_NODE_CLASS);
        if (nodeType instanceof Action) {
            // TODO: This is a NPE. What is the purpose of this?
            Action a = null;
            a.actionPerformed(null);
            newNode = a.getValue("node");
        } else {
            Class nodeClass = (Class) getArg("className", DEFAULT_NODE_CLASS);
            // assert _nodeClass != null
            try {
                newNode = nodeClass.newInstance();
            } catch (java.lang.IllegalAccessException ignore) {
                LOG.error("Unable to instantiate node " + nodeClass.getName());
                return null;
            } catch (java.lang.InstantiationException ignore) {
                LOG.error("Failed to instantiate node " + nodeClass.getName());
                return null;
            }
        }
        LOG.debug("New node created " + newNode);

        if (newNode instanceof GraphNodeHooks) {
            LOG.debug("Initializing GraphNodeHooks");
            ((GraphNodeHooks) newNode).initialize(_args);
        }
        return newNode;
    }

    // //////////////////////////////////////////////////////////////
    // for testing purpose only
    public Object getActiveGraphModel() {
        return getArg("graphModel");
    }

} /* end class CmdCreateNode */
