/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * $Id: LayoutRowWidgetTagHandler.java 30553 2008-02-24 15:51:31Z atchertchian $
 */

package org.nuxeo.ecm.platform.forms.layout.facelets;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutRow;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;

/**
 * Layout widget recursion tag handler.
 * <p>
 * Iterates over a layout row widgets and apply next handlers as many times as
 * needed.
 * <p>
 * Only works when used inside a tag using the {@link LayoutRowTagHandler}.
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public class LayoutRowWidgetTagHandler extends TagHandler {

    private static final Log log = LogFactory.getLog(LayoutRowWidgetTagHandler.class);

    protected final TagConfig config;

    public LayoutRowWidgetTagHandler(TagConfig config) {
        super(config);
        this.config = config;
    }

    /**
     * For each widget in current row, exposes widget variables and applies
     * next handler.
     * <p>
     * Needs row to be exposed in context, so works in conjunction with
     * {@link LayoutRowTagHandler}.
     * <p>
     * Widget variables exposed: {@link RenderVariables.widgetVariables#widget}
     * , same variable suffixed with "_n" where n is the widget level, and
     * {@link RenderVariables.widgetVariables#widgetIndex}.
     */
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {

        // resolve widgets from row in context
        LayoutRow row = null;
        String rowVariableName = RenderVariables.rowVariables.layoutRow.name();
        FaceletHandlerHelper helper = new FaceletHandlerHelper(ctx, config);
        TagAttribute rowAttribute = helper.createAttribute(rowVariableName,
                String.format("#{%s}", rowVariableName));
        if (rowAttribute != null) {
            row = (LayoutRow) rowAttribute.getObject(ctx, LayoutRow.class);
        }
        if (row == null) {
            log.error("Could not resolve layout row " + rowAttribute);
            return;
        }

        Widget[] widgets = row.getWidgets();
        if (widgets == null || widgets.length == 0) {
            return;
        }

        int widgetCounter = 0;
        for (Widget widget : widgets) {
            // set unique id on widget and sub widgets before exposing them to
            // the context
            WidgetTagHandler.generateWidgetIdsRecursive(helper, widget);

            // expose widget variables
            Map<String, ValueExpression> variables = new HashMap<String, ValueExpression>();
            ExpressionFactory eFactory = ctx.getExpressionFactory();
            ValueExpression widgetVe = eFactory.createValueExpression(widget,
                    Widget.class);
            variables.put(RenderVariables.widgetVariables.widget.name(),
                    widgetVe);
            Integer level = null;
            String tagConfigId = null;
            if (widget != null) {
                level = Integer.valueOf(widget.getLevel());
                tagConfigId = widget.getTagConfigId();
            }
            variables.put(String.format("%s_%s",
                    RenderVariables.widgetVariables.widget.name(), level),
                    widgetVe);
            ValueExpression widgetIndexVe = eFactory.createValueExpression(
                    Integer.valueOf(widgetCounter), Integer.class);
            variables.put(RenderVariables.widgetVariables.widgetIndex.name(),
                    widgetIndexVe);
            variables.put(String.format("%s_%s",
                    RenderVariables.widgetVariables.widgetIndex.name(), level),
                    widgetIndexVe);

            // XXX: expose widget controls too, need to figure out
            // why controls cannot be references to widget.controls like
            // properties are in TemplateWidgetTypeHandler
            if (widget != null) {
                for (Map.Entry<String, Serializable> ctrl : widget.getControls().entrySet()) {
                    String key = ctrl.getKey();
                    String name = String.format(
                            "%s_%s",
                            RenderVariables.widgetVariables.widgetControl.name(),
                            key);
                    Serializable value = ctrl.getValue();
                    variables.put(name,
                            eFactory.createValueExpression(value, Object.class));
                }
            }

            List<String> blockedPatterns = new ArrayList<String>();
            blockedPatterns.add(RenderVariables.widgetVariables.widget.name()
                    + "*");
            blockedPatterns.add(RenderVariables.widgetVariables.widgetIndex.name()
                    + "*");
            blockedPatterns.add(RenderVariables.widgetVariables.widgetControl.name()
                    + "_*");

            FaceletHandler handler = helper.getAliasTagHandler(tagConfigId,
                    variables, blockedPatterns, nextHandler);

            // apply
            handler.apply(ctx, parent);
            widgetCounter++;
        }
    }
}
