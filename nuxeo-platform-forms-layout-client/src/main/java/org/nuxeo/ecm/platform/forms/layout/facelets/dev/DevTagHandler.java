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

import org.nuxeo.ecm.platform.forms.layout.facelets.FaceletHandlerHelper;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.FaceletHandler;
import com.sun.facelets.tag.CompositeFaceletHandler;
import com.sun.facelets.tag.TagAttributes;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.jsf.ComponentHandler;

/**
 * Dev tag container, displaying a div and decorating the dev handler for dev
 * mode rendering, and displaying the original handler (layout or widget
 * handler) after that.
 *
 * @since 5.9.1
 */
public class DevTagHandler extends TagHandler {

    protected final TagConfig config;

    protected final String refId;

    protected final FaceletHandler originalHandler;

    protected final FaceletHandler devHandler;

    protected static final String PANEL_COMPONENT_TYPE = "org.ajax4jsf.OutputPanel";

    public DevTagHandler(TagConfig config, String refId,
            FaceletHandler originalHandler, FaceletHandler devHandler) {
        super(config);
        this.refId = refId;
        this.config = config;
        this.originalHandler = originalHandler;
        this.devHandler = devHandler;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {
        FaceletHandlerHelper helper = new FaceletHandlerHelper(ctx, config);
        TagAttributes cAttrs = FaceletHandlerHelper.getTagAttributes(
                helper.createAttribute("id", helper.generateDevRegionId(refId)),
                helper.createAttribute("styleClass", "displayI nxlDevRegion"),
                helper.createAttribute("layout", "block"));
        TagAttributes devAttrs = FaceletHandlerHelper.getTagAttributes(
                helper.createAttribute("id",
                        helper.generateDevContainerId(refId)),
                helper.createAttribute("styleClass", "displayN nxlDevContainer"),
                helper.createAttribute("layout", "block"));
        ComponentHandler dComp = helper.getHtmlComponentHandler(
                config.getTagId(), devAttrs, devHandler, PANEL_COMPONENT_TYPE,
                null);
        FaceletHandler nextHandler = new CompositeFaceletHandler(
                new FaceletHandler[] { dComp, originalHandler });
        ComponentHandler cComp = helper.getHtmlComponentHandler(
                config.getTagId(), cAttrs, nextHandler, PANEL_COMPONENT_TYPE,
                null);
        cComp.apply(ctx, parent);
    }
}
