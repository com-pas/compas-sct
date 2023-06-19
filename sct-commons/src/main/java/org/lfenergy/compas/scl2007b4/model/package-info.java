// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

@XmlSchema(
        namespace = "http://www.iec.ch/61850/2003/SCL",
        xmlns = {
                @XmlNs(namespaceURI = "http://www.iec.ch/61850/2003/SCL", prefix = ""),
                @XmlNs(namespaceURI = "https://www.lfenergy.org/compas/extension/v1", prefix = "compas")
        },
        elementFormDefault = XmlNsForm.QUALIFIED)


package org.lfenergy.compas.scl2007b4.model;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

/* This file is used by the Marshaller to set prefix "compas" for Compas Privates when marshalling JAXB objects */
