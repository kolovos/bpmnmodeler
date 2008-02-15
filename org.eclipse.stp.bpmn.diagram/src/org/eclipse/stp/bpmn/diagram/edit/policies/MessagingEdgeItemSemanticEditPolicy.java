/*
 * Copyright (c) 2007, Intalio Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intalio Inc. - initial API and implementation
 */
package org.eclipse.stp.bpmn.diagram.edit.policies;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.core.util.CrossReferenceAdapter;
import org.eclipse.gmf.runtime.emf.type.core.commands.DestroyElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.stp.bpmn.BpmnPackage;

/**
 * @generated
 */
public class MessagingEdgeItemSemanticEditPolicy extends
        BpmnBaseItemSemanticEditPolicy {

    /**
     * @notgenerated
     */
    protected Command getDestroyElementCommand(DestroyElementRequest req) {
        return getMSLWrapper(new DestroyElementCommand(req) {
            /* (non-Javadoc)
             * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doExecuteWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
             */
            protected CommandResult doExecuteWithResult(
                    IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {
                CommandResult cr = super.doExecuteWithResult(monitor, info);
                return cr;
            }
            
            /**
             * Override the tearIncoming so we force the removal of 
             * incomingMessages and outgoingMessages.
             * Even though they are derived from orderedMessages, the feature
             * orderedMessages is itself never removed.
             * 
             * Tears down references to the object that we are destroying, from all other
             * objects in the resource set.
             * 
             * @param destructee the object being destroyed
             */
            protected void tearDownIncomingReferences(EObject destructee) {
                CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
                    .getExistingCrossReferenceAdapter(destructee);
                if (crossReferencer != null) {
                    Collection inverseReferences = crossReferencer
                        .getInverseReferences(destructee);
                    if (inverseReferences != null) {
                        int size = inverseReferences.size();
                        if (size > 0) {
                            Setting setting;
                            EReference eRef;
                            Setting[] settings = (Setting[]) inverseReferences
                                .toArray(new Setting[size]);
                            for (int i = 0; i < size; ++i) {
                                setting = settings[i];
                                eRef = (EReference) setting.getEStructuralFeature();
                                if (//so we must remove the references for
                                    //outgoing and incoming on the source and 
                                    //target.
                                    //but the normal code would not because both features
                                    //are derived. and the only feature that is not derived 
                                    //is the orderedMessage... which is never removed
                                    //directly.
                        (eRef.isDerived() && 
                                (BpmnPackage.MESSAGE_VERTEX__OUTGOING_MESSAGES == eRef.getFeatureID()
                                        || BpmnPackage.MESSAGE_VERTEX__INCOMING_MESSAGES == eRef.getFeatureID()/*
                                        || BpmnPackage.ACTIVITY__ORDERED_MESSAGES == eRef.getFeatureID()*/)
                                || (eRef.isChangeable() && !eRef.isDerived() &&
                                        !eRef.isContainment() && 
                                        !eRef.isContainer()))) {
                                    EcoreUtil.remove(setting.getEObject(), eRef, destructee);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    
}
