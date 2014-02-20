/*
 * (C) Copyright 2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id: GenericHtmlComponentHandler.java 28610 2008-01-09 17:13:52Z sfermigier $
 */

package org.nuxeo.ecm.platform.ui.web.tag.handler;

import javax.faces.component.ValueHolder;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.MetaRuleset;

import org.nuxeo.ecm.platform.ui.web.tag.jsf.DefaultValueHolderRule;

import com.sun.faces.facelets.tag.jsf.html.HtmlComponentHandler;

/**
 * Generic HTML component handler.
 * <p>
 * Handler that manages a defaultValue attribute set on the tag, to default to
 * this value when value is null or empty. *
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public class GenericHtmlComponentHandler extends HtmlComponentHandler {

    public GenericHtmlComponentHandler(ComponentConfig config) {
        super(config);
    }

    /**
     * Create meta rule set as regular html component, adding a default value
     * holder rule.
     *
     * @see DefaultValueHolderRule
     */
    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        MetaRuleset m = super.createMetaRuleset(type);
        if (ValueHolder.class.isAssignableFrom(type)) {
            m.addRule(DefaultValueHolderRule.Instance);
        }
        return m;
    }

}
