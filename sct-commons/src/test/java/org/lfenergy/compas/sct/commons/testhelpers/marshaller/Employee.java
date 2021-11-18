// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers.marshaller;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         <element name="address">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="addressLine1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   <element name="addressLine2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   <element name="country" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   <element name="state" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   <element name="zip" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="assestsAllocated" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="id" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "address",
        "assestsAllocated",
        "id"
})
@XmlRootElement(name = "employee")
public class Employee {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected Employee.Address address;
    protected List assestsAllocated;
    protected byte id;

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the address property.
     *
     * @return
     *     possible object is
     *     {@link Employee.Address }
     *
     */
    public Employee.Address getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     *
     * @param value
     *     allowed object is
     *     {@link Employee.Address }
     *
     */
    public void setAddress(Employee.Address value) {
        this.address = value;
    }

    /**
     * Gets the value of the assestsAllocated property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <code>set</code> method for the assestsAllocated property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *    getAssestsAllocated().add(newItem);
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List getAssestsAllocated() {
        if (assestsAllocated == null) {
            assestsAllocated = new ArrayList();
        }
        return this.assestsAllocated;
    }

    /**
     * Gets the value of the id property.
     *
     */
    public byte getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     */
    public void setId(byte value) {
        this.id = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <sequence>
     *         <element name="addressLine1" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         <element name="addressLine2" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         <element name="country" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         <element name="state" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         <element name="zip" type="{http://www.w3.org/2001/XMLSchema}short"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "addressLine1",
            "addressLine2",
            "country",
            "state",
            "zip"
    })
    public static class Address {

        @XmlElement(required = true)
        protected String addressLine1;
        @XmlElement(required = true)
        protected String addressLine2;
        @XmlElement(required = true)
        protected String country;
        @XmlElement(required = true)
        protected String state;
        protected short zip;

        /**
         * Gets the value of the addressLine1 property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getAddressLine1() {
            return addressLine1;
        }

        /**
         * Sets the value of the addressLine1 property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setAddressLine1(String value) {
            this.addressLine1 = value;
        }

        /**
         * Gets the value of the addressLine2 property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getAddressLine2() {
            return addressLine2;
        }

        /**
         * Sets the value of the addressLine2 property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setAddressLine2(String value) {
            this.addressLine2 = value;
        }

        /**
         * Gets the value of the country property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getCountry() {
            return country;
        }

        /**
         * Sets the value of the country property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setCountry(String value) {
            this.country = value;
        }

        /**
         * Gets the value of the state property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getState() {
            return state;
        }

        /**
         * Sets the value of the state property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setState(String value) {
            this.state = value;
        }

        /**
         * Gets the value of the zip property.
         *
         */
        public short getZip() {
            return zip;
        }

        /**
         * Sets the value of the zip property.
         *
         */
        public void setZip(short value) {
            this.zip = value;
        }
    }

}
