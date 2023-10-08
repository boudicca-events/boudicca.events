/**
 * Copyright (c) 2012-2015 Edgar Espina
 * <p>
 * This file is part of Handlebars.java.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package events.boudicca.publisherhtml.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.ValueResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.AbstractTemplateView;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * COPIED FROM https://github.com/jknack/handlebars.java/tree/master/handlebars-springmvc/src/main/java/com/github/jknack/handlebars/springmvc
 * and modified to jakarta namespace
 *
 * A handlebars view implementation.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class HandlebarsView extends AbstractTemplateView {

    /**
     * The compiled template.
     */
    protected Template template;

    /**
     * The value's resolvers.
     */
    protected ValueResolver[] valueResolvers;

    /**
     * Merge model into the view. {@inheritDoc}
     */
    @Override
    protected void renderMergedTemplateModel(final Map<String, Object> model,
                                             final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        Context context = Context.newBuilder(model)
                .resolver(valueResolvers)
                .build();
        try {
            template.apply(context, response.getWriter());
        } finally {
            context.destroy();
        }
    }

    /**
     * @return The underlying template for this view.
     */
    public Template getTemplate() {
        return template;
    }

    @Override
    public boolean checkResource(final Locale locale) {
        return template != null;
    }

    /**
     * Set the compiled template.
     *
     * @param template The compiled template. Required.
     */
    public void setTemplate(final Template template) {
        this.template = requireNonNull(template, "A handlebars template is required.");
    }

    /**
     * Set the value resolvers.
     *
     * @param valueResolvers The value resolvers. Required.
     */
    public void setValueResolver(final ValueResolver... valueResolvers) {
        this.valueResolvers = requireNonNull(valueResolvers,
                "At least one value-resolver must be present.");
    }

    @Override
    protected boolean isContextRequired() {
        return false;
    }
}
