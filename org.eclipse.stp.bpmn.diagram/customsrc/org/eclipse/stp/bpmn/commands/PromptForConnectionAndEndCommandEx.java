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
 * Date             Author              Changes
 * Nov 28, 2006     MPeleshchsyhyn         Created
 **/
package org.eclipse.stp.bpmn.commands;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gmf.runtime.diagram.ui.commands.PromptForConnectionAndEndCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.internal.commands.ElementTypeLabelProvider;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.IMetamodelType;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.stp.bpmn.diagram.BpmnDiagramMessages;
import org.eclipse.stp.bpmn.diagram.edit.parts.PoolEditPart;
import org.eclipse.stp.bpmn.diagram.edit.parts.PoolPoolCompartmentEditPart;
import org.eclipse.stp.bpmn.diagram.edit.parts.SubProcessSubProcessBodyCompartmentEditPart;
import org.eclipse.stp.bpmn.diagram.part.BpmnDiagramEditorPlugin;
import org.eclipse.stp.bpmn.diagram.providers.BpmnElementTypes;
import org.eclipse.swt.graphics.Image;

/**
 * Makes sure that the message connections are between pools.
 * Make sure that the sequence connections are in the same sub-process or pool.
 * 
 * @author MPeleshchyshyn
 * @author hmalphettes
 * @author <a href="http://www.intalio.com">&copy; Intalio, Inc.</a>
 */
public class PromptForConnectionAndEndCommandEx extends
    PromptForConnectionAndEndCommand {

    private static Image EXISTING = null;
    
    protected class ConnectionAndEndLabelProviderEx 
        extends ElementTypeLabelProvider {

        @Override
        public void dispose() {
            super.dispose();
            if (EXISTING != null) {
                EXISTING.dispose();
                EXISTING = null;
            }
        }
        
        /** the known connection item */
        private Object connectionItem;
        
        protected ConnectionAndEndLabelProviderEx(Object connectionItem) {
            this.connectionItem = connectionItem;
        }
        
        @Override
        public Image getImage(Object object) {
            if (EXISTING_ELEMENT.equals(object)) {
                if (EXISTING != null && !EXISTING.isDisposed()) {
                    return EXISTING;
                }
                EXISTING = BpmnDiagramEditorPlugin.
                    getBundledImageDescriptor("icons/obj24/existingElement.png"). //$NON-NLS-1$
                    createImage();
                return EXISTING;
            }
            return super.getImage(object);
        }
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element) {
            if (element instanceof String) {
                if (EXISTING_ELEMENT.equals(element)) {
                    return BpmnDiagramMessages.PromptForConnectionAndEndCommandEx_ConnectToExisting;
                }
            }
            String theInputStr = null;
            if (element instanceof IElementType) {
                if (isDirectionReversed())
                    theInputStr = BpmnDiagramMessages.PromptForConnectionAndEndCommandEx_ConnectFromNew;
                else
                    theInputStr = BpmnDiagramMessages.PromptForConnectionAndEndCommandEx_ConnectToNew;
                String text = NLS.bind(theInputStr, new Object[] {
                    super.getText(element)});
                return text;
            } else {
                if (isDirectionReversed())
                    theInputStr = BpmnDiagramMessages.PromptForConnectionAndEndCommandEx_ConnectFrom;
                else
                    theInputStr = BpmnDiagramMessages.PromptForConnectionAndEndCommandEx_ConnectTo;
                String text = NLS.bind(theInputStr, new Object[] {
                    super.getText(element)});
                return text;
            }
        }
        
        /**
         * Gets the connection item.
         * 
         * @return the connection item
         */
        protected Object getConnectionItem() {
            return connectionItem;
        }
    }
	/**
	 * taken from superclass.
	 * This can be added to the content list to add a 'select existing' option.
	 */
	protected static String EXISTING_ELEMENT = DiagramUIMessages.ConnectionHandle_Popup_ExistingElement;
	
    private CreateConnectionRequest request;
    private IGraphicalEditPart _containerEP;

