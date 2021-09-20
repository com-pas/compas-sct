<!-- SPDX-FileCopyrightText: 2021 RTE FRANCE -->
<!-- -->
<!-- SPDX-License-Identifier: Apache-2.0 -->
# COMPAS SCT (Substation Configuration Tool)
## Introduction

The CoMPAS SCT (System Configuration Tool) is part of the CoMPAS (Configuration Module for Power System Automation)
ecosystem which is an open source project aimed at providing a tool for configuring control system and 
profile management related to the 61850 standard. Its architecture allows an easy integration with the other 
components of CoMPAS, in addition to being modular and flexible with a high level of abstraction, it gives 
the freedom to implement the tool with the database of its choice.

The below package diagram shows different part of the tool architecture. 

![Package Diagram](images/PackageDiagram-CompasSCT.png)

Hence, we can distinguish four major parts:

* **[sct-commons](#SCT-COMMONS)** : a library that contents shared functionalities for the bound SCL object.
* **[sct-service](#SCT-SERVICE)** : It computes all needed operations and uses sct-data for database access.
* **[sct-data](#SCT-DATA)** : It holds data models and database connectivity services.
* **[sct-app](#SCT-APPLICATION)** : *TODO*.

## SCT COMMONS
This package holds a light weight and configurable XML binding tool based on the JAXB utilities, and set of bound SCL 
objects adapter. Actually the JAXB generated SCL objects can only be read through from the parent tag to child tag. That can be very limiting.
The adapter concept allows:
* navigating in all direction (upward, downward)
* more flexible manipulation of the JAXB SCL object
* considering specific algorithm based on SCL version

The SCT services specification of the norm IEC 61850  will be implemented in this package.

The Approach behind the SCL adapter is to complete the navigation provided by the JAXB tool, by adding 
functionalities that allow the browsing upward (from child to any ancestor). The conception is based on the 
abstraction defined below :

    public abstract class SclElementAdapter<P extends SclElementAdapter, T> {
        protected P parentAdapter;
        protected T currentElem;
    
        public SclElementAdapter(P parentAdapter) {
            this.parentAdapter = parentAdapter;
        }
    
        public SclElementAdapter(P parentAdapter, T currentElem) {
            this.parentAdapter = parentAdapter;
            setCurrentElem(currentElem);
        }
    
        public final void setCurrentElem(T currentElem){
            Assert.isTrue(amChildElementRef(currentElem),"No relation between SCL parent and child elements");
            this.currentElem = currentElem;
        }
    
        protected abstract boolean amChildElementRef(T sclElement);
    }

The root element adapter (entry point) is special as it does not have any parent adapter, hence, its method `amChildElementRef(T)` 
should always return `true`:

    public class SclRootAdapter extends SclElementAdapter<SclRootAdapter, SCL>{
        private version;
        private revision;
        private release;
    
        public SclRootAdapter(SCL currentElem) {
            super(null, currentElem);
            //set version, release & revision
        }
    
        public SclRootAdapter(String hId, String hVersion, String hRevision){
            super(null);
            this.currentElem = initialize(hId,hVersion,hRevision);
        }
    
        @Override
        protected boolean amChildElementRef(SCL sclElement) {
            return true;
        }

        [...]
    }



## SCT DATA
Data models and connectivity to database are defined here. Data access layer is an abstract layer that defined connectivity
interfaces. This layer manages a database with single table (SQL-Like database) or single collection (NoSQL-Like database).
The concrete data access layers are implemented in specific packages

* ### SQL-Like Database
An implementation of the sct-data connectivity interface with custom data models. This allows the application to work with sql-like database.
The libraries ares use for SQL-Like databases, those that support XML type (PostgreSql, Oracle, etc)

* ### NoSQL-Like Database
Like SQL-like part, this package contains the sct-data connector interfaces implementation for NoSQL-Like databases (BaseX, existDB, etc ) 
that support XML processing

## SCT SERVICE
This module implements all needed specification as functions (methods in Java). As shown in package diagram, 
it interacts with sct-data (database access) and sct-commons (delegate SCL manipulation).
## SCT APPLICATION
**TODO**