package com.vaadin.client.communication.tree;

import com.google.gwt.dom.client.Text;

public class TextElementListener {
    public static void bind(TreeNode node, Text textNode) {
        node.getProperty("content")
                .addPropertyChangeListener((property, oldValue) -> {
                    Object value = property.getValue();
                    textNode.setData(value == null ? "" : value.toString());
                });

    }
}