<!--
SPDX-FileCopyrightText: 2021 Alliander N.V.

SPDX-License-Identifier: Apache-2.0
-->

[![Maven Build Github Action Status](<https://img.shields.io/github/workflow/status/com-pas/compas-sct/Build%20Project?logo=GitHub>)](https://github.com/com-pas/compas-sct/actions?query=workflow%3A%22Build+Project%22)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com-pas_compas-sct&metric=alert_status)](https://sonarcloud.io/dashboard?id=com-pas_compas-sct)
[![REUSE status](https://api.reuse.software/badge/github.com/com-pas/compas-sct)](https://api.reuse.software/info/github.com/com-pas/compas-sct)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/5925/badge)](https://bestpractices.coreinfrastructure.org/projects/5925)
[![Slack](https://raw.githubusercontent.com/com-pas/compas-architecture/master/public/LFEnergy-slack.svg)](http://lfenergy.slack.com/)

# System Configuration Tool (SCT) components
The SCT tool is a library for generating SCD (System Configuration Description) files based on IEC 61850 standard.
The code is written with Java (POJO) and is based on Chain of Responsability pattern.
It's architecture is modular and is composed by 3 modules (sct-app, sct-commons and sct-data).
The main feature is to generate a SCD file from SSD (Substation Specification Description) and STD (System Template definition) files.
+ ***sct-app*** : is the high level part and contains a service which allows automatic generation of the SCD file. This last 
calls sct-commons functions to realize features.
For further use of the SCT tool this part could be used to add end points or other monitoring tools to use efficiently the SCT. 
This will allow to dockerize easily the tool for more portal and large usage.
+ ***sct-commons*** : contains implementation of basic elements of SCLin low level methods and functions (middle level methods) to realize needed operations for them in order to allow
easy manipulation of SCL files.
+ ***sct-data*** : module which propose some interfaces to be implemented in order to interact with databases.

The main use case of the product is generation of SCD file (automatically or manually by calling low level functions). 
Perspectives are given to users to implement other use cases in coherence with the standard IEC-61850 as the SCT stands for a library for now.

For more informations about the project documentation (architecture, code documentation, etc), please refer to [Documentation](https://com-pas.github.io/compas-sct/) 

Interested in contributing? Please read carefully the [CONTRIBUTING guidelines](https://github.com/com-pas/contributing/blob/master/CONTRIBUTING.md).
