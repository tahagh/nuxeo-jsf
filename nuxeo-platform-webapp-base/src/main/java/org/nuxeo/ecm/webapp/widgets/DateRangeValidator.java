/*
 * (C) Copyright 2012 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *    mcedica@nuxeo.com
 *
 * $Id$
 */
package org.nuxeo.ecm.webapp.widgets;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Compares two dates in a date rage widget and throws a validation error if the
 * second date it's not superior to the stratDate
 *
 * @since 5.7
 *
 */
@Name("dateRangeValidator")
@Scope(ScopeType.CONVERSATION)
public class DateRangeValidator implements Serializable {

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected Map<String, String> messages;

    private static final Log log = LogFactory.getLog(DateRangeValidator.class);

    public void validateDateRange(FacesContext context, UIComponent component,
            Object value) {
        Map<String, Object> attributes = component.getAttributes();
        String startDateComponentId = (String) attributes.get("startDateComponentId");
        String endDateComponentId = (String) attributes.get("endDateComponentId");
        if (startDateComponentId == null || endDateComponentId == null) {
            return;
        }

        UIInput startDateComp = (UIInput) component.findComponent(startDateComponentId);
        UIInput endDateComp = (UIInput) component.findComponent(endDateComponentId);
        if (startDateComp == null) {
            log.error("Can not find component with id " + startDateComponentId);
            return;
        }

        if (endDateComp == null) {
            log.error("Can not find component with id " + endDateComponentId);
            return;
        }
        Date stratDate = (Date) startDateComp.getLocalValue();
        Date endDate = (Date) endDateComp.getLocalValue();

        if (stratDate != null && endDate != null
                && endDate.compareTo(stratDate) < 0) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    String.format(
                            messages.get("error.dateRangeVlaidator.invalidDateRange"),
                            stratDate, endDate), null);
            throw new ValidatorException(message);
        }
    }
}