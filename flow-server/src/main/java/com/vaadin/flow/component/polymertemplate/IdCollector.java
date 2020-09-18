/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.component.polymertemplate;

import org.jsoup.nodes.Element;

import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.template.internal.AbstractInjectableElementInitializer;
import com.vaadin.flow.internal.AnnotationReader;

/**
 * Collects information of {@link Id @Id} mapped fields in a template class.
 *
 * @since 2.0
 * @deprecated Use
 *             {@link com.vaadin.flow.component.template.internal.IdCollector}
 *             instead. This will be removed in an upcoming version.
 */
@Deprecated
public class IdCollector
        extends com.vaadin.flow.component.template.internal.IdCollector {

    /**
     * Creates a collector the the given template.
     *
     * @param templateClass
     *            the template class, containing the {@code @Id} fields
     * @param templateFile
     *            The name of the file containing the template or
     *            <code>null</code> if not available {@code null}
     * @param templateRoot
     *            The root element of the template or <code>null</code> if not
     *            available
     */
    public IdCollector(Class<?> templateClass, String templateFile,
            Element templateRoot) {
      super(templateClass, templateFile, templateRoot);
    }

}
