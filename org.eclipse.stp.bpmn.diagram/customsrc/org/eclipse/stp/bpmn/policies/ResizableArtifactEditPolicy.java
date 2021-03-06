/**
 * Copyright (C) 2000-2007, Intalio Inc.
 *
 * The program(s) herein may be used and/or copied only with the
 * written permission of Intalio Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *
 * Dates       		 Author              Changes
 * Mar 9, 2007      Antoine Toulm&eacute;   Creation
 */
package org.eclipse.stp.bpmn.policies;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.handles.AbstractHandle;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.stp.bpmn.tools.ActivityResizeTracker;
import org.eclipse.stp.bpmn.tools.TaskDragEditPartsTrackerEx;

/**
 * @author <a href="mailto:atoulme@intalio.com">Antoine Toulm&eacute;</a>
 * @author <a href="http://www.intalio.com">&copy; Intalio, Inc.</a>
 */
public class ResizableArtifactEditPolicy extends ResizableShapeEditPolicyEx {

    protected Handle createHandle(GraphicalEditPart owner, int direction) {
        return new ActivityResizeHandle(owner, direction);
    }

    /**
     * Creates new handle for activity.
     */
    protected static class ActivityResizeHandle extends ResizeHandleEx {
        public ActivityResizeHandle(GraphicalEditPart owner, int direction) {
            super(owner, direction);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.stp.bpmn.policies.ResizableShapeEditPolicyEx.ResizeHandleEx#createDragTracker()
         */
        @Override
        protected DragTracker createDragTracker() {
            return new ActivityResizeTracker(getOwner(), cursorDirection);
        }
    }

    @Override
    protected void replaceHandleDragEditPartsTracker(Handle handle) {
        if (handle instanceof AbstractHandle) {
            AbstractHandle h = (AbstractHandle) handle;
            h.setDragTracker(new TaskDragEditPartsTrackerEx((IGraphicalEditPart)getHost()));
        }
    }
    
    

}