//    /** Adapts to the connection type result. */
//	private ObjectAdapter connectionAdapter = new ObjectAdapter();
//
//	/** Adapts to the other end type result. */
//	private ObjectAdapter endAdapter = new ObjectAdapter();
	
    public PromptForConnectionAndEndCommandEx(CreateConnectionRequest request,
            IGraphicalEditPart containerEP) {
        super(request, containerEP);
        this.request = request;
        _containerEP = containerEP;
    }

    @Override
    protected List getConnectionMenuContent() {
        List l = super.getConnectionMenuContent();
        
        EditPart source = request.getSourceEditPart();
        EditPart target = request.getTargetEditPart();
        EditPart sPool = getPool(source);
        EditPart tPool = getPool(target);
        
        EditPart sContainer = getContainer(source.getParent());
        
        // filter the possible connections
        if (tPool != null && sPool.equals(tPool)) {
            l.remove(BpmnElementTypes.MessagingEdge_3002);
        }
        if (tPool != null && 
                (sContainer != _containerEP || !sPool.equals(tPool))) {
            l.remove(BpmnElementTypes.SequenceEdge_3001);
        }
        return l;
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gmf.runtime.diagram.ui.commands.PromptForConnectionAndEndCommand#getEndMenuContent(java.lang.Object)
     */
    @Override
    protected List getEndMenuContent(Object connectionItem) {
        List l = super.getEndMenuContent(connectionItem);
        if ((IMetamodelType) connectionItem == BpmnElementTypes.Association_3003) {
        	// remove the possibility to connect to an existing artifact for now.
        	l.remove(EXISTING_ELEMENT);
            return l;
        }
        EditPart source = request.getSourceEditPart();
        EditPart target = request.getTargetEditPart();
        EditPart sPool = getPool(source);
        EditPart tPool = getPool(target);
        
        EditPart sContainer = getContainer(source.getParent());
        if (!(connectionItem instanceof IMetamodelType)) {
        	return l;
        }
        if ((IMetamodelType) connectionItem == BpmnElementTypes.MessagingEdge_3002) {
            if (tPool != null && sPool.equals(tPool)) {
//                Object lastElement = l.get(l.size() - 1);
                l.clear();
//                l.add(lastElement);
            }
        } else if ((IMetamodelType) connectionItem == BpmnElementTypes.SequenceEdge_3001) {
            if (tPool != null && 
            		(sContainer != _containerEP || !sPool.equals(tPool))) {
//                Object lastElement = l.get(l.size() - 1);
                l.clear();
//                l.add(lastElement);
            }
        }
        return l;
    }

    /**
     * Returns parent pool for the specified edit part or edit part itself in
     * case if specified edit part is a pool edit part
     * 
     * @param editPart
     *            the edit part
     * @return parent pool for the specified edit part or edit part itself in
     *         case if specified edit part is a pool edit part
     */
    protected EditPart getPool(EditPart editPart) {
        if (editPart instanceof PoolEditPart) {
            return editPart;
        }
        if (editPart == null) {
        	return null;
        }
        EditPart parent = editPart.getParent();

        while (!(parent instanceof PoolEditPart) && parent != null) {
            parent = parent.getParent();
        }

        return parent;
    }
    /**
     * Returns the container compartment edit part in which the passed
     * edit part should be considered enclosed as far as the sequence
     * connection is concerned.
     * <p>
     * If the edit part is an activity or a sub-process, it matches the edit 
     * part of the graph that contains it.
     * </p>
     * <p>
     * If the edit part is a boundary event activity, the parent container
     * is not the sub-process that carries the event but the container of that
     * sub-process. This way it is allowed to have a connection between a
     * event boundary and the activity to execute when the event is triggered.
     * </p>
     * @param editPart
     *            the edit part
     * @return parent pool for the specified edit part or edit part itself in
     *         case if specified edit part is a pool edit part
     */
    protected EditPart getContainer(EditPart editPart) {
        if (editPart instanceof PoolPoolCompartmentEditPart || 
                editPart instanceof SubProcessSubProcessBodyCompartmentEditPart) {
            return editPart;
        }
        
        EditPart parent = editPart.getParent();
        if (parent == null) {
            return null;//humf!
        }
        
        return getContainer(parent);
    }
    
    /**
     * overriden to return shorter messages
     */
    protected ILabelProvider getConnectionAndEndLabelProvider(
            Object connectionItem) {
        return new ConnectionAndEndLabelProviderEx(connectionItem);
    }
//    @Override
//    public boolean canExecute() {
//    	return createPopupBalloon() != null;
//    }
    
//    /**
//     * New method that is going to generate a balloon instead of the popup menu.
//     * @return a PopupBalloon object or null
//     */
//    protected PopupBalloon createPopupBalloon() {
//    	final List connectionMenuContent = getConnectionMenuContent();
//		if (connectionMenuContent == null || connectionMenuContent.isEmpty()) {
//			return null;
//		} else {
//			PopupBalloon balloon = new PopupBalloon(_containerEP);
//			for (Iterator iter = connectionMenuContent.iterator(); iter.hasNext();) {
//				Object connectionItem = iter.next();
//				for (Object subItem : getEndMenuContent(connectionItem)) {
//					IElementType item = (IElementType) subItem;
//					PassivePopupBarTool theTracker =
//						balloon.new PassivePopupBarTool(balloon, _containerEP, item);
//					balloon.addPopupBarDescriptor(item, theTracker);
//				}
//			}
//			if ((balloon.hasPopupBarDescriptors())) {
//				return balloon;
//			}
//		}
//		return null;
//    }
    
//    @Override
//    protected CommandResult doExecuteWithResult(
//    		IProgressMonitor progressMonitor, IAdaptable info)
//    		throws ExecutionException {
//    	PopupBalloon balloon = createPopupBalloon();
//
//		if (balloon == null) {
//			return CommandResult.newErrorCommandResult(getLabel());
//		}
//
//		balloon.showBalloonExclusive(request.getLocation(), getParentShell());
//		
//		ICommand com = (ICommand) balloon.getCommand();
//		
//		com.execute(progressMonitor, info);
//		CommandResult cmdResult = com.getCommandResult();
//		if (cmdResult.getStatus().getSeverity() != IStatus.OK) {
//			return cmdResult;
//		}
//		Object result = cmdResult.getReturnValue();
//		if (result instanceof List) {
//			List resultList = (List) result;
//			if (resultList.size() == 2) {
//				connectionAdapter.setObject(resultList.get(0));
//
//				Object targetResult = resultList.get(1);
//
//				if (targetResult.equals(EXISTING_ELEMENT)) {
//					targetResult = isDirectionReversed() ? ModelingAssistantService
//						.getInstance().selectExistingElementForSource(
//							getKnownEnd(), (IElementType) resultList.get(0))
//						: ModelingAssistantService.getInstance()
//							.selectExistingElementForTarget(getKnownEnd(),
//								(IElementType) resultList.get(0));
//					if (targetResult == null) {
//						return CommandResult.newCancelledCommandResult();
//					}
//				}
//				endAdapter.setObject(targetResult);
//				return CommandResult.newOKCommandResult();
//			}
//		}
//		return CommandResult.newErrorCommandResult(getLabel());
//    }
    
//    /**
//	 * Gets the known end, which even in the case of a reversed
//	 * <code>CreateUnspecifiedTypeConnectionRequest</code>, is the source
//	 * editpart.
//	 * 
//	 * @return the known end
//	 */
//	private EditPart getKnownEnd() {
//		return request.getSourceEditPart();
//	}
//	
//	/**
//	 * This can be added to the content list to add a 'select existing' option.
//	 */
//	private static String EXISTING_ELEMENT = DiagramUIMessages.ConnectionHandle_Popup_ExistingElement;
}
