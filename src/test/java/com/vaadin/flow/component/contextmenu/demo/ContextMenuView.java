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
package com.vaadin.flow.component.contextmenu.demo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link ContextMenu} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-context-menu")
public class ContextMenuView extends DemoView {

    @Override
    public void initView() {
        addBasicContextMenu();
        addContextMenuWithComponents();
    }

    private void addBasicContextMenu() {
        // begin-source-example
        // source-example-heading: Basic ContextMenu
        ContextMenu contextMenu = new ContextMenu();

        Paragraph target = new Paragraph("Open the context menu with "
                + "a right click or a long touch on the target component");
        contextMenu.setTarget(target);

        contextMenu.setItems(getItems());

        // end-source-example

        addCard("Basic ContextMenu", target, contextMenu);
        target.setId("basic-context-menu-target");
        contextMenu.setId("basic-context-menu");
    }

    private void addContextMenuWithComponents() {
        // begin-source-example
        // source-example-heading: ContextMenu With Components
        ContextMenu contextMenu = new ContextMenu();

        Paragraph target = new Paragraph("Open the context menu with "
                + "a right click or a long touch on the target component");
        contextMenu.setTarget(target);

        Paragraph content = new Paragraph("Foo");
        contextMenu.add(content, createCheckbox());

        contextMenu.setCloseOnClick(false);

        // end-source-example

        addCard("ContextMenu With Components", target, contextMenu);
        target.setId("context-menu-with-components-target");
        contextMenu.setId("context-menu-with-components");
    }

    private List<String> getItems() {
        return IntStream.range(1, 6).mapToObj(i -> "Item " + i)
                .collect(Collectors.toList());
    }

    private Component createCheckbox() {
        Input checkbox = new Input();
        checkbox.setType("checkbox");
        checkbox.setValue("checkbox");

        return new Div(checkbox, new Label("checkbox"));

    }
}
