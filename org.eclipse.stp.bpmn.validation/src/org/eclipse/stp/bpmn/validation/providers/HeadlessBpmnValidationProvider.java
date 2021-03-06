/*
 *******************************************************************************
 ** Copyright (c) 2006, Intalio Inc.
 ** All rights reserved. This program and the accompanying materials
 ** are made available under the terms of the Eclipse Public License v1.0
 ** which accompanies this distribution, and is available at
 ** http://www.eclipse.org/legal/epl-v10.html
 ** 
 ** Contributors:
 **     Intalio Inc. - initial API and implementation
 ********************************************************************************
 */
package org.eclipse.stp.bpmn.validation.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.validation.model.EvaluationMode;
import org.eclipse.emf.validation.model.IConstraintStatus;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.stp.bpmn.TextAnnotation;
import org.eclipse.stp.bpmn.diagram.edit.parts.TextAnnotation2EditPart;
import org.eclipse.stp.bpmn.diagram.edit.parts.TextAnnotationEditPart;
import org.eclipse.stp.bpmn.diagram.part.BpmnVisualIDRegistry;
import org.eclipse.stp.bpmn.validation.BpmnValidationMessages;
import org.eclipse.stp.bpmn.validation.BpmnValidationPlugin;
import org.eclipse.stp.bpmn.validation.IConstraintStatusEx;
import org.eclipse.stp.bpmn.validation.IValidationMarkerCreationHook;
import org.eclipse.stp.bpmn.validation.builder.ValidationMarkerCustomAttributes;

/**
 * Identical to BpmnValidationProvider.runValidation
 * except that it can be executed in a headless environment as it
 * does not depend on gmf.common.ui.
 */
@SuppressWarnings("unchecked")
public class HeadlessBpmnValidationProvider {

	//duplicate of the constant defined in gmf.common.ui
	//org.eclipse.gmf.runtime.common.ui.resources.IMarker.ELEMENT_ID;
	//this way we avoid having to load that plugin.
	public static String ELEMENT_ID = "elementId";//$NON-NLS-1$

