/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.contextmenu;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.MenuItem.MenuItemClickEvent;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

/**
 * Server-side component for {@code <vaadin-context-menu>}
 * 
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
@HtmlImport("flow-component-renderer.html")
@HtmlImport("frontend://bower_components/vaadin-list-box/src/vaadin-list-box.html")
public class ContextMenu extends GeneratedVaadinContextMenu<ContextMenu>
        implements HasComponents {

    // Fired with right-click or long touch
    private static final String DEFAULT_OPEN_EVENT = "vaadin-contextmenu";

    private static final String CLICK_EVENT = "click";

    private Component target;

    private Element template;
    private Element container;

    private Registration openOnRegistration;
    private Element targetChildElement;

    /**
     * Creates an empty context menu.
     */
    public ContextMenu() {
        template = new Element("template");
        getElement().appendChild(template);

        container = new Element("vaadin-list-box");
        getElement().appendVirtualChild(container);

        getElement().getNode()
                .runWhenAttached(ui -> ui.beforeClientResponse(this,
                        context -> attachComponentRenderer()));

        // Workaround for: https://github.com/vaadin/flow/issues/3496
        getElement().setProperty("opened", false);

        setOpenOn(DEFAULT_OPEN_EVENT);
    }

    /**
     * Creates an empty context menu with the given target component.
     * 
     * @param target
     *            the target component for this context menu
     * @see #setTarget(Component)
     */
    public ContextMenu(Component target) {
        this();
        setTarget(target);
    }

    /**
     * Sets the target component for this context menu.
     * <p>
     * The context menu can be opened with a right click or a long touch on the
     * target component.
     * 
     * @param target
     *            the target component for this context menu
     */
    public void setTarget(Component target) {
        this.target = target;
        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, context -> ui.getPage()
                        .executeJavaScript("$0.listenOn=$1", this, target)));
        updateOpenOnListener();
    }

    private void updateOpenOnListener() {
        if (openOnRegistration != null) {
            openOnRegistration.remove();
        }
        if (target == null) {
            return;
        }

        //@formatter:off
        String childIndexScript = "(function(){"
                + "var e = event.target;"
                + "if(e == element){"
                +   "return -1;"
                + "}"
                + "while(e.parentElement != element){"
                +   "e = e.parentElement;"
                + "}"
                + "var i = 0;" 
                + "while((e = e.previousSibling) != null){" 
                + "  i++;"
                + "}"
                + "return i;"
                + "})()";
        //@formatter:on
        openOnRegistration = target.getElement()
                .addEventListener(getOpenOnString(), event -> {
                    int index = (int) event.getEventData()
                            .getNumber(childIndexScript);
                    if (index == -1) {
                        targetChildElement = null;
                    } else {
                        targetChildElement = target.getElement()
                                .getChild(index);
                    }
                }).addEventData(childIndexScript);
    }

    protected Element getTargetChildElement() {
        return targetChildElement;
    }

    /**
     * Gets the target component of this context menu, or {@code null} if it
     * doesn't have a target.
     * 
     * @return the target component of this context menu
     * @see #setTarget(Component)
     */
    public Component getTarget() {
        return target;
    }

    /**
     * Determines the way for opening the context menu.
     * <p>
     * By default, the context menu can be opened with a right click or a long
     * touch on the target component.
     * 
     * @param openOnClick
     *            if {@code true}, the context menu can be opened with left
     *            click only. Otherwise the context menu follows the default
     *            behavior.
     */
    public void setOpenOnClick(boolean openOnClick) {
        String value = openOnClick ? CLICK_EVENT : DEFAULT_OPEN_EVENT;
        setOpenOn(value);
    }

    @Override
    protected void setOpenOn(String openOn) {
        assert openOn != null;
        if (openOn.equals(getOpenOnString())) {
            return;
        }
        super.setOpenOn(openOn);
        updateOpenOnListener();
    }

    /**
     * Gets whether the context menu can be opened via left click.
     * <p>
     * By default, this will return {@code false} and context menu can be opened
     * with a right click or a long touch on the target component.
     * 
     * @return {@code true} if the context menu can be opened with left click
     *         only. Otherwise the context menu follows the default behavior.
     */
    public boolean isOpenOnClick() {
        return CLICK_EVENT.equals(getOpenOnString());
    }

    /**
     * Closes this context menu if it is currently open.
     */
    @Override
    public void close() {
        super.close();
    }

    /**
     * Adds a new item component with the given text content and click listener
     * to the context menu overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * high-lightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-high-lightable
     * components between the items, use the {@link #add(Component...)} method.
     * 
     * @param text
     *            the text content for the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     * @see #addItem(Component, ComponentEventListener)
     * @see #add(Component...)
     */
    public MenuItem addItem(String text,
            ComponentEventListener<MenuItemClickEvent> clickListener) {
        MenuItem menuItem = addItem();
        menuItem.setText(text);
        if (clickListener != null) {
            menuItem.addClickListener(clickListener);
        }
        return menuItem;
    }

    /**
     * Adds a new item component with the given component and click listener to
     * the context menu overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * high-lightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-high-lightable
     * components between the items, use the {@link #add(Component...)} method.
     * 
     * @param component
     *            the component inside the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     * @see #addItem(String, ComponentEventListener)
     * @see #add(Component...)
     */
    public MenuItem addItem(Component component,
            ComponentEventListener<MenuItemClickEvent> clickListener) {
        MenuItem menuItem = addItem();
        menuItem.add(component);
        if (clickListener != null) {
            menuItem.addClickListener(clickListener);
        }
        return menuItem;
    }

    protected MenuItem addItem() {
        MenuItem menuItem = new MenuItem(this);
        add(menuItem);
        return menuItem;
    }

    /**
     * Adds the given components into the context menu overlay.
     * <p>
     * For the common use case of having a list of high-lightable items inside
     * the overlay, you can use the
     * {@link #addItem(Component, ComponentEventListener)} convenience methods
     * instead.
     * <p>
     * The added elements in the DOM will not be children of the
     * {@code <vaadin-context-menu>} element, but will be inserted into an
     * overlay that is attached into the {@code <body>}.
     *
     * @param components
     *            the components to add
     * @see #addItem(String, ComponentEventListener)
     * @see #addItem(Component, ComponentEventListener)
     */
    @Override
    public void add(Component... components) {
        Objects.requireNonNull(components, "Components to add cannot be null");
        for (Component component : components) {
            Objects.requireNonNull(component,
                    "Component to add cannot be null");
            container.appendChild(component.getElement());
        }
    }

    @Override
    public void remove(Component... components) {
        Objects.requireNonNull(components,
                "Components to remove cannot be null");
        for (Component component : components) {
            Objects.requireNonNull(component,
                    "Component to remove cannot be null");
            if (container.equals(component.getElement().getParent())) {
                container.removeChild(component.getElement());
            } else {
                throw new IllegalArgumentException("The given component ("
                        + component + ") is not a child of this component");
            }
        }
    }

    /**
     * {@inheritDoc} This also removes all the items added with
     * {@link #addItem(String)} and its overload methods.
     */
    @Override
    public void removeAll() {
        container.removeAllChildren();
    }

    /**
     * Gets the child components of this component. This includes components
     * added with {@link #add(Component...)} and the {@link MenuItem} components
     * created with {@link #addItem(String)} and its overload methods.
     *
     * @return the child components of this component
     */
    @Override
    public Stream<Component> getChildren() {
        Builder<Component> childComponents = Stream.builder();
        container.getChildren().forEach(childElement -> ComponentUtil
                .findComponents(childElement, childComponents::add));
        return childComponents.build();
    }

    /**
     * Gets the items added to this component (the children of this component
     * that are instances of {@link MenuItem}).
     * 
     * @return the {@link MenuItem} components in this context menu
     * @see #addItem(String, ComponentEventListener)
     */
    public List<MenuItem> getItems() {
        return getChildren().filter(MenuItem.class::isInstance)
                .map(child -> (MenuItem) child).collect(Collectors.toList());
    }

    /**
     * Gets the open state from the context menu.
     *
     * @return the {@code opened} property from the context menu
     */
    public boolean isOpened() {
        return super.isOpenedBoolean();
    }

    /**
     * Adds a listener for the {@code opened-changed} events fired by the web
     * component.
     *
     * @param listener
     *            the listener to add
     * @return a Registration for removing the event listener
     */
    @Override
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent<ContextMenu>> listener) {
        return super.addOpenedChangeListener(listener);
    }

    private void attachComponentRenderer() {
        String appId = UI.getCurrent().getInternals().getAppId();
        int nodeId = container.getNode().getId();
        String renderer = String.format(
                "<flow-component-renderer appid=\"%s\" nodeid=\"%s\"></flow-component-renderer>",
                appId, nodeId);
        template.setProperty("innerHTML", renderer);
    }

}
