/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.itmill.toolkit.terminal.gwt.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
import com.itmill.toolkit.terminal.gwt.client.ContainerResizedListener;
import com.itmill.toolkit.terminal.gwt.client.ErrorMessage;
import com.itmill.toolkit.terminal.gwt.client.Paintable;
import com.itmill.toolkit.terminal.gwt.client.UIDL;
import com.itmill.toolkit.terminal.gwt.client.Util;

public class IPanel extends SimplePanel implements Paintable,
        ContainerResizedListener {

    public static final String CLASSNAME = "i-panel";

    ApplicationConnection client;

    String id;

    private final Element captionNode = DOM.createDiv();

    private final Element captionText = DOM.createSpan();

    private Icon icon;

    private final Element bottomDecoration = DOM.createDiv();

    private final Element contentNode = DOM.createDiv();

    private Element errorIndicatorElement;

    private ErrorMessage errorMessage;

    private String height;

    private Paintable layout;

    public IPanel() {
        super();
        DOM.appendChild(getElement(), captionNode);
        DOM.appendChild(captionNode, captionText);
        DOM.appendChild(getElement(), contentNode);
        DOM.appendChild(getElement(), bottomDecoration);
        setStyleName(CLASSNAME);
        DOM
                .setElementProperty(captionNode, "className", CLASSNAME
                        + "-caption");
        DOM
                .setElementProperty(contentNode, "className", CLASSNAME
                        + "-content");
        DOM.setElementProperty(bottomDecoration, "className", CLASSNAME
                + "-deco");
    }

    protected Element getContainerElement() {
        return contentNode;
    }

    private void setCaption(String text) {
        DOM.setInnerText(captionText, text);
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        // Ensure correct implementation
        if (client.updateComponent(this, uidl, false)) {
            return;
        }

        this.client = client;
        id = uidl.getId();

        // Panel size. Height needs to be saved for later use
        final String w = uidl.hasVariable("width") ? uidl
                .getStringVariable("width") : null;
        height = uidl.hasVariable("height") ? uidl.getStringVariable("height")
                : null;
        setWidth(w != null ? w : "");

        // Restore default stylenames
        DOM
                .setElementProperty(captionNode, "className", CLASSNAME
                        + "-caption");
        DOM
                .setElementProperty(contentNode, "className", CLASSNAME
                        + "-content");
        DOM.setElementProperty(bottomDecoration, "className", CLASSNAME
                + "-deco");

        // Handle caption displaying
        boolean hasCaption = false;
        if (uidl.hasAttribute("caption")
                && !uidl.getStringAttribute("caption").equals("")) {
            setCaption(uidl.getStringAttribute("caption"));
            hasCaption = true;
        } else {
            setCaption("");
            DOM.setElementProperty(captionNode, "className", CLASSNAME
                    + "-nocaption");
        }

        setIconUri(uidl, client);

        handleDescription(uidl);

        handleError(uidl);

        // Add proper stylenames for all elements. This way we can prevent
        // unwanted CSS selector inheritance.
        if (uidl.hasAttribute("style")) {
            final String[] styles = uidl.getStringAttribute("style").split(" ");
            final String captionBaseClass = CLASSNAME
                    + (hasCaption ? "-caption" : "-nocaption");
            final String contentBaseClass = CLASSNAME + "-content";
            final String decoBaseClass = CLASSNAME + "-deco";
            String captionClass = captionBaseClass;
            String contentClass = contentBaseClass;
            String decoClass = decoBaseClass;
            for (int i = 0; i < styles.length; i++) {
                captionClass += " " + captionBaseClass + "-" + styles[i];
                contentClass += " " + contentBaseClass + "-" + styles[i];
                decoClass += " " + decoBaseClass + "-" + styles[i];
            }
            DOM.setElementProperty(captionNode, "className", captionClass);
            DOM.setElementProperty(contentNode, "className", contentClass);
            DOM.setElementProperty(bottomDecoration, "className", decoClass);
        }

        // Height adjustment
        iLayout();

        // Render content
        final UIDL layoutUidl = uidl.getChildUIDL(0);
        final Paintable newLayout = client.getPaintable(layoutUidl);
        if (newLayout != layout) {
            if (layout != null) {
                client.unregisterPaintable(layout);
            }
            setWidget((Widget) newLayout);
            layout = newLayout;
        }
        (layout).updateFromUIDL(layoutUidl, client);

    }

    private void handleError(UIDL uidl) {
        if (uidl.hasAttribute("error")) {
            final UIDL errorUidl = uidl.getErrors();
            if (errorIndicatorElement == null) {
                errorIndicatorElement = DOM.createDiv();
                DOM.setElementProperty(errorIndicatorElement, "className",
                        "i-errorindicator");
                DOM.sinkEvents(errorIndicatorElement, Event.MOUSEEVENTS);
                sinkEvents(Event.MOUSEEVENTS);
            }
            DOM.insertBefore(captionNode, errorIndicatorElement, captionText);
            if (errorMessage == null) {
                errorMessage = new ErrorMessage();
            }
            errorMessage.updateFromUIDL(errorUidl);

        } else if (errorIndicatorElement != null) {
            DOM.removeChild(captionNode, errorIndicatorElement);
            errorIndicatorElement = null;
        }
    }

    private void handleDescription(UIDL uidl) {
        DOM.setElementProperty(captionText, "title", uidl
                .hasAttribute("description") ? uidl
                .getStringAttribute("description") : "");
    }

    private void setIconUri(UIDL uidl, ApplicationConnection client) {
        final String iconUri = uidl.hasAttribute("icon") ? uidl
                .getStringAttribute("icon") : null;
        if (iconUri == null) {
            if (icon != null) {
                DOM.removeChild(captionNode, icon.getElement());
                icon = null;
            }
        } else {
            if (icon == null) {
                icon = new Icon(client);
                DOM.insertChild(captionNode, icon.getElement(), 0);
            }
            icon.setUri(iconUri);
        }
    }

    public void iLayout() {
        if (height != null && height != "") {
            final boolean hasChildren = getWidget() != null;
            Element contentEl = null;
            String origPositioning = null;
            if (hasChildren) {
                // Remove children temporary form normal flow to detect proper
                // size
                contentEl = getWidget().getElement();
                origPositioning = DOM.getStyleAttribute(contentEl, "position");
                DOM.setStyleAttribute(contentEl, "position", "absolute");
            }
            // Set defaults
            DOM.setStyleAttribute(contentNode, "overflow", "hidden");
            DOM.setStyleAttribute(contentNode, "height", "");

            // Calculate target height
            super.setHeight(height);
            final int targetHeight = getOffsetHeight();

            // Calculate used height
            super.setHeight("");
            final int usedHeight = DOM.getElementPropertyInt(bottomDecoration,
                    "offsetTop")
                    + DOM.getElementPropertyInt(bottomDecoration,
                            "offsetHeight")
                    - DOM.getElementPropertyInt(getElement(), "offsetTop");

            // Calculate content area height (don't allow negative values)
            int h = targetHeight - usedHeight;
            if (h < 0) {
                h = 0;
            }

            // Set proper values for content element
            DOM.setStyleAttribute(contentNode, "height", h + "px");
            DOM.setStyleAttribute(contentNode, "overflow", "auto");

            // Restore content to flow
            if (hasChildren) {
                ApplicationConnection.getConsole().log(
                        "positioning:" + origPositioning);
                DOM.setStyleAttribute(contentEl, "position", origPositioning);
            }
        } else {
            DOM.setStyleAttribute(contentNode, "height", "");
        }
        Util.runDescendentsLayout(this);
    }

    public void onBrowserEvent(Event event) {
        final Element target = DOM.eventGetTarget(event);
        if (errorIndicatorElement != null
                && DOM.compare(target, errorIndicatorElement)) {
            switch (DOM.eventGetType(event)) {
            case Event.ONMOUSEOVER:
                if (errorMessage != null) {
                    errorMessage.showAt(errorIndicatorElement);
                }
                break;
            case Event.ONMOUSEOUT:
                if (errorMessage != null) {
                    errorMessage.hide();
                }
                break;
            case Event.ONCLICK:
                ApplicationConnection.getConsole().log(
                        DOM.getInnerHTML(errorMessage.getElement()));
                return;
            default:
                break;
            }
        }
    }

}
