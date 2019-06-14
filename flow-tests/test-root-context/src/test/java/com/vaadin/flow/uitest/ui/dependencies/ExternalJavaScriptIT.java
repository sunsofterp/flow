/*
 * Copyright 2000-2019 Vaadin Ltd.
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

package com.vaadin.flow.uitest.ui.dependencies;

import java.util.List;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ExternalJavaScriptIT extends ChromeBrowserTest {
    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("div"));
    }

    @Test
    public void javaScriptAnnotation_externalJs_shouldBeAddedToPage() {
        List<WebElement> scriptTags = findElements(By.tagName("script"));
        Assert.assertTrue(
                "External JS annotated with @JavaScript annotation should be added as a script tag with text/javascript type to the page!",
                scriptTags.stream().anyMatch(
                        scriptTag -> ExternalJavaScriptView.SOME_RANDOM_EXTERNAL_JS_URL
                                .equals(scriptTag.getAttribute("src"))
                                && "text/javascript".equals(
                                        scriptTag.getAttribute("type"))));
    }

    @Test
    public void javaScriptAnnotation_externalJsInAComponentBeingAdded_shouldBeAddedToPage() {
        findElement(By.id("addComponentButton")).click();
        waitForElementPresent(By.id("componentWithExternalJavaScript"));

        List<WebElement> scriptTags = findElements(By.tagName("script"));
        Assert.assertTrue(
                "When a component is added to the page, external JS annotated with @JavaScript annotation in the component should be added as a script tag with text/javascript type to the page!",
                scriptTags.stream().anyMatch(
                        scriptTag -> ComponentWithExternalJavaScript.SOME_RANDOM_EXTERNAL_JS_URL
                                .equals(scriptTag.getAttribute("src"))
                                && "text/javascript".equals(
                                scriptTag.getAttribute("type"))));
    }
}
