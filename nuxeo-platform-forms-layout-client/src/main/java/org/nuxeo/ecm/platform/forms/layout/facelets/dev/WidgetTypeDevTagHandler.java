/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Anahide Tchertchian
 */
package org.nuxeo.ecm.platform.forms.layout.facelets.dev;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import org.nuxeo.ecm.platform.forms.layout.api.Widget;
import org.nuxeo.ecm.platform.forms.layout.facelets.FaceletHandlerHelper;
import org.nuxeo.ecm.platform.ui.web.tag.handler.TagConfigFactory;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributes;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.ui.DecorateHandler;

/**
 * Default widget type dev tag handler, displaying information about current
 * widget regardless of its type.
 *
 * @since 5.9.1
 */
public class WidgetTypeDevTagHandler extends TagHandler {

    protected final String TEMPLATE = "/widgets/dev/widget_dev_template.xhtml";

    protected final TagConfig config;

    protected final TagAttribute widget;

    public WidgetTypeDevTagHandler(TagConfig config) {
        super(config);
        this.config = config;
        this.widget = getAttribute("widget");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {
        Widget widgetInstance = (Widget) widget.getObject(ctx, Widget.class);
        FaceletHandlerHelper helper = new FaceletHandlerHelper(ctx, config);
        TagAttribute templateAttr = helper.createAttribute("template", TEMPLATE);
        TagAttributes attributes = FaceletHandlerHelper.getTagAttributes(templateAttr);
        String widgetTagConfigId = widgetInstance.getTagConfigId();
        TagConfig config = TagConfigFactory.createTagConfig(this.config,
                widgetTagConfigId, attributes, nextHandler);
        DecorateHandler includeHandler = new DecorateHandler(config);
        includeHandler.apply(ctx, parent);
    }
}