    /**
     * @generated
     */
    public static final String MARKER_TYPE =
        BpmnValidationPlugin.PLUGIN_ID + ".diagnostic"; //$NON-NLS-1$
    /**
     * @generated
     */
    public static final String MARKER_TASK_TYPE =
        "org.eclipse.stp.bpmn.diagram" + ".taskmarker"; //$NON-NLS-1$ //$NON-NLS-2$
	/**
	 * @generated
	 */
	static boolean constraintsActive = false;

	
	/**
	 * @generated
	 */
	public static void runValidation(View view) {
		final View target = view;
		Runnable task = new Runnable() {
			public void run() {
				try {
					constraintsActive = true;
					validate(target);
                } catch (Throwable t) {
                	t.printStackTrace();
                    BpmnValidationPlugin.getDefault().getLog().log(
                            new Status(IStatus.ERROR, 
                                    BpmnValidationPlugin.PLUGIN_ID,
                                    IStatus.ERROR, 
                                    t.getMessage(), 
                                    t));
				} finally {
					constraintsActive = false;
				}
			}
		};
		TransactionalEditingDomain txDomain = TransactionUtil
				.getEditingDomain(target);
		if (txDomain != null) {
			try {
				txDomain.runExclusive(task);
			} catch (Exception e) {
                BpmnValidationPlugin.getDefault().getLog().log(
                        new Status(IStatus.ERROR,
                                BpmnValidationPlugin.PLUGIN_ID,
                                IStatus.ERROR,
                                BpmnValidationMessages.BpmnValidationProvider_validateActionFailed, e));
			}
		} else {
			task.run();
		}
	}

	
	/**
	 * @notgenerated
	 * now taking care of the code
	 */
	private static void validate(View target) {
		IFile diagramFile = (target.eResource() != null) ? WorkspaceSynchronizer
				.getFile(target.eResource())
				: null;
		try {
			if (diagramFile != null)
				diagramFile.deleteMarkers(MARKER_TYPE, false,
						IResource.DEPTH_ZERO);
			    diagramFile.deleteMarkers(MARKER_TASK_TYPE, false,
                    IResource.DEPTH_ZERO);
		} catch (CoreException e) {
            BpmnValidationPlugin.getDefault().getLog().log(
                    new Status(IStatus.ERROR,
                            BpmnValidationPlugin.PLUGIN_ID,
                            IStatus.ERROR,
                            BpmnValidationMessages.BpmnValidationProvider_validateFailed, e));
		}
		
        TreeIterator<EObject> iter = target.eResource().getAllContents();
        while (iter.hasNext()) {
            EObject eobj = iter.next();
            if ((eobj instanceof View)) {
                String id = (((View) eobj).getType());
                if ((BpmnVisualIDRegistry.getType(TextAnnotationEditPart.VISUAL_ID).equals(id) ||
                        BpmnVisualIDRegistry.getType(TextAnnotation2EditPart.VISUAL_ID).equals(id)) &&
                        ((View) eobj).getElement() instanceof TextAnnotation) {
                    //support for generating task markers just like the java editor.
                    String name = ((TextAnnotation) ((View) eobj).getElement()).getName();
                    if (name != null) {
                        int taskPriority = -1;
                        if (name.indexOf("TODO") != -1 || name.indexOf("XXX") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
                            taskPriority = IMarker.PRIORITY_NORMAL;
                        } else if (name.indexOf("FIXME") != -1) { //$NON-NLS-1$
                            taskPriority = IMarker.PRIORITY_HIGH;
                        }
                        if (taskPriority != -1) {
                            try {
                                IMarker marker = diagramFile.createMarker(MARKER_TASK_TYPE);//IMarker.TASK);
                                String location = EMFCoreUtil.getQualifiedName(eobj, true);
                                if (location.startsWith("<Diagram>::")) { //$NON-NLS-1$
                                    location = location.substring("<Diagram>::".length()); //$NON-NLS-1$
                                }
                                marker.setAttribute(IMarker.LOCATION, location);
                                marker.setAttribute(
                                        org.eclipse.gmf.runtime.common.core.resources.IMarker.ELEMENT_ID, 
                                        ViewUtil.getIdStr((View) eobj));
                                //just like in the jdt we don't remove the TODO FIXME XXX...
                                marker.setAttribute(IMarker.MESSAGE, name.trim());
                                marker.setAttribute(IMarker.PRIORITY, taskPriority);
                                marker.setAttribute(IMarker.USER_EDITABLE, false);
                                Collection<IValidationMarkerCreationHook> hooks =
                                    BpmnValidationPlugin.getDefault().getCreationMarkerCallBacks();
                                if (hooks != null) {
                                    for (IValidationMarkerCreationHook hook : hooks) {
                                        hook.validationMarkerCreated(marker, eobj);
                                    }
                                }
                            } catch (CoreException e) {
                                e.printStackTrace();
                            }
                        }
                        }
                }
            }
        }
		Diagnostic diagnostic = runEMFValidator(target);

		IBatchValidator validator = (IBatchValidator) ModelValidationService
				.getInstance().newValidator(EvaluationMode.BATCH);
		validator.setIncludeLiveConstraints(true);
		IStatus status = Status.OK_STATUS;
		if (target.isSetElement() && target.getElement() != null) {
			status = validator.validate(target.getElement());
		}
		List<IStatus> allStatuses = new ArrayList<IStatus>();
		if (status.isMultiStatus()) {
		    if (status instanceof IConstraintStatusEx) {
		        //sometimes, a single status is retuned
		        //but itself should be interpreted as an error.
		        //instead of just a wrapper for a list of errors.
		        allStatuses.add(status);
		    } else {
			    for (IStatus st : status.getChildren()) {
			        allStatuses.add(st);
			    }
		    }
		} else {
		    allStatuses.add(status);
		}

		HashSet<EObject> targets = new HashSet<EObject>();
		for (Iterator it = diagnostic.getChildren().iterator(); it.hasNext();) {
			targets.add(getDiagnosticTarget((Diagnostic) it.next()));
		}
		
		//also add the children statuses:
		//if a single validation constraint actually created multiple statuses
		//by taking advantage of IConstraintStatusEx addChild API.
		List<IStatus> moreStatuses = new ArrayList<IStatus>();
		for (Iterator<IStatus> it = allStatuses.iterator(); it.hasNext();) {
		    IStatus nextStatus = it.next();
			if (nextStatus instanceof IConstraintStatus) {
				targets.add(((IConstraintStatus) nextStatus).getTarget());
				if (((IConstraintStatus) nextStatus).isMultiStatus()) {
				    for (IStatus child : ((IConstraintStatus)nextStatus).getChildren()) {
				        if (child instanceof IConstraintStatus) {
		                    targets.add(((IConstraintStatus) child).getTarget());
		                    moreStatuses.add((IConstraintStatus)child);
				        }
				    }
				}
			}
		}
		allStatuses.addAll(moreStatuses);

		Map viewMap = buildElement2ViewMap(target, targets);
		for (Iterator it = diagnostic.getChildren().iterator(); it
				.hasNext();) {
			Diagnostic nextDiagnostic = (Diagnostic) it.next();
			List data = nextDiagnostic.getData();
			if (!data.isEmpty() && data.get(0) instanceof EObject) {
				EObject element = (EObject) data.get(0);
				View view = findTargetView(element, viewMap);
				if (diagramFile != null)
					addMarker(diagramFile, view != null ? view : target,
							element, nextDiagnostic.getMessage(),
							diagnosticToStatusSeverity(nextDiagnostic.getSeverity()),
							nextDiagnostic.getCode(),
							getCustomMarkerAttributes(nextDiagnostic));
			}
		}

		for (Iterator it = allStatuses.iterator(); it.hasNext();) {
			Object nextStatusObj = it.next();
			if (nextStatusObj instanceof IConstraintStatus) {
				IConstraintStatus nextStatus = (IConstraintStatus) nextStatusObj;
				View view = findTargetView(nextStatus.getTarget(), viewMap);
				if (diagramFile != null)
					addMarker(diagramFile, view != null ? view : target,
							nextStatus.getTarget(),
							nextStatus.getMessage(), 
							nextStatus.getSeverity(),
							nextStatus.getCode(),
							getCustomMarkerAttributes(nextStatus));
			}
		}
	}

