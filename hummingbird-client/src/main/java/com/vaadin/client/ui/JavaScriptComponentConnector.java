/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.client.ui;

import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.JavaScriptConnectorHelper;
import com.vaadin.client.communication.HasJavaScriptConnectorHelper;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.JavaScriptComponentState;
import com.vaadin.ui.AbstractJavaScriptComponent;

@Connect(AbstractJavaScriptComponent.class)
public final class JavaScriptComponentConnector extends
        AbstractHasComponentsConnector implements HasJavaScriptConnectorHelper {

    private final JavaScriptConnectorHelper helper = new JavaScriptConnectorHelper(
            this) {
        @Override
        protected void showInitProblem(
                java.util.ArrayList<String> attemptedNames) {
            getWidget().showNoInitFound(attemptedNames);
        }
    };

    @Override
    protected com.google.gwt.user.client.ui.Widget createWidget() {
        return new JavaScriptWidget("div");
    };

    @Override
    public JavaScriptWidget getWidget() {
        return (JavaScriptWidget) super.getWidget();
    }

    @Override
    protected void init() {
        super.init();
        helper.init();
    }

    @Override
    public JavaScriptConnectorHelper getJavascriptConnectorHelper() {
        return helper;
    }

    @Override
    public JavaScriptComponentState getState() {
        return (JavaScriptComponentState) super.getState();
    }

    @Override
    public void onUnregister() {
        super.onUnregister();
        helper.onUnregister();
    }

    @Override
    public void onConnectorHierarchyChange(
            ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        // TODO
    }

}
