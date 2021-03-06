/******************************************************************************
 * Copyright (c) 2006, Intalio Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intalio Inc. - initial API and implementation
 *******************************************************************************
 * Dates       		 Author              Changes
 * Dec 20, 2006      Antoine Toulm&eacute;   Creation
 */
package org.eclipse.stp.bpmn.policies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.OpenEditPolicy;
import org.eclipse.stp.bpmn.diagram.BpmnDiagramMessages;
import org.eclipse.stp.bpmn.diagram.part.BpmnDiagramEditorPlugin;
import org.eclipse.stp.bpmn.dnd.file.FileDnDConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * This edit policy opens a file when there is one attached
 * to the edit part through an annotation.
 * @author <a href="mailto:atoulme@intalio.com">Antoine Toulm&eacute;</a>
 * @author <a href="http://www.intalio.com">&copy; Intalio, Inc.</a>
 */
public class OpenFileEditPolicy extends OpenEditPolicy {

	@Override
	protected Command getOpenCommand(Request request) {
		if (request instanceof SelectionRequest) {
			return null;
		}
		EditPart part = getTargetEditPart(request);
		if (part == null) {
			return null;
		}
		IWorkbenchWindow  window = PlatformUI.getWorkbench().
			getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
	    IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return null;
		}
		if (part instanceof IGraphicalEditPart) {
			EObject object =((IGraphicalEditPart) part).getNotationView().getElement();
			if (object instanceof EModelElement) {
			    return getOpenCommand(request, (IGraphicalEditPart)part, (EModelElement)object, page);
			}
			
		}
		return null;
	}
	
	protected Command getOpenCommand(Request request, IGraphicalEditPart part,
	        EModelElement model,
	        final IWorkbenchPage page) {
	    
		EAnnotation ann = model.getEAnnotation(FileDnDConstants.ANNOTATION_SOURCE);
		if (ann == null) {
			return null;
		}
		String filePath = (String) ann.getDetails().get(
					FileDnDConstants.PROJECT_RELATIVE_PATH);
		String line = (String) ann.getDetails().get(
				FileDnDConstants.LINE_NUMBER);
		
	    IFile ourFile = WorkspaceSynchronizer.getFile(
				model.eResource()).getProject().getFile(filePath);
	    
		return getOpenCommand(ourFile, line, page, null);
	}
	
	protected Command getOpenCommand(final IFile fileToOpen, Object line,
	        final IWorkbenchPage page,
	        Map<String,Object> otherOptionalGotoMarkerAttributes) {
		IMarker marker = null;
		if (line != null || otherOptionalGotoMarkerAttributes != null) {
			try {
			    if (otherOptionalGotoMarkerAttributes == null) {
			        otherOptionalGotoMarkerAttributes = new HashMap<String, Object>();
			    }
			    marker = //use an in memory marker so that wierd things
			        //don't start to appear on the shapes.
	                //must use this type of marker to be taken into account by the navigation service.
			        new InMemoryMarker(fileToOpen,
			                "org.eclipse.stp.bpmn.validation.diagnostic"); //$NON-NLS-1$
                otherOptionalGotoMarkerAttributes.put(IMarker.TRANSIENT, true);
                if (line != null) {
                    otherOptionalGotoMarkerAttributes.put(IMarker.LINE_NUMBER, 
                        line instanceof Integer ? ((Integer)line).intValue()
                                : Integer.valueOf((String)line).intValue());
                }
                marker.setAttributes(otherOptionalGotoMarkerAttributes);
			} catch (CoreException e1) {
				// kill the exception and just open the file
			    marker = null;
			} catch (NumberFormatException e2) {
				// kill the exception and just open the file too.\
			    marker = null;
			}
		}
		if (marker == null) {
			Command co = new Command(BpmnDiagramMessages.OpenFileEditPolicy_command_name) {
				
				@Override
				public void execute() {

				try {
					IDE.openEditor(page,fileToOpen);
				} catch (PartInitException e) {
					BpmnDiagramEditorPlugin.getInstance().getLog().log(
							new Status(
								IStatus.ERROR,
								BpmnDiagramEditorPlugin.ID,
								IStatus.ERROR, 
								e.getMessage(), 
								e));
				}
			}};
			return co;
		} else {
			final IMarker finalMarker = marker;
			Command co = 
				new Command(BpmnDiagramMessages.bind(BpmnDiagramMessages.OpenFileEditPolicy_command_name_with_line, line)) { 
				
				@Override
				public void execute() {

				try {
				    IDE.openEditor(page, finalMarker);
				} catch (PartInitException e) {
					BpmnDiagramEditorPlugin.getInstance().getLog().log(
							new Status(
									IStatus.ERROR, 
									BpmnDiagramEditorPlugin.ID,
									IStatus.ERROR, 
									e.getMessage(), 
									e));
				}
			}};
			return co;
		}
	}
	
    /**
     * In-memory marker to be able to open an editor at a particular line.
     * Make sure we leave the resource untouched so no weird decoration appears on the shapes.
     * 
     * @author hmalphettes
     */
    private static class InMemoryMarker implements IMarker {
        private long creationTime = System.currentTimeMillis();

        private Map<String, Object> attributes = new HashMap<String, Object>();

        private IResource resource;

        private String type;

        /**
         * Creates a marker with a dummy type.
         * Handy when we need to create quickly a marker to open an editor at the right line. 
         * @param resource
         */
        public InMemoryMarker(IResource resource) {
            this(resource, "yo"); //$NON-NLS-1$
        }
        public InMemoryMarker(IResource resource, String type) {
            assert resource != null;
            this.resource = resource;
            this.type = type;
        }

        public void delete() throws CoreException {
        }

        public boolean exists() {
            return true;
        }

        public Object getAttribute(String attributeName) throws CoreException {
            return attributes.get(attributeName);
        }

        public int getAttribute(String attributeName, int defaultValue) {
            Integer intAttribute = (Integer) attributes.get(attributeName);
            return intAttribute != null ? intAttribute.intValue()
                    : defaultValue;
        }

        public String getAttribute(String attributeName, String defaultValue) {
            Object objVal = attributes.get(attributeName);
            return objVal != null ? objVal.toString() : defaultValue;
        }

        public boolean getAttribute(String attributeName, boolean defaultValue) {
            Boolean boolAttribute = (Boolean) attributes.get(attributeName);
            return boolAttribute != null ? boolAttribute.booleanValue()
                    : defaultValue;
        }

        public Map getAttributes() throws CoreException {
            return attributes;
        }

        public Object[] getAttributes(String[] attributeNames)
                throws CoreException {
            Collection<Object> values = new ArrayList<Object>();
            for (int i = 0; i < attributeNames.length; i++) {
                Object val = attributes.get(attributeNames[i]);
                if (val != null) {
                    values.add(val);
                }
            }
            return values.toArray(new Object[values.size()]);
        }

        public long getCreationTime() throws CoreException {
            return creationTime;
        }

        public long getId() {
            return creationTime;
        }

        public IResource getResource() {
            return resource;
        }

        public String getType() throws CoreException {
            return type;
        }

        public boolean isSubtypeOf(String superType) throws CoreException {
            return false;
        }

        public void setAttribute(String attributeName, int value)
                throws CoreException {
            attributes.put(attributeName, value);
        }

        public void setAttribute(String attributeName, Object value)
                throws CoreException {
            attributes.put(attributeName, value);
        }

        public void setAttribute(String attributeName, boolean value)
                throws CoreException {
            attributes.put(attributeName, value);
        }

        public void setAttributes(String[] attributeNames, Object[] values)
                throws CoreException {
        }

        public void setAttributes(Map attributes) throws CoreException {
            this.attributes = attributes;
        }

        public Object getAdapter(Class adapter) {
            return null;
        }
    }


}
