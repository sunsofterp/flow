package com.vaadin.tests.components.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Hierarchical;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.tests.components.select.AbstractSelectTestCase;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.Tree.ItemStyleGenerator;

public class Trees extends AbstractSelectTestCase<Tree>
        implements ExpandListener, CollapseListener {

    private int rootItemIds = 3;

    private ItemStyleGenerator rootGreenSecondLevelRed = new com.vaadin.ui.Tree.ItemStyleGenerator() {

        @Override
        public String getStyle(Tree source, Object itemId) {
            Hierarchical c = (Container.Hierarchical) getComponent()
                    .getContainerDataSource();
            if (c.isRoot(itemId)) {
                return "green";
            }

            Object parent = c.getParent(itemId);
            if (!c.isRoot(parent)) {
                return "red";
            }

            return null;
        }

        @Override
        public String toString() {
            return "Root green, second level red";
        }

    };

    private ItemStyleGenerator evenItemsBold = new com.vaadin.ui.Tree.ItemStyleGenerator() {

        @Override
        public String getStyle(Tree source, Object itemId) {
            Hierarchical c = (Container.Hierarchical) getComponent()
                    .getContainerDataSource();
            int idx = 0;

            for (Iterator<?> i = c.getItemIds().iterator(); i.hasNext();) {
                Object id = i.next();
                if (id == itemId) {
                    if (idx % 2 == 1) {
                        return "bold";
                    } else {
                        return null;
                    }
                }

                idx++;
            }

            return null;
        }

        @Override
        public String toString() {
            return "Even items bold";
        }

    };

    @Override
    protected Class<Tree> getTestClass() {
        return Tree.class;
    }

    @Override
    protected void createActions() {
        super.createActions();

        // Causes container changes so doing this first..
        createRootItemSelectAction(CATEGORY_DATA_SOURCE);

        createExpandCollapseActions(CATEGORY_FEATURES);
        createSelectionModeSelect(CATEGORY_SELECTION);
        createChildrenAllowedAction(CATEGORY_DATA_SOURCE);

        createListeners(CATEGORY_LISTENERS);
        createItemStyleGenerator(CATEGORY_FEATURES);

    }

    private void createItemStyleGenerator(String category) {

        LinkedHashMap<String, com.vaadin.ui.Tree.ItemStyleGenerator> options = new LinkedHashMap<String, com.vaadin.ui.Tree.ItemStyleGenerator>();

        options.put("-", null);
        options.put(rootGreenSecondLevelRed.toString(),
                rootGreenSecondLevelRed);
        options.put(evenItemsBold.toString(), evenItemsBold);

        createSelectAction("Item Style generator", category, options, "-",
                itemStyleGeneratorCommand);

    }

    private void createListeners(String category) {
        createBooleanAction("Expand listener", category, false,
                expandListenerCommand);
        createBooleanAction("Collapse listener", category, false,
                collapseListenerCommand);
        createBooleanAction("Item click listener", category, false,
                itemClickListenerCommand);

    }

    private enum SelectMode {
        NONE, SINGLE, MULTI_SIMPLE, MULTI;
    }

    protected void createSelectionModeSelect(String category) {
        LinkedHashMap<String, SelectMode> options = new LinkedHashMap<String, SelectMode>();
        options.put("None", SelectMode.NONE);
        options.put("Single", SelectMode.SINGLE);
        options.put("Multi - simple", SelectMode.MULTI_SIMPLE);
        options.put("Multi - ctrl/shift", SelectMode.MULTI);

        createSelectAction("Selection Mode", category, options,
                "Multi - ctrl/shift", new Command<Tree, SelectMode>() {

                    @Override
                    public void execute(Tree t, SelectMode value, Object data) {
                        switch (value) {
                        case NONE:
                            t.setSelectable(false);
                            break;
                        case SINGLE:
                            t.setMultiSelect(false);
                            t.setSelectable(true);
                            break;
                        case MULTI_SIMPLE:
                            t.setSelectable(true);
                            t.setMultiSelect(true);
                            t.setMultiselectMode(MultiSelectMode.SIMPLE);
                            break;
                        case MULTI:
                            t.setSelectable(true);
                            t.setMultiSelect(true);
                            t.setMultiselectMode(MultiSelectMode.DEFAULT);
                            break;
                        }
                    }
                });
    }

    @Override
    protected Container createContainer(int properties, int items) {
        return createHierarchicalContainer(properties, items, rootItemIds);
    }

    private Container.Hierarchical createHierarchicalContainer(int properties,
            int items, int roots) {
        Container.Hierarchical c = new HierarchicalContainer();

        populateContainer(c, properties, items);

        if (items <= roots) {
            return c;
        }

        // "roots" roots, each with
        // "firstLevel" children, two with no children (one with childAllowed,
        // one without)
        // ("firstLevel"-2)*"secondLevel" children ("secondLevel"/2 with
        // childAllowed, "secondLevel"/2 without)

        // N*M+N*(M-2)*C = items
        // items=N(M+MC-2C)

        // Using secondLevel=firstLevel/2 =>
        // items = roots*(firstLevel+firstLevel*firstLevel/2-2*firstLevel/2)
        // =roots*(firstLevel+firstLevel^2/2-firstLevel)
        // = roots*firstLevel^2/2
        // => firstLevel = sqrt(items/roots*2)

        int firstLevel = (int) Math.ceil(Math.sqrt(items / roots * 2.0));
        int secondLevel = firstLevel / 2;

        while (roots * (1 + 2 + (firstLevel - 2) * secondLevel) < items) {
            // Increase something so we get enough items
            secondLevel++;
        }

        List<Object> itemIds = new ArrayList<Object>(c.getItemIds());

        int nextItemId = roots;
        for (int rootIndex = 0; rootIndex < roots; rootIndex++) {
            // roots use items 0..roots-1
            Object rootItemId = itemIds.get(rootIndex);

            // force roots to be roots even though they automatically should be
            c.setParent(rootItemId, null);

            for (int firstLevelIndex = 0; firstLevelIndex < firstLevel; firstLevelIndex++) {
                if (nextItemId >= items) {
                    break;
                }
                Object firstLevelItemId = itemIds.get(nextItemId++);
                c.setParent(firstLevelItemId, rootItemId);

                if (firstLevelIndex < 2) {
                    continue;
                }

                // firstLevelChildren 2.. have child nodes
                for (int secondLevelIndex = 0; secondLevelIndex < secondLevel; secondLevelIndex++) {
                    if (nextItemId >= items) {
                        break;
                    }

                    Object secondLevelItemId = itemIds.get(nextItemId++);
                    c.setParent(secondLevelItemId, firstLevelItemId);
                }
            }
        }

        return c;
    }

    private void createRootItemSelectAction(String category) {
        LinkedHashMap<String, Integer> options = new LinkedHashMap<String, Integer>();
        for (int i = 1; i <= 10; i++) {
            options.put(String.valueOf(i), i);
        }
        options.put("20", 20);
        options.put("50", 50);
        options.put("100", 100);

        createSelectAction("Number of root items", category, options, "3",
                rootItemIdsCommand);
    }

    private void createExpandCollapseActions(String category) {
        LinkedHashMap<String, Object> options = new LinkedHashMap<String, Object>();

        for (Object id : getComponent().getItemIds()) {
            options.put(id.toString(), id);
        }
        createMultiClickAction("Expand", category, options, expandItemCommand,
                null);
        createMultiClickAction("Expand recursively", category, options,
                expandItemRecursivelyCommand, null);
        createMultiClickAction("Collapse", category, options,
                collapseItemCommand, null);

    }

    private void createChildrenAllowedAction(String category) {
        LinkedHashMap<String, Object> options = new LinkedHashMap<String, Object>();

        for (Object id : getComponent().getItemIds()) {
            options.put(id.toString(), id);
        }
        createMultiToggleAction("Children allowed", category, options,
                setChildrenAllowedCommand, true);

    }

    /*
     * COMMANDS
     */
    private Command<Tree, Integer> rootItemIdsCommand = new Command<Tree, Integer>() {

        @Override
        public void execute(Tree c, Integer value, Object data) {
            rootItemIds = value;
            updateContainer();
        }
    };

    private Command<Tree, Object> expandItemCommand = new Command<Tree, Object>() {

        @Override
        public void execute(Tree c, Object itemId, Object data) {
            c.expandItem(itemId);
        }
    };
    private Command<Tree, Object> expandItemRecursivelyCommand = new Command<Tree, Object>() {

        @Override
        public void execute(Tree c, Object itemId, Object data) {
            c.expandItemsRecursively(itemId);
        }
    };

    private Command<Tree, Object> collapseItemCommand = new Command<Tree, Object>() {

        @Override
        public void execute(Tree c, Object itemId, Object data) {
            c.collapseItem(itemId);
        }
    };

    private Command<Tree, Boolean> setChildrenAllowedCommand = new Command<Tree, Boolean>() {

        @Override
        public void execute(Tree c, Boolean areChildrenAllowed, Object itemId) {
            c.setChildrenAllowed(itemId, areChildrenAllowed);
        }
    };

    private Command<Tree, Boolean> expandListenerCommand = new Command<Tree, Boolean>() {
        @Override
        public void execute(Tree c, Boolean value, Object data) {
            if (value) {
                c.addExpandListener(Trees.this);
            } else {
                c.removeExpandListener(Trees.this);
            }
        }
    };

    private Command<Tree, Boolean> collapseListenerCommand = new Command<Tree, Boolean>() {
        @Override
        public void execute(Tree c, Boolean value, Object data) {
            if (value) {
                c.addCollapseListener(Trees.this);
            } else {
                c.removeCollapseListener(Trees.this);
            }
        }
    };

    private Command<Tree, com.vaadin.ui.Tree.ItemStyleGenerator> itemStyleGeneratorCommand = new Command<Tree, com.vaadin.ui.Tree.ItemStyleGenerator>() {

        @Override
        public void execute(Tree c, com.vaadin.ui.Tree.ItemStyleGenerator value,
                Object data) {
            c.setItemStyleGenerator(value);

        }
    };

    @Override
    public void nodeCollapse(CollapseEvent event) {
        log(event.getClass().getSimpleName() + ": " + event.getItemId());
    }

    @Override
    public void nodeExpand(ExpandEvent event) {
        log(event.getClass().getSimpleName() + ": " + event.getItemId());
    }

}
