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

/* This file is used by the Marshaller to set prefix "compas" for Compas Privates when marshalling JAXB objects */

import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;