	/**
	 * @generated
	 */
	static View findTargetView(EObject targetElement, Map viewMap) {
		if (targetElement instanceof View) {
			return (View) targetElement;
		}
		for (EObject container = targetElement; container != null; container = container
				.eContainer()) {
			if (viewMap.containsKey(container))
				return (View) viewMap.get(container);
		}
		return null;
	}

	/**
	 * @generated
	 */
	static Map<?,?> buildElement2ViewMap(View view, Set<?> targets) {
		Map map = new HashMap();
		getElement2ViewMap(view, map, targets);
		if (!targets.isEmpty()) {
			Set<EObject> path = new HashSet<EObject>();
			for (Iterator<?> it = targets.iterator(); it.hasNext();) {
				EObject nextNotMapped = (EObject) it.next();
				for (EObject container = nextNotMapped.eContainer(); container != null; container = container
						.eContainer()) {
					if (!map.containsKey(container)) {
						path.add(container);
					} else
						break;
				}
			}
			getElement2ViewMap(view, map, path);
		}
		return map;
	}

	/**
	 * @generated
	 */
	static void getElement2ViewMap(View view, Map map, Set targets) {
		if (!map.containsKey(view.getElement())
				&& targets.remove(view.getElement())) {
			map.put(view.getElement(), view);
		}
		for (Iterator it = view.getChildren().iterator(); it.hasNext();) {
			getElement2ViewMap((View) it.next(), map, targets);
		}
		if (view instanceof Diagram) {
			for (Iterator it = ((Diagram) view).getEdges().iterator(); it
					.hasNext();) {
				getElement2ViewMap((View) it.next(), map, targets);
			}
		}
	}
	
