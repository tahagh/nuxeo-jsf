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
 * $Id$
 */

package org.nuxeo.ecm.platform.ui.web.tag.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributes;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.commons.lang.StringUtils;

import com.sun.faces.facelets.tag.TagAttributeImpl;
import com.sun.faces.facelets.tag.TagAttributesImpl;
import com.sun.faces.facelets.tag.jstl.core.ForEachHandler;

/**
 * Repeat handler, similar to the standard ForEach handler.
 * <p>
 * This component encapsulates a c:forEach tag inside a nxu:set tag, to be able
 * to control when the sub-components should be re-created in the case of an
 * ajax re-render.
 *
 * @author Anahide Tchertchian
 */
public class RepeatTagHandler extends TagHandler {

    protected static final DataModel EMPTY_MODEL = new ListDataModel(
            Collections.emptyList());

    /**
     * @since 5.7
     */
    protected static final String ITERATION_VAR_PREFIX = "nxuRepeat_";

    /**
     * @since 5.7
     */
    protected final TagConfig config;

    /**
     * @deprecated, user {@link #items} instead
     */
    @Deprecated
    protected final TagAttribute value;

    /**
     * @since 5.7
     */
    protected final TagAttribute items;

    /**
     * @since 5.7
     */
    protected final TagAttribute itemsId;

    protected final TagAttribute var;

    protected final TagAttribute index;

    /**
     * @since 5.7
     */
    protected final TagAttribute status;

    /**
     * @since 5.7
     */
    protected final TagAttribute begin;

    /**
     * @since 5.7
     */
    protected final TagAttribute end;

    /**
     * @since 5.7
     */
    protected final TagAttribute step;

    /**
     * @since 5.7
     */
    protected final TagAttribute tranzient;

    /**
     * @since 5.7
     */
    protected final TagAttribute varStatus;

    public RepeatTagHandler(TagConfig config) {
        super(config);
        this.config = config;
        items = getAttribute("items");
        itemsId = getAttribute("itemsId");
        value = getAttribute("value");
        var = getAttribute("var");
        index = getAttribute("index");
        status = getAttribute("status");
        begin = getAttribute("begin");
        end = getAttribute("end");
        step = getAttribute("step");
        tranzient = getAttribute("transient");
        varStatus = getAttribute("varStatus");
    }

    protected TagAttribute getItemsAttribute() {
        TagAttribute itemsAttr = items;
        if (items == null) {
            // BBB
            itemsAttr = value;
        }
        return itemsAttr;
    }

    protected String getTagConfigId(FaceletContext ctx) {
        String refId = null;
        if (itemsId != null) {
            refId = itemsId.getValue(ctx);
        }

        if (StringUtils.isBlank(refId)) {
            TagAttribute itemsAttr = getItemsAttribute();
            Object val = null;
            if (itemsAttr != null) {
                val = itemsAttr.getObject(ctx);
                if (val != null) {
                    refId = val.toString();
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        if (refId != null) {
            builder.append(refId);
        }
        builder.append(";");

        Integer intValue = new Integer(builder.toString().hashCode());
        return intValue.toString();
    }

    /**
     * Encapsulate the call to a c:forEach tag in an SetTagHandler exposing the
     * value and making sure the tagConfigId changes when the value changes
     * (see NXP-11434)
     */
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {
        FaceletHandler nextHandler = this.nextHandler;
        TagAttribute varStatusAttr = varStatus;
        if (index != null) {
            // wrap the next handler in a set tag handler to expose the index
            // value from the varStatus attribute.
            String indexValue = index.getValue(ctx);
            if (!StringUtils.isBlank(indexValue)) {
                String varStatusValue = varStatus != null ? varStatus.getValue(ctx)
                        : null;
                if (StringUtils.isBlank(varStatusValue)) {
                    // need to create and set it as an attribute for the index
                    // to be exposed
                    varStatusAttr = createAttribute(this.config, "varStatus",
                            getVarName(String.format("%s_%s", indexValue,
                                    "varStatus")));
                } else {
                    varStatusAttr = createAttribute(this.config, "varStatus",
                            varStatusValue);
                }
                TagAttributes indexVarAttrs = new TagAttributesImpl(
                        new TagAttribute[] {
                                createAttribute(this.config, "var", indexValue),
                                createAttribute(this.config, "value",
                                        String.format("#{%s.index}",
                                                varStatusAttr.getValue())) });
                TagConfig indexVarConfig = TagConfigFactory.createTagConfig(
                        this.config, this.tagId, indexVarAttrs,
                        this.nextHandler);
                nextHandler = new SetTagHandler(indexVarConfig);
            }
        }

        List<TagAttribute> forEachAttrs = new ArrayList<TagAttribute>();
        forEachAttrs.add(createAttribute(this.config, "items",
                String.format("#{%s}", getVarName("items"))));
        forEachAttrs.addAll(copyAttributes(this.config, var, begin, end, step,
                varStatusAttr, tranzient));
        TagConfig forEachConfig = TagConfigFactory.createTagConfig(
                this.config,
                this.tagId,
                new TagAttributesImpl(
                        forEachAttrs.toArray(new TagAttribute[] {})),
                nextHandler);
        ForEachHandler forEachHandler = new ForEachHandler(forEachConfig);

        String setTagConfigId = getTagConfigId(ctx);
        TagAttribute itemsAttr = getItemsAttribute();
        TagAttributes aliasAttrs = new TagAttributesImpl(new TagAttribute[] {
                createAttribute(this.config, "var", getVarName("items")),
                createAttribute(this.config, "value",
                        itemsAttr != null ? itemsAttr.getValue() : null),
                createAttribute(this.config, "cache", "true") });
        TagConfig aliasConfig = TagConfigFactory.createTagConfig(this.config,
                setTagConfigId, aliasAttrs, forEachHandler);
        FaceletHandler handler = new SetTagHandler(aliasConfig);

        // apply
        handler.apply(ctx, parent);
    }

    protected String getVarName(String id) {
        return String.format("%s%s", ITERATION_VAR_PREFIX, id);
    }

    protected TagAttribute createAttribute(TagConfig tagConfig, String name,
            String value) {
        return new TagAttributeImpl(tagConfig.getTag().getLocation(), "", name,
                name, value);
    }

    protected TagAttribute copyAttribute(TagConfig tagConfig,
            TagAttribute attribute) {
        return new TagAttributeImpl(tagConfig.getTag().getLocation(), "",
                attribute.getLocalName(), attribute.getLocalName(),
                attribute.getValue());
    }

    protected List<TagAttribute> copyAttributes(TagConfig tagConfig,
            TagAttribute... attributes) {
        List<TagAttribute> res = new ArrayList<TagAttribute>();
        if (attributes != null) {
            for (TagAttribute attr : attributes) {
                if (attr != null) {
                    res.add(copyAttribute(tagConfig, attr));
                }
            }
        }
        return res;
    }

}
