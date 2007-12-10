/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.itmill.toolkit.terminal.gwt.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
import com.itmill.toolkit.terminal.gwt.client.Caption;
import com.itmill.toolkit.terminal.gwt.client.Container;
import com.itmill.toolkit.terminal.gwt.client.Paintable;
import com.itmill.toolkit.terminal.gwt.client.StyleConstants;
import com.itmill.toolkit.terminal.gwt.client.UIDL;
import com.itmill.toolkit.terminal.gwt.client.Util;

/**
 * Abstract base class for ordered layouts. Use either vertical or horizontal
 * subclass.
 * 
 * @author IT Mill Ltd
 */
public abstract class IOrderedLayout extends ComplexPanel implements Container {

    public static final String CLASSNAME = "i-orderedlayout";

    public static final int ORIENTATION_VERTICAL = 0;
    public static final int ORIENTATION_HORIZONTAL = 1;

    int orientationMode = ORIENTATION_VERTICAL;

    protected HashMap componentToCaption = new HashMap();

    protected ApplicationConnection client;

    /**
     * Contains reference to Element where Paintables are wrapped. Normally a TR
     * or a TBODY element.
     */
    protected Element childContainer;

    /*
     * Elements that provides the Layout interface implementation.
     */
    protected Element size;
    protected Element margin;

    protected Element topMargin = null;
    protected Element bottomMargin = null;

    private static final String structure = "<div><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tbody></tbody></table></div>";

    public IOrderedLayout(int orientation) {
        orientationMode = orientation;
        constructDOM();
        setStyleName(CLASSNAME);
    }

    protected void constructDOM() {
        size = DOM.createDiv();
        DOM.setInnerHTML(size, structure);
        margin = DOM.getFirstChild(size);
        final Element tBody = DOM.getFirstChild(DOM.getFirstChild(margin));
        if (orientationMode == ORIENTATION_HORIZONTAL) {
            childContainer = DOM.createTR();
            DOM.appendChild(tBody, childContainer);
        } else {
            childContainer = tBody;
        }
        setElement(size);
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        this.client = client;

        // Ensure correct implementation
        if (client.updateComponent(this, uidl, false)) {
            return;
        }

        // Set size
        // TODO move these to own methods (override setWidth & setHeight)
        if (uidl.hasAttribute("width")) {
            setWidth(uidl.getStringAttribute("width"));
            DOM.setStyleAttribute(DOM.getFirstChild(margin), "width", "100%");
            DOM.setStyleAttribute(getElement(), "tableLayout", "fixed");
        } else {
            setWidth("");
            DOM.setStyleAttribute(DOM.getFirstChild(margin), "width", "");
            DOM.setStyleAttribute(getElement(), "tableLayout", "");
        }
        if (uidl.hasAttribute("height")) {
            setHeight(uidl.getStringAttribute("height"));
            DOM.setStyleAttribute(margin, "height", "100%");
            DOM.setStyleAttribute(DOM.getFirstChild(margin), "height", "100%");
            DOM.setStyleAttribute(getElement(), "tableLayout", "fixed");
        } else {
            setHeight("");
            DOM.setStyleAttribute(margin, "height", "");
            DOM.setStyleAttribute(DOM.getFirstChild(margin), "height", "");
            DOM.setStyleAttribute(getElement(), "tableLayout", "");
        }

        // Update contained components

        final ArrayList uidlWidgets = new ArrayList();
        for (final Iterator it = uidl.getChildIterator(); it.hasNext();) {
            final UIDL uidlForChild = (UIDL) it.next();
            final Paintable child = client.getPaintable(uidlForChild);
            uidlWidgets.add(child);
        }

        final ArrayList oldWidgets = getPaintables();

        final Iterator oldIt = oldWidgets.iterator();
        final Iterator newIt = uidlWidgets.iterator();
        final Iterator newUidl = uidl.getChildIterator();

        Widget oldChild = null;
        while (newIt.hasNext()) {
            final Widget child = (Widget) newIt.next();
            final UIDL childUidl = (UIDL) newUidl.next();

            if (oldChild == null && oldIt.hasNext()) {
                // search for next old Paintable which still exists in layout
                // and delete others
                while (oldIt.hasNext()) {
                    oldChild = (Widget) oldIt.next();
                    // now oldChild is an instance of Paintable
                    if (uidlWidgets.contains(oldChild)) {
                        break;
                    } else {
                        removePaintable((Paintable) oldChild);
                        oldChild = null;
                    }
                }
            }
            if (oldChild == null) {
                // we are adding components to layout
                add(child);
            } else if (child == oldChild) {
                // child already attached and updated
                oldChild = null;
            } else if (hasChildComponent(child)) {
                // current child has been moved, re-insert before current
                // oldChild
                // TODO this might be optimized by moving only container element
                // to correct position
                removeCaption(child);
                int index = getWidgetIndex(oldChild);
                if (componentToCaption.containsKey(oldChild)) {
                    index--;
                }
                remove(child);
                this.insert(child, index);
            } else {
                // insert new child before old one
                final int index = getWidgetIndex(oldChild);
                insert(child, index);
            }
            ((Paintable) child).updateFromUIDL(childUidl, client);
        }
        // remove possibly remaining old Paintable object which were not updated
        while (oldIt.hasNext()) {
            oldChild = (Widget) oldIt.next();
            final Paintable p = (Paintable) oldChild;
            if (!uidlWidgets.contains(p)) {
                removePaintable(p);
            }
        }

        // Handle component alignments
        handleAlignments(uidl);

        // Handle layout margins
        handleMargins(uidl);

    }

