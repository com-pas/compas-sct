// SPDX-FileCopyrightText: 2025 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.testhelpers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class ErrorHandlerCollector implements ErrorHandler {

    private final List<SAXParseException> exceptions = new ArrayList<>();

    @Override
    public void warning(SAXParseException ex) {
        exceptions.add(ex);
    }

    @Override
    public void error(SAXParseException ex) {
        exceptions.add(ex);
    }

    @Override
    public void fatalError(SAXParseException ex) {
        exceptions.add(ex);
    }

}
