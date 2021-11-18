// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers.marshaller;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.javacodegeeks.examples.xjc package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.javacodegeeks.examples.xjc
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Employee }
     *
     */
    public Employee createEmployee() {
        return new Employee();
    }

    /**
     * Create an instance of {@link Employee.Address }
     *
     */
    public Employee.Address createEmployeeAddress() {
        return new Employee.Address();
    }

}
