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

/** 
 * Date             Author             Changes 
 * 16 Oct 2006      bilchyshyn         Created 
 **/
package org.eclipse.stp.bpmn.figures.router;

/**
 * Connection router for sequence edges that souyrce anchor is on
 * an event shape on the border of a sub-process shape.
 * 
 * hmalphettes
 * @author <a href="http://www.intalio.com">&copy; Intalio, Inc.</a>
 */
public class EventHandlersRectilinearRouter extends RectilinearRouterEx {

    /**
     * Constructs a rectilinear router tht normalizes on the x-axis first.
     */
    public EventHandlersRectilinearRouter() {
        super.normalizeBehavior = NORMALIZE_ON_HORIZONTAL_EXCEPT_FIRST_SEG;
    }

}