    static Map<String,Object> getCustomMarkerAttributes(Diagnostic diagnostic) {
        return ValidationMarkerCustomAttributes.getMarkerAttributesMap(diagnostic);
    }
    static Map<String,Object> getCustomMarkerAttributes(IConstraintStatus constrainStatus) {
        return constrainStatus instanceof IConstraintStatusEx
            ? ((IConstraintStatusEx)constrainStatus).getMarkerCustomAttributes() : null;
    }

	/**
	 * @notgenerated
	 * adding a code to keep track of the error.
	 */
	static void addMarker(IFile file, View view, EObject element,
			String message, int statusSeverity, int code, Map<String,Object> customAttributes) {
		try {
            IMarker marker = file.createMarker(MARKER_TYPE);
            
            if (customAttributes == null) {
                customAttributes = new HashMap<String, Object>();
            }
            
            customAttributes.put(IMarker.MESSAGE, message);
			
			String location = EMFCoreUtil.getQualifiedName(element, true);
			if (location.startsWith("<Diagram>::")) { //$NON-NLS-1$
			    location = location.substring("<Diagram>::".length()); //$NON-NLS-1$
			}
			customAttributes.put(IMarker.LOCATION, location);
			
			customAttributes.put(
							ELEMENT_ID,
							ViewUtil.getIdStr(view));
			int markerSeverity = IMarker.SEVERITY_INFO;
			if (statusSeverity == IStatus.WARNING) {
				markerSeverity = IMarker.SEVERITY_WARNING;
			} else if (statusSeverity == IStatus.ERROR
					|| statusSeverity == IStatus.CANCEL) {
				markerSeverity = IMarker.SEVERITY_ERROR;
			}
			customAttributes.put(IMarker.SEVERITY, markerSeverity);
			customAttributes.put("code", code); //$NON-NLS-1$
			
			marker.setAttributes(customAttributes);
			
			if (BpmnValidationPlugin.getDefault() != null) {
				Collection<IValidationMarkerCreationHook> hooks =
				    BpmnValidationPlugin.getDefault().getCreationMarkerCallBacks();
				if (hooks != null) {
				    for (IValidationMarkerCreationHook hook : hooks) {
				        hook.validationMarkerCreated(marker, element);
				    }
				}
			}
		} catch (CoreException e) {
			if (BpmnValidationPlugin.getDefault() != null) {
				BpmnValidationPlugin.getDefault().getLog().log(
                    new Status(IStatus.ERROR,
                            BpmnValidationPlugin.PLUGIN_ID,
                            IStatus.ERROR,
                            BpmnValidationMessages.BpmnValidationProvider_markerCreationFailed, e));
			}
		}
	}

	/**
	 * @generated
	 */
	static EObject getDiagnosticTarget(Diagnostic diagnostic) {
		if (!diagnostic.getData().isEmpty()) {
			Object target = diagnostic.getData().get(0);
			return target instanceof EObject ? (EObject) target : null;
		}
		return null;
	}

	/**
	 * @generated
	 */
	static int diagnosticToStatusSeverity(int diagnosticSeverity) {
		if (diagnosticSeverity == Diagnostic.OK) {
			return IStatus.OK;
		} else if (diagnosticSeverity == Diagnostic.INFO) {
			return IStatus.INFO;
		} else if (diagnosticSeverity == Diagnostic.WARNING) {
			return IStatus.WARNING;
		} else if (diagnosticSeverity == Diagnostic.ERROR
				|| diagnosticSeverity == Diagnostic.CANCEL) {
			return IStatus.ERROR;
		}
		return IStatus.INFO;
	}

	/**
	 * @generated
	 */
	static Diagnostic runEMFValidator(View target) {
		if (target.isSetElement() && target.getElement() != null) {
			return new Diagnostician() {
				public String getObjectLabel(EObject eObject) {
                    try {
                        return EMFCoreUtil.getQualifiedName(eObject, true);
                    } catch (Exception e) {
                        return "<" + eObject.eClass().getName() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
				}
			}.validate(target.getElement());
		}
		return Diagnostic.OK_INSTANCE;
	}
}
