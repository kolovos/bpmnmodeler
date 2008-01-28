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
 * Dec 04, 2006     hmalphettes         Created
 **/
package org.eclipse.stp.bpmn.figures;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.stp.bpmn.diagram.edit.parts.PoolEditPart;
import org.eclipse.stp.bpmn.figures.activities.LaneFigure;

/**
 * 
 * Overrides the default figure to paint horizontal lines at the level of
 * the border between 2 lanes figure.
 * 
 * @author hmalphettes
 * @author <a href="http://www.intalio.com">&copy; Intalio, Inc.</a>
 */
public class PoolPoolCompartmentFigure extends NotScrollableShapeCompartmentFigure {

    private Set<Integer> _coordsOnLastUpdate = new HashSet<Integer>();
    
    public PoolPoolCompartmentFigure(String title, IMapMode mm) {
        super(title, mm);
    }


    @Override
    public void paint(Graphics graphics) {
//        super.paint(graphics);
        graphics.pushState();
        graphics.setForegroundColor(PoolEditPart.BORDER);
        List<IFigure> figs = ((List<IFigure>)((List<IFigure>)
            ((List<IFigure>)((List<IFigure>)
              ((List<IFigure>)getChildren()).get(1)//animatablescrollpane
                .getChildren())).get(0)//freeformviewport
                    .getChildren())).get(0)//freeformlayer
                        .getChildren();
        Point theBottom = getBounds().getBottom();
        translateToAbsolute(theBottom);
        boolean doUpdateRestOfPool = false;
        Set<Integer> newCoordsOnLastUpdate = new HashSet<Integer>();
        for (IFigure fig : figs) {
            if (fig instanceof LaneFigure) {
                Point bottom = fig.getBounds().getBottom();
                fig.translateToAbsolute(bottom);
                //don't draw if you are the last bottom line.
                if (bottom.y + 35 < theBottom.y) {
                    this.translateToRelative(bottom);
                    graphics.drawLine(getBounds().x, bottom.y,
                        getBounds().x + getBounds().width, bottom.y);
                    newCoordsOnLastUpdate.add(bottom.y);
                    if (!doUpdateRestOfPool 
                            && !_coordsOnLastUpdate.contains(bottom.y)) {
                        doUpdateRestOfPool = true;
                    }
                }
            }
        }
        _coordsOnLastUpdate = newCoordsOnLastUpdate;
        graphics.popState();
        if (doUpdateRestOfPool) {
            this.getUpdateManager().addDirtyRegion(this,
                    getBounds().x, getBounds().y, getBounds().width, getBounds().height);
        }
        super.paint(graphics);
    }
    
}