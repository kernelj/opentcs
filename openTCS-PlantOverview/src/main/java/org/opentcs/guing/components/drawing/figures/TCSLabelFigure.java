/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.drawing.figures;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import org.jhotdraw.draw.LabelFigure;
import org.jhotdraw.draw.event.FigureEvent;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.FigureComponent;

/**
 * Das Label zu einer PointFigure das den Namen des zugehörigen Objekts im
 * Kernel-Modell anzeigt.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class TCSLabelFigure
    extends LabelFigure {

  private Point2D.Double fOffset;
  private LabeledFigure fParent;
  private boolean isLabelVisible = true;

  /**
   *
   */
  public TCSLabelFigure() {
    this("?");
  }

  /**
   *
   * @param text
   */
  public TCSLabelFigure(String text) {
    super(text);
    fOffset = new Point2D.Double(-10.0, -20.0);
  }

  /**
   * Sets the visibility of the label.
   *
   * @param visible
   */
  public void setLabelVisible(boolean visible) {
    isLabelVisible = visible;

    if (visible) {
      setText(fParent.getPresentationFigure().getModel().getName());
    }
    else {
      setText("");
    }

    invalidate();
    validate();
  }

  /**
   * Position relativ zur Figur
   *
   * @param offset
   */
  public void setOffset(Point2D.Double offset) {
    fOffset = offset;
  }

  public Double getOffset() {
    return fOffset;
  }

  void setParent(LabeledFigure parent) {
    fParent = parent;
  }

  @Override  // AbstractFigure
  public void changed() {
    // Called when the figure has changed - movement with MouseDragger.
    super.changed();

    if (fParent != null) {
      TCSFigure figure = fParent.getPresentationFigure();
      FigureComponent model = figure.getModel();

      Point2D.Double newOffset = new Point2D.Double(
          getBounds().getX() - figure.getBounds().x,
          getBounds().getY() - figure.getBounds().y);

      if (newOffset.x != fOffset.x || newOffset.y != fOffset.y) {
        fOffset = newOffset;
        // Die Properties des Kernel-Objekts
        StringProperty sp = (StringProperty) model.getProperty(ElementPropKeys.POINT_LABEL_OFFSET_X);

        if (sp != null) {
          sp.setText(String.format("%d", (long) newOffset.x));
          sp.markChanged();
        }

        sp = (StringProperty) model.getProperty(ElementPropKeys.POINT_LABEL_OFFSET_Y);

        if (sp != null) {
          sp.setText(String.format("%d", (long) newOffset.y));
          sp.markChanged();
        }
        // TODO Point Label Orientation Angle!
//      sp = (StringProperty) model.getProperty(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);

//      if (sp != null) {
//        sp.setText(String.format("%d", 0));  
//        sp.markChanged();
//      }

        model.propertiesChanged(fParent);
      }
    }
  }

  @Override // AbstractFigure
  public int getLayer() {
    return 1; // stay above other figures ?
  }

  @Override  // LabelFigure
  public void figureChanged(FigureEvent event) {
    if (event.getFigure() instanceof LabeledFigure) {
      LabeledFigure lf = (LabeledFigure) event.getFigure();
      TCSFigure figure = lf.getPresentationFigure();
      FigureComponent model = figure.getModel();
      String name = model.getName();
      setText(name);

      if (isLabelVisible) {
        // Label neu zeichnen
        invalidate();
        validate();
      }
    }
  }
}
