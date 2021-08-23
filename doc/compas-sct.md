<!-- SPDX-FileCopyrightText: 2020 RTE FRANCE -->
<!-- -->
<!-- SPDX-License-Identifier: Apache-2.0 -->
# COMPAS SCT (Substation Configuration Tool)
## Introduction


System Configuration Tool (SCT) is part of CoMPAS open source project. It goal is to bring a flexible and adaptative tool for configuring PACS (Power Automation and power Control System). It's an n-tiers architecture which combine reliabilitily, flexibility, modularity and adaptability to allows users to choose their own database to implement the SCT.

The following architecture is divided in three major parts:
* sct-commons : ....
* sct-service : this part could be considered as the engine of the SCT. It computes all needed operations and uses sct-data for database access.
* sct-data : implements a generic abstract data service which should be extended by specific real data service for exchange with chosen database.
* sct-app : *TODO*

![Package Diagram](images/PackageDiagram-CompasSCT.png)


## SCT COMMONS
*TODO*
## SCT DATA
*TODO*
### SQL-Like Database
*TODO*
### NoSQL-Like Database
*TODO*
## SCT SERVICE  
*TODO*
## SCT APPLICATION
*TODO*