<?xml version="1.0" encoding="UTF-8"?>
<!-- SPDX-FileCopyrightText: 2024 RTE FRANCE -->
<!-- -->
<!-- SPDX-License-Identifier: Apache-2.0 -->

<SCL xmlns="http://www.iec.ch/61850/2003/SCL" xmlns:compas="https://www.lfenergy.org/compas/extension/v1" version="2007" revision="B" release="4">
    <Private xmlns="http://www.iec.ch/61850/2003/SCL" type="COMPAS-SclFileType">
        <compas:SclFileType>SCD</compas:SclFileType>
    </Private>
    <Header id="3F5B212AFD42F7F990742092EB237467" version="000012" revision="A" toolID="Compas"/>
    <Substation name="VENES">
        <VoltageLevel nomFreq="50" numPhases="3" name="4">
            <Voltage unit="V" multiplier="k">90</Voltage>
            <Bay name="4BUIS.1">
                <Function name="FUNCTION_1">
                    <LNode iedName="IED_NAME_1" ldInst="LDEVICE_1" lnClass="PDIS" lnInst="1">
                        <Private type="COMPAS-LNodeStatus">on</Private>
                    </LNode>
                    <LNode iedName="IED_NAME_1" ldInst="LDEVICE_1" lnClass="PDIS" lnInst="2">
                        <Private type="COMPAS-LNodeStatus">on</Private>
                    </LNode>
                    <LNode iedName="IED_NAME_1" ldInst="LDEVICE_1" lnClass="PDIS" lnInst="3">
                        <Private type="COMPAS-LNodeStatus">off</Private>
                    </LNode>
                    <LNode iedName="IED_NAME_1" ldInst="LDEVICE_1" lnClass="PDIS" lnInst="4">
                        <Private type="COMPAS-LNodeStatus">off</Private>
                    </LNode>
                </Function>
                <Function name="FUNCTION_2">
                    <LNode iedName="IED_NAME_1" ldInst="LDEVICE_1" lnClass="LLN0">
                        <Private type="COMPAS-LNodeStatus">on</Private>
                    </LNode>
                    <LNode iedName="IED_NAME_1" ldInst="LDEVICE_2" lnClass="LLN0">
                        <Private type="COMPAS-LNodeStatus">on</Private>
                    </LNode>
                    <LNode iedName="IED_NAME_1" ldInst="LDEVICE_3" lnClass="LLN0">
                        <Private type="COMPAS-LNodeStatus">off</Private>
                    </LNode>
                    <LNode iedName="IED_NAME_1" ldInst="LDEVICE_4" lnClass="LLN0">
                        <Private type="COMPAS-LNodeStatus">off</Private>
                    </LNode>
                </Function>
            </Bay>
        </VoltageLevel>
    </Substation>
    <IED name="IED_NAME_1">
        <AccessPoint name="PROCESS_AP">
            <Server>
                <Authentication/>
                <LDevice inst="LDEVICE_1">
                    <LN0 lnType="lnType1" lnClass="LLN0" inst="">
                        <Private type="COMPAS-LNodeStatus">on</Private>
                        <DOI name="Mod">
                            <DAI name="stVal"/>
                        </DOI>
                    </LN0>
                    <LN lnType="lnType2" lnClass="PDIS" inst="1">
                        <Private type="COMPAS-LNodeStatus">on;off</Private>
                        <DOI name="Mod">
                            <DAI name="stVal"/>
                        </DOI>
                    </LN>
                    <LN lnType="lnType2" lnClass="PDIS" inst="2">
                        <Private type="COMPAS-LNodeStatus">on</Private>
                        <DOI name="Mod">
                            <DAI name="stVal"/>
                        </DOI>
                    </LN>
                    <LN lnType="lnType2" lnClass="PDIS" inst="3">
                        <Private type="COMPAS-LNodeStatus">on;off</Private>
                        <DOI name="Mod">
                            <DAI name="stVal"/>
                        </DOI>
                    </LN>
                    <LN lnType="lnType2" lnClass="PDIS" inst="4">
                        <Private type="COMPAS-LNodeStatus">off</Private>
                        <DOI name="Mod">
                            <DAI name="stVal"/>
                        </DOI>
                    </LN>
                </LDevice>
                <LDevice inst="LDEVICE_2">
                    <LN0 lnType="lnType1" lnClass="LLN0" inst="">
                        <Private type="COMPAS-LNodeStatus">on</Private>
                        <DOI name="Mod">
                            <DAI name="stVal"/>
                        </DOI>
                    </LN0>
                </LDevice>
                <LDevice inst="LDEVICE_3">
                    <LN0 lnType="lnType1" lnClass="LLN0" inst="">
                        <Private type="COMPAS-LNodeStatus">on;off</Private>
                        <DOI name="Mod">
                            <DAI name="stVal"/>
                        </DOI>
                    </LN0>
                </LDevice>
                <LDevice inst="LDEVICE_4">
                    <LN0 lnType="lnType1" lnClass="LLN0" inst="">
                        <Private type="COMPAS-LNodeStatus">off</Private>
                        <DOI name="Mod">
                            <DAI name="stVal"/>
                        </DOI>
                    </LN0>
                </LDevice>
            </Server>
        </AccessPoint>
    </IED>
    <DataTypeTemplates>
        <LNodeType id="lnType1" lnClass="LLN0">
            <DO name="Mod" type="DO_Mod"/>
        </LNodeType>
        <LNodeType id="lnType2" lnClass="PDIS">
            <DO name="Mod" type="DO_Mod"/>
        </LNodeType>
        <DOType id="DO_Mod" cdc="ENC">
            <DA fc="ST" dchg="true" name="stVal" bType="Enum" type="DA_stVal"/>
        </DOType>
        <EnumType id="DA_stVal">
            <EnumVal ord="1">on</EnumVal>
            <EnumVal ord="2">off</EnumVal>
            <EnumVal ord="3">test</EnumVal>
        </EnumType>
    </DataTypeTemplates>
</SCL>
