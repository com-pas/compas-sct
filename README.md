# System Configuration Tool (SCT) components

Interested in contributing? Please read carefully the [CONTRIBUTING guidelines](https://github.com/com-pas/contributing/blob/master/CONTRIBUTING.md).

Step 1:

<img width="728" alt="Step1-SCD_Initialisation" src="https://user-images.githubusercontent.com/76168202/164449343-9b86db6a-e765-4b1d-9f6f-0c292bcceb9e.PNG">

For the SCD file constitution, see diagram above:

Creation of a new empty SCD file with Header ID and version and revision. Then the SCD file is populated by:
- import of the SSD file associated with a Site
- import of the STD files associated with the functions for a given system version. A system version defines a qualified set of ICD. And each ICD describes an IED for an IED version.
- suppression of all Control Blocks, DataSets and all ExtRef @srcXXX attributes
- edition of LNode @iedName attribute
- activation of used LDevice and desactivation of unused LDevice according to Function/LNode contained into /Substation

Step 2:

<img width="436" alt="Step2-Binding_in_current_SCD" src="https://user-images.githubusercontent.com/76168202/164449443-b9c5f125-3271-432c-8dd5-c30475d657ac.PNG">

For the binding of SCD, see diagram above:

- Removal of ExtRef @iedName @ldInst..... @daname and @srcXXX attributes for all inactive ExtRef or for all Extref which have inactive source or target
- for each active or untested ExtRef.desc with an existing Extref.iedName attribute and without compas:Flow@CriteriaAssociationID Private attribute, copy the right /IED.name into compas:Flow@ExtRefiedName and into Extref.iedName

Step 3:

<img width="791" alt="Step3-DataSet_and_CB_creation_then_ExtRef_update_in_current_SCD" src="https://user-images.githubusercontent.com/76168202/164449563-06e2f969-df34-4146-a83e-769e0d7142b9.PNG">

For the process of DataSet and Control Block creation and then Extref update, see diagram above:

- Extraction of all ExtRef attributes and properties
- keep only Extref signals which are:
    - Active or untested
    - AND external to their IED
    - AND ExtRef binding attributes are existing and populated
    - AND having DA@fc="ST" OR "MX"
- Then group the lines by ExtRef@pServT and @iedName and @ldInst and FlowKind property (and DA@fc for Report only)
- Each group of lines is used to vreate a new DataSet and Control Block
- Then for each ExtRef signal, create and populate ExtRef @srcXXX attributes