    /**
     * Retuns a list of Paintables currently rendered in layout
     * 
     * @return list of Paintable objects
     */
    protected ArrayList getPaintables() {
        final ArrayList al = new ArrayList();
        final Iterator it = iterator();
        while (it.hasNext()) {
            final Widget w = (Widget) it.next();
            if (w instanceof Paintable) {
                al.add(w);
            }
        }
        return al;
    }

    /**
     * Removes Paintable from DOM and its reference from ApplicationConnection.
     * 
     * Also removes Paintable's Caption if one exists
     * 
     * @param p
     *                Paintable to be removed
     */
    public boolean removePaintable(Paintable p) {
        final Caption c = (Caption) componentToCaption.get(p);
        if (c != null) {
            componentToCaption.remove(c);
            remove(c);
        }
        client.unregisterPaintable(p);
        return remove((Widget) p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.itmill.toolkit.terminal.gwt.client.Layout#replaceChildComponent(com.google.gwt.user.client.ui.Widget,
     *      com.google.gwt.user.client.ui.Widget)
     */
    public void replaceChildComponent(Widget from, Widget to) {
        client.unregisterPaintable((Paintable) from);
        final Caption c = (Caption) componentToCaption.get(from);
        if (c != null) {
            remove(c);
            componentToCaption.remove(c);
        }
        final int index = getWidgetIndex(from);
        if (index >= 0) {
            remove(index);
            insert(to, index);
        }
    }

    protected void insert(Widget w, int beforeIndex) {
        if (w instanceof Caption) {
            final Caption c = (Caption) w;
            // captions go into same container element as their
            // owners
            final Element container = DOM.getParent(((UIObject) c.getOwner())
                    .getElement());
            final Element captionContainer = DOM.createDiv();
            DOM.insertChild(container, captionContainer, 0);
            insert(w, captionContainer, beforeIndex, false);
        } else {
            final Element wrapper = createWidgetWrappper();
            DOM.insertChild(childContainer, wrapper, beforeIndex);
            insert(w, getWidgetContainerFromWrapper(wrapper), beforeIndex,
                    false);
        }
    }

    protected Element getWidgetContainerFromWrapper(Element wrapper) {
        switch (orientationMode) {
        case ORIENTATION_HORIZONTAL:
            return wrapper;
        default:
            return DOM.getFirstChild(wrapper);
        }
    }

    /**
     * creates an Element which will contain child widget
     */
    protected Element createWidgetWrappper() {
        final Element td = DOM.createTD();
        // We need this overflow:hidden, because it's the default rendering of
        // IE (although it can be overridden with overflow:visible).
        DOM.setStyleAttribute(td, "overflow", "hidden");
        switch (orientationMode) {
        case ORIENTATION_HORIZONTAL:
            return td;
        default:
            final Element tr = DOM.createTR();
            DOM.appendChild(tr, td);
            return tr;
        }
    }

    public boolean hasChildComponent(Widget component) {
        return getWidgetIndex(component) >= 0;
    }

    public void updateCaption(Paintable component, UIDL uidl) {

        Caption c = (Caption) componentToCaption.get(component);

        if (Caption.isNeeded(uidl)) {
            if (c == null) {
                final int index = getWidgetIndex((Widget) component);
                c = new Caption(component, client);
                insert(c, index);
                componentToCaption.put(component, c);
            }
            c.updateCaption(uidl);
        } else {
            if (c != null) {
                remove(c);
                componentToCaption.remove(component);
            }
        }
    }

    public void removeCaption(Widget w) {
        final Caption c = (Caption) componentToCaption.get(w);
        if (c != null) {
            this.remove(c);
            componentToCaption.remove(w);
        }
    }

    public void add(Widget w) {
        final Element wrapper = createWidgetWrappper();
        DOM.appendChild(childContainer, wrapper);
        super.add(w, orientationMode == ORIENTATION_HORIZONTAL ? wrapper : DOM
                .getFirstChild(wrapper));
    }

    public boolean remove(int index) {
        return remove(getWidget(index));
    }

    public boolean remove(Widget w) {
        final Element wrapper = DOM.getParent(w.getElement());
        final boolean removed = super.remove(w);
        if (removed) {
            if (!(w instanceof Caption)) {
                DOM.removeChild(childContainer,
                        orientationMode == ORIENTATION_HORIZONTAL ? wrapper
                                : DOM.getParent(wrapper));
            }
            return true;
        }
        return false;
    }

    public Widget getWidget(int index) {
        return getChildren().get(index);
    }

    public int getWidgetCount() {
        return getChildren().size();
    }

    public int getWidgetIndex(Widget child) {
        return getChildren().indexOf(child);
    }

    protected void handleMargins(UIDL uidl) {
        // Modify layout margins
        String marginClasses = "";
        final MarginInfo margins = new MarginInfo(uidl
                .getIntAttribute("margins"));
        final Element topBottomMarginContainer = orientationMode == ORIENTATION_HORIZONTAL ? DOM
                .getParent(childContainer)
                : childContainer;
        // Top margin
        // remove from current position so we can insert it to proper position
        if (topMargin != null) {
            DOM.removeChild(topBottomMarginContainer, topMargin);
        }
        topMargin = null;
        if (margins.hasTop()) {
            marginClasses += " " + StyleConstants.LAYOUT_MARGIN_TOP;
            if (topMargin == null) {
                // We need to insert a new row in to the table
                topMargin = DOM.createTR();
                DOM.appendChild(topMargin, DOM.createTD());
                DOM.appendChild(DOM.getFirstChild(topMargin), DOM.createDiv());
                DOM.setElementProperty(topMargin, "className", CLASSNAME
                        + "-toppad");
                if (orientationMode == ORIENTATION_HORIZONTAL) {
                    DOM.setElementAttribute(DOM.getFirstChild(topMargin),
                            "colspan", "" + getPaintables().size());
                }
                DOM.insertChild(topBottomMarginContainer, topMargin, 0);
            }
        }

        // Right margin
        if (margins.hasRight()) {
            marginClasses += " " + StyleConstants.LAYOUT_MARGIN_RIGHT;
        }

        // Bottom margin
        // remove from current position so we can insert it to proper position
        if (bottomMargin != null) {
            DOM.removeChild(topBottomMarginContainer, bottomMargin);
        }
        bottomMargin = null;
        if (margins.hasBottom()) {
            marginClasses += " " + StyleConstants.LAYOUT_MARGIN_BOTTOM;
            if (bottomMargin == null) {
                // We need to insert a new row in to the table
                bottomMargin = DOM.createTR();
                DOM.appendChild(bottomMargin, DOM.createTD());
                DOM.appendChild(DOM.getFirstChild(bottomMargin), DOM
                        .createDiv());
                DOM.setElementProperty(bottomMargin, "className", CLASSNAME
                        + "-bottompad");
                if (orientationMode == ORIENTATION_HORIZONTAL) {
                    DOM.setElementAttribute(DOM.getFirstChild(bottomMargin),
                            "colspan", "" + getPaintables().size());
                }
                DOM.appendChild(topBottomMarginContainer, bottomMargin);
            }
        }

        // Left margin
        if (margins.hasLeft()) {
            marginClasses += " " + StyleConstants.LAYOUT_MARGIN_LEFT;
        }

        // Add
        DOM.setElementProperty(margin, "className", marginClasses);
    }

    protected void handleAlignments(UIDL uidl) {
        // Component alignments as a comma separated list.
        // See com.itmill.toolkit.terminal.gwt.client.ui.AlignmentInfo.java for
        // possible values.
        final int[] alignments = uidl.getIntArrayAttribute("alignments");
        int alignmentIndex = 0;
        // Insert alignment attributes
        final Iterator it = getPaintables().iterator();
        while (it.hasNext()) {

            // Calculate alignment info
            final AlignmentInfo ai = new AlignmentInfo(
                    alignments[alignmentIndex++]);

            final Element td = DOM.getParent(((Widget) it.next()).getElement());
            if (Util.isIE()) {
                DOM
                        .setElementAttribute(td, "vAlign", ai
                                .getVerticalAlignment());
            } else {
                DOM.setStyleAttribute(td, "verticalAlign", ai
                        .getVerticalAlignment());
            }
            // TODO use one-cell table to implement horizontal alignments
            if (Util.isIE()) {
                DOM.setElementAttribute(td, "align", ai
                        .getHorizontalAlignment());
            } else {
                DOM.setStyleAttribute(td, "textAlign", ai
                        .getHorizontalAlignment());
            }
        }
    }

}
