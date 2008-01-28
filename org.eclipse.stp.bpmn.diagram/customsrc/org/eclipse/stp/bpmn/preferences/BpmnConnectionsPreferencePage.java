/******************************************************************************
 * Copyright (c) 2006, Intalio Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intalio Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.stp.bpmn.preferences;

import org.eclipse.gmf.runtime.diagram.ui.preferences.ConnectionsPreferencePage;
import org.eclipse.gmf.runtime.notation.Routing;
import org.eclipse.gmf.runtime.notation.Smoothness;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.stp.bpmn.diagram.BpmnDiagramMessages;
import org.eclipse.stp.bpmn.diagram.part.BpmnDiagramEditorPlugin;
import org.eclipse.stp.bpmn.diagram.part.BpmnDiagramPreferenceInitializer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Preference page. Follow the tutorial here:
 * <link>http://help.eclipse.org/help32/topic/org.eclipse.gmf.doc/tutorials/diagram/diagramPreferencesTutorial.html</link>
 * 
 * @author hmalphettes
 * @author <a href="http://www.intalio.com">&copy; Intalio, Inc.</a>
 */
public class BpmnConnectionsPreferencePage extends ConnectionsPreferencePage {

    /**
     * the connection line style for the messaging edge;
     */
    private static String PREF_MSG_LINE_STYLE =
        BpmnDiagramPreferenceInitializer.PREF_MSG_LINE_STYLE;
    /**
     * the connection line style for the sequence edge;
     */
    private static String PREF_SEQ_LINE_STYLE =
        BpmnDiagramPreferenceInitializer.PREF_SEQ_LINE_STYLE;
    /**
     * whether new message connections should be routed with the shortest path
     */
    private static String PREF_MSG_ROUTE_SHORTEST =
        BpmnDiagramPreferenceInitializer.PREF_MSG_ROUTE_SHORTEST;
    /**
     * whether new sequence connections should be routed with the shortest path
     */
    private static String PREF_SEQ_ROUTE_SHORTEST =
        BpmnDiagramPreferenceInitializer.PREF_SEQ_ROUTE_SHORTEST;
    /**
     * whether new message connections should use avoid obstacle algo
     */
    private static String PREF_MSG_ROUTE_AVOID_OBSTACLES =
        BpmnDiagramPreferenceInitializer.PREF_MSG_ROUTE_AVOID_OBSTACLES;
    /**
     * whether new sequence connections should use avoid obstacle algo
     */
    private static String PREF_SEQ_ROUTE_AVOID_OBSTACLES =
        BpmnDiagramPreferenceInitializer.PREF_SEQ_ROUTE_AVOID_OBSTACLES;
    /**
     * seq edge smoothness
     */
    private static String PREF_SEQ_ROUTE_SMOOTH_FACTOR =
        BpmnDiagramPreferenceInitializer.PREF_SEQ_ROUTE_SMOOTH_FACTOR;
    /**
     * msg edge smoothness
     */
    private static String PREF_MSG_ROUTE_SMOOTH_FACTOR =
        BpmnDiagramPreferenceInitializer.PREF_MSG_ROUTE_SMOOTH_FACTOR;
    
    public BpmnConnectionsPreferencePage() {
        super();
        setPreferenceStore(BpmnDiagramEditorPlugin.getInstance().getPreferenceStore());
    }
    
    /**
     * Add to the standard field the default settings for sequence
     * and messaging edges in bpmn diagrams.
     */
    protected void addFields(Composite parent) {
//      this one is a bit ugly and confusing for now
        //it would add display the rpeference for connetions related to text and notes attachments.
//        super.addFieldEditors(parent);
        
        addSequenceConnectionFields(parent);
        addMessageConnectionFields(parent);
    }

    /**
     * Preferences for the messaging connection.
     * @param parent
     */
    protected void addMessageConnectionFields(Composite parent) {
        addConnectionFields(parent, BpmnDiagramMessages.BpmnConnectionsPreferencePage_message_connections_menu,
                PREF_MSG_LINE_STYLE,
                PREF_MSG_ROUTE_AVOID_OBSTACLES,
                PREF_MSG_ROUTE_SHORTEST,
                PREF_MSG_ROUTE_SMOOTH_FACTOR);
    }
    /**
     * Preferences for the messaging connection.
     * @param parent
     */
    protected void addSequenceConnectionFields(Composite parent) {
        addConnectionFields(parent, BpmnDiagramMessages.BpmnConnectionsPreferencePage_flow_connections_menu,
                PREF_SEQ_LINE_STYLE,
                PREF_SEQ_ROUTE_AVOID_OBSTACLES,
                PREF_SEQ_ROUTE_SHORTEST,
                PREF_SEQ_ROUTE_SMOOTH_FACTOR);
    }
    /**
     * Preferences for the messaging connection.
     * @param parent
     */
    protected void addConnectionFields(Composite parent,
            String groupTitle, String msgLineStylePref,
            String prefAvoidObstacles, String prefShortestPath,
            String prefSmoothness) {

        Group bpmnGlobalGroup = new Group(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        bpmnGlobalGroup.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        bpmnGlobalGroup.setLayoutData(gridData);
        bpmnGlobalGroup.setText(groupTitle);

        Composite newParent = new Composite(bpmnGlobalGroup, SWT.NONE);
        
        RadioGroupFieldEditor routerStyleEditor= new RadioGroupFieldEditor(
                msgLineStylePref, BpmnDiagramMessages.BpmnConnectionsPreferencePage_style, 2,
            new String[][] {
                {Routing.RECTILINEAR_LITERAL.getLiteral(), Routing.RECTILINEAR_LITERAL.getName()},
                {BpmnDiagramMessages.BpmnConnectionsPreferencePage_oblique/*Routing.MANUAL_LITERAL.getLiteral()*/, Routing.MANUAL_LITERAL.getName()}
            },
            newParent, true);
        routerStyleEditor.setPreferenceStore(super.getPreferenceStore());
        super.addField(routerStyleEditor);
        
        BooleanFieldEditor avoidObstacles = new BooleanFieldEditor(
                prefAvoidObstacles,
                BpmnDiagramMessages.BpmnConnectionsPreferencePage_avoid_obstacles,
                newParent);
        super.addField(avoidObstacles);
//        avoidObstacles.getLabelControl(newParent).setToolTipText(
//                "Rounds around obstacles. When true shortest path setting" +
//                "is not taken into account.");
        
        BooleanFieldEditor shortestPath = new BooleanFieldEditor(
                prefShortestPath,
                BpmnDiagramMessages.BpmnConnectionsPreferencePage_shortest_path,
                newParent);
//        shortestPath.getLabelControl(newParent).setToolTipText(
//                "Use the shortest path. Not taken into account" +
//                " when avoid obstacles is in use.");
        super.addField(shortestPath);
        
       RadioGroupFieldEditor smoothnessEditor= new RadioGroupFieldEditor(
               prefSmoothness, BpmnDiagramMessages.BpmnConnectionsPreferencePage_smoothness, 2,
           new String[][] {
               {Smoothness.NORMAL_LITERAL.getLiteral(), Smoothness.NORMAL_LITERAL.getName()},
               {Smoothness.NONE_LITERAL.getLiteral(), Smoothness.NONE_LITERAL.getName()},
               {Smoothness.LESS_LITERAL.getLiteral(), Smoothness.LESS_LITERAL.getName()},
               {Smoothness.MORE_LITERAL.getLiteral(), Smoothness.MORE_LITERAL.getName()}
           },
           newParent, true);
       smoothnessEditor.setPreferenceStore(super.getPreferenceStore());
       super.addField(smoothnessEditor);
    }
}
