/*
 * (C) Copyright 2006-2013 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 * $Id: AbstractWidgetTypeHandler.java 28491 2008-01-04 19:04:30Z sfermigier $
 */

package org.nuxeo.ecm.platform.forms.layout.facelets.plugins;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;
import org.nuxeo.ecm.platform.forms.layout.api.exceptions.WidgetException;
import org.nuxeo.ecm.platform.forms.layout.facelets.FaceletHandlerHelper;
import org.nuxeo.ecm.platform.forms.layout.facelets.LeafFaceletHandler;
import org.nuxeo.ecm.platform.forms.layout.facelets.RenderVariables;
import org.nuxeo.ecm.platform.forms.layout.facelets.WidgetTypeHandler;
import org.nuxeo.ecm.platform.forms.layout.facelets.dev.WidgetTypeDevTagHandler;
import org.nuxeo.ecm.platform.ui.web.tag.handler.TagConfigFactory;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletHandler;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributes;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.ui.DecorateHandler;

/**
 * Abstract widget type handler.
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public abstract class AbstractWidgetTypeHandler implements WidgetTypeHandler {

    private static final long serialVersionUID = -2933485416045771633L;

    public static final String DEV_TEMPLATE_PROPERTY_NAME = "dev_template";

    protected Map<String, String> properties;

    public abstract FaceletHandler getFaceletHandler(FaceletContext ctx,
            TagConfig tagConfig, Widget widget, FaceletHandler[] subHandlers)
            throws WidgetException;

    public FaceletHandler getDevFaceletHandler(FaceletContext ctx,
            TagConfig tagConfig, Widget widget) throws WidgetException {
        String template = getDevTemplateValue(widget);
        FaceletHandlerHelper helper = new FaceletHandlerHelper(ctx, tagConfig);
        if (StringUtils.isBlank(template)) {
            return getDefaultDevFaceletHandler(ctx, helper, tagConfig, widget);
        }
        TagAttribute templateAttr = helper.createAttribute("template", template);
        TagAttributes attributes = FaceletHandlerHelper.getTagAttributes(templateAttr);
        String widgetTagConfigId = widget.getTagConfigId();
        TagConfig config = TagConfigFactory.createTagConfig(tagConfig,
                widgetTagConfigId, attributes, new LeafFaceletHandler());
        DecorateHandler includeHandler = new DecorateHandler(config);
        return includeHandler;
    }

    protected FaceletHandler getDefaultDevFaceletHandler(FaceletContext ctx,
            FaceletHandlerHelper helper, TagConfig config, Widget widget) {
        // use the default dev handler for widget types
        TagAttribute attr = helper.createAttribute(
                "widget",
                String.format("#{%s}",
                        RenderVariables.widgetVariables.widget.name()));
        TagAttributes devWidgetAttributes = FaceletHandlerHelper.getTagAttributes(attr);
        TagConfig devWidgetConfig = TagConfigFactory.createTagConfig(config,
                widget.getTagConfigId(), devWidgetAttributes,
                new LeafFaceletHandler());
        return new WidgetTypeDevTagHandler(devWidgetConfig);
    }

    public String getProperty(String name) {
        if (properties != null) {
            return properties.get(name);
        }
        return null;
    }

    /**
     * Helper method, throws an exception if property value is null.
     */
    public String getRequiredProperty(String name) throws WidgetException {
        String value = getProperty(name);
        if (value == null) {
            throw new WidgetException(String.format(
                    "Required property %s is missing "
                            + "on widget type configuration", name));
        }
        return value;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    protected String getDevTemplateValue(Widget widget) {
        // lookup in the widget type configuration
        String template = getProperty(DEV_TEMPLATE_PROPERTY_NAME);
        if (template == null) {
            // lookup in the widget configuration
            template = (String) widget.getProperty(DEV_TEMPLATE_PROPERTY_NAME);
        }
        return template;
    }

}
