<!-- SPDX-FileCopyrightText: 2022 RTE FRANCE -->
<!-- -->
<!-- SPDX-License-Identifier: Apache-2.0 -->
# CoMPAS SCT (Substation Configuration Tool)
## Quick Start
----------------
This page will help you get started with our Services

> CoMPAS SCT offers an SCL services related to **IEC 61850 model implementation**

> Note: scl2007b4 and scl-extension (CoMPAS Core Modules) are fully integrated into CoMPAS SCT Project.

#### 1. Install CoMPAS SCT
First you need to add following dependencies to the pom.xml of your repository.
```xml
<!-- SclService -->
<dependency>
    <groupId>org.lfenergy.compas</groupId>
    <artifactId>sct-commons</artifactId>
    <version>0.1.0</version>
</dependency>
<!-- SclAutomationService -->
<dependency>
    <groupId>org.lfenergy.compas</groupId>
    <artifactId>sct-app</artifactId>
    <version>0.1.0</version>
</dependency>
```
Actually there are 4 packages available:
- compas-sct
- sct-commons
- sct-app
- sct-data


#### 2. Usage
Now that you have your **compas-sct** dependency set, you can start communicating with Commons SCT services.

**sct-commons** provides a collection of services to help you build SCL files for a range of use cases.
Following services provides needed functions compliant with IEC 61850 :

1. **SclService**
2. **SubstationService**

Let's start with a simple **SclService** call

```java
var scl = SclService.initScl(Optional.empty(), "1.0", "1.0");
marshaller.marshal(scl.getCurrentElem(), System.out);
```

When the command completes, it prints XML output representing the basic
details for **SCL**. Its structure resembles the following:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<SCL xmlns:compas="https://www.lfenergy.org/compas/extension/v1" xmlns="http://www.iec.ch/61850/2003/SCL" version="2007" revision="B" release="4">
    <Private type="COMPAS-SclFileType">
        <compas:SclFileType>SCD</compas:SclFileType>
    </Private>
    <Header id="2c1573a2-f8cf-4976-86b5-c58de9e05021" version="1.0" revision="1.0" toolID="COMPAS"/>
</SCL>
```

Nice work! You've successfully sent a request to Commons SCT service for
initialization of SCD file, you can find this example under <u>docs/example/example-app</u>.

But the QuickStart doesn't end there.
After having the reader of the SCL object.
Developers could realize different operations on SCL file as importing Substation from SSD file or IED from STD file,
updating Binding information, listing all DAIs information or updating them, etc.

#### 3. More on CoMPAS SCT operations (Automation features)
We can chain together different SCT services :
**SclAutomationService** provides a simple way to learn the Commons service.

Start it by using existing files of type `SSD` and `STD` and 
running the following :

```java
// ssd : SSD 
// std : STD 
HeaderDTO headerDTO = new HeaderDTO(UUID.randomUUID(), "1.0", "1.0");
var scl = SclAutomationService.createSCD(ssd, headerDTO, Set.of(std));
marshaller.marshal(scl.getCurrentElem(), System.out);
```
When the command completes, it prints XML output representing completed **SCL** file.
Its structure resembles the following:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<SCL xmlns:compas="https://www.lfenergy.org/compas/extension/v1" xmlns="http://www.iec.ch/61850/2003/SCL" version="2007" revision="B" release="4">
    <Private type="COMPAS-SclFileType">
        <compas:SclFileType>SCD</compas:SclFileType>
    </Private>
    <Header id="36576d4c-5b84-4975-9ec1-f1e28ce710a4" version="1.0" revision="1.0" toolID="COMPAS"/>
    <Substation>
        ...
    </Substation>
    <Communication>
        ...
    </Communication>
    <IED>
        ...
    </IED>
    <DataTypeTemplates>
        ...
    </DataTypeTemplates>
</SCL>
```
----------------
> SCD can be created from Commons SCT services
> (such as SclService) or by calling Adapters class (such as SclRootAdapter) or
> by using SclAutomationService.