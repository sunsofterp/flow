/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.itmill.toolkit.terminal.gwt.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
import com.itmill.toolkit.terminal.gwt.client.CaptionWrapper;
import com.itmill.toolkit.terminal.gwt.client.Container;
import com.itmill.toolkit.terminal.gwt.client.Paintable;
import com.itmill.toolkit.terminal.gwt.client.UIDL;

public class IGridLayout extends FlexTable implements Paintable, Container {

    public static final String CLASSNAME = "i-gridlayout";

    /** Widget to captionwrapper map */
    private final HashMap widgetToCaptionWrapper = new HashMap();

    public IGridLayout() {
        super();
        setStyleName(CLASSNAME);
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        if (client.updateComponent(this, uidl, false)) {
            return;
        }

        if (uidl.hasAttribute("caption")) {
            setTitle(uidl.getStringAttribute("caption"));
        }
        int row = 0, column = 0;

        final ArrayList oldWidgetWrappers = new ArrayList();
        for (final Iterator iterator = iterator(); iterator.hasNext();) {
            oldWidgetWrappers.add(iterator.next());
        }
        clear();

        for (final Iterator i = uidl.getChildIterator(); i.hasNext();) {
            final UIDL r = (UIDL) i.next();
            if ("gr".equals(r.getTag())) {
                column = 0;
                for (final Iterator j = r.getChildIterator(); j.hasNext();) {
                    final UIDL c = (UIDL) j.next();
                    if ("gc".equals(c.getTag())) {
                        prepareCell(row, column);

                        // Set cell width
                        int w;
                        if (c.hasAttribute("w")) {
                            w = c.getIntAttribute("w");
                        } else {
                            w = 1;
                        }
                        // TODO do real alignments
                        (getRowFormatter()).setVerticalAlign(row,
                                HasVerticalAlignment.ALIGN_TOP);

                        // set col span
                        ((FlexCellFormatter) getCellFormatter()).setColSpan(
                                row, column, w);

                        // Set cell height
                        int h;
                        if (c.hasAttribute("h")) {
                            h = c.getIntAttribute("h");
                        } else {
                            h = 1;
                        }
                        ((FlexCellFormatter) getCellFormatter()).setRowSpan(
                                row, column, h);

                        final UIDL u = c.getChildUIDL(0);
                        if (u != null) {
                            final Paintable child = client.getPaintable(u);
                            CaptionWrapper wr;
                            if (widgetToCaptionWrapper.containsKey(child)) {
                                wr = (CaptionWrapper) widgetToCaptionWrapper
                                        .get(child);
                                oldWidgetWrappers.remove(wr);
                            } else {
                                wr = new CaptionWrapper(child, client);
                                widgetToCaptionWrapper.put(child, wr);
                            }

                            setWidget(row, column, wr);

                            if (!u.getBooleanAttribute("cached")) {
                                child.updateFromUIDL(u, client);
                            }
                        }
                        column += w;
                    }
                }
                row++;
            }
        }

        // loop oldWidgetWrappers that where not re-attached and unregister them
        for (final Iterator it = oldWidgetWrappers.iterator(); it.hasNext();) {
            final CaptionWrapper w = (CaptionWrapper) it.next();
            client.unregisterPaintable(w.getPaintable());
            widgetToCaptionWrapper.remove(w.getPaintable());
        }
    }

    public boolean hasChildComponent(Widget component) {
        if (widgetToCaptionWrapper.containsKey(component)) {
            return true;
        }
        return false;
    }

    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        // TODO Auto-generated method stub

    }

    public void updateCaption(Paintable component, UIDL uidl) {
        final CaptionWrapper wrapper = (CaptionWrapper) widgetToCaptionWrapper
                .get(component);
        wrapper.updateCaption(uidl);
    }

}
