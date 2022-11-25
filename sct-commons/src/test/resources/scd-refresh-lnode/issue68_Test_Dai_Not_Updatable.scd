<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!-- SPDX-FileCopyrightText: 2022 RTE FRANCE -->
<!-- -->
<!-- SPDX-License-Identifier: Apache-2.0 -->
<SCL xmlns:compas="https://www.lfenergy.org/compas/extension/v1" xmlns="http://www.iec.ch/61850/2003/SCL" version="2007" revision="B" release="4">
	<Private type="COMPAS-SclFileType">
		<compas:SclFileType>SCD</compas:SclFileType>
	</Private>
	<Header id="9ff3be19-a353-4b9c-b71b-af133855336a" version="5" revision="A" toolID="COMPAS">
		<History>
			<Hitem version="5" revision="A" when="Tue Aug 23 10:37:48 CEST 2022" who="" what="" why="" />
		</History>
	</Header>
	<Substation name="SITE">
		<Private type="RTE-ExportSite">
			<rte:ExportSite xmlns:rte="http://www.rte-france.com" Indice="2" Label="PALLU" UUID="d66637d5-2387-409e-99c3-f3d67079013a" />
		</Private>
		<VoltageLevel nomFreq="50" numPhases="3" name="0">
			<Voltage unit="V" multiplier="k">0</Voltage>
			<Bay name="BAY_1">
				<Private type="COMPAS-Bay">
					<compas:Bay BayCodif="TG00000001" UUID="9cd6f05b-1bbd-4ba3-86c5-41c99103e06d" Version="1" MainShortLabel="SITE1" SecondLabel="SITE-TGENE" NumBay="7" />
				</Private>
				<Private type="COMPAS-Criteria">
					<compas:Criteria ObjectID="9cd6f05b-1bbd-4ba3-86c5-41c99103e06d" CriteriaAssociationID="CriteriaAssociationID" />
				</Private>
				<Function name="FUNCTION_1">
					<Private type="COMPAS-Function">
						<compas:Function UUID="8f4cda3f-828c-4006-9b87-2af96b19304b" Label="FUNCTION_1" />
					</Private>
					<LNode iedName="IedName1" ldInst="LDSUIED" lnClass="LLN0" lnInst="">
						<Private type="COMPAS-ICDHeader">
							<compas:ICDHeader ICDSystemVersionUUID="IED4d4fe1a8cda64cf88a5ee4176a1a0eed" IEDType="SAMU" IEDinstance="3" IEDName="IedName1" VendorName="RTE" IEDmodel="ICDfromModeling" IEDredundancy="B" BayLabel="SITE1" hwRev="01.00.00" swRev="01.00.00" headerId="cacfa2df-bdb2-47a7-b7ea-07916586ebdd" headerVersion="01.00.00" headerRevision="01.00.00"></compas:ICDHeader>
						</Private>
					</LNode>
				</Function>
			</Bay>
		</VoltageLevel>
	</Substation>
	<Communication>
		<SubNetwork type="8-MMS" name="RSPACE_PROCESS_NETWORK">
			<ConnectedAP iedName="IedName2" apName="PROCESS_AP">
				<Address>
					<P type="OSI-PSEL">00000001</P>
				</Address>
			</ConnectedAP>
			<ConnectedAP iedName="IedName1" apName="PROCESS_AP">
				<Address>
					<P type="OSI-PSEL">00000001</P>
				</Address>
			</ConnectedAP>
		</SubNetwork>
		<SubNetwork type="IP" name="RSPACE_ADMIN_NETWORK">
			<ConnectedAP iedName="IedName2" apName="ADMIN_AP">
				<Address>
					<P type="SyslogIP">Adresse IP du serveur Syslog</P>
				</Address>
			</ConnectedAP>
			<ConnectedAP iedName="IedName1" apName="ADMIN_AP">
				<Address>
					<P type="SyslogIP">Adresse IP du serveur Syslog</P>
				</Address>
			</ConnectedAP>
		</SubNetwork>
	</Communication>
	<IED name="IedName1" type="Valeur définie par le fournisseur" manufacturer="ADU" configVersion="ADU" originalSclVersion="2007" originalSclRevision="B" originalSclRelease="4" owner="ADU">
		<Private type="RTE-IEDType">SAMU</Private>
		<Private type="COMPAS-IEDType">SAMU</Private>
		<Private type="COMPAS-SystemVersion">
			<compas:SystemVersion MainSystemVersion="01.00" MinorSystemVersion="009.001.001" />
		</Private>
		<Private type="COMPAS-ICDHeader">
			<compas:ICDHeader ICDSystemVersionUUID="IED4d4fe1a8cda64cf88a5ee4176a1a0eed" IEDType="SAMU" IEDinstance="3" IEDName="IedName1" VendorName="RTE" IEDmodel="ICDfromModeling" IEDredundancy="B" BayLabel="SITE1" hwRev="01.00.00" swRev="01.00.00" headerId="cacfa2df-bdb2-47a7-b7ea-07916586ebdd" headerVersion="01.00.00" headerRevision="01.00.00"></compas:ICDHeader>
		</Private>
		<AccessPoint name="PROCESS_AP">
			<Server>
				<Authentication />
				<LDevice inst="LDSUIED" ldName="IedName1LDSUIED" desc="IED Monitoring Function">
					<Private type="COMPAS-LDevice">
						<compas:LDevice xmlns:compas="https://www.lfenergy.org/compas/extension/v1" LDeviceID="IedName1LDSUIED" LDeviceStatus="ACTIVE"/>
					</Private>
					<LN0 lnClass="LLN0" inst="" lnType="LNType1" desc="Logical node zero">
						<DOI name="NamPlt">
							<DAI name="swRev">
								<Val>01.00.000</Val>
							</DAI>
							<DAI name="configRev">
								<Val>01.00.000</Val>
							</DAI>
						</DOI>
						<DOI name="Mod">
							<DAI name="stVal">
								<Val>off</Val>
							</DAI>
						</DOI>
					</LN0>
				</LDevice>
			</Server>
		</AccessPoint>
		<AccessPoint name="ADMIN_AP" />
	</IED>

	<IED name="IedName2" type="Valeur définie par le fournisseur" manufacturer="ADU" configVersion="ADU" originalSclVersion="2007" originalSclRevision="B" originalSclRelease="4" owner="ADU">
		<Private type="RTE-IEDType">SAMU</Private>
		<Private type="COMPAS-IEDType">SAMU</Private>
		<Private type="COMPAS-SystemVersion">
			<compas:SystemVersion MainSystemVersion="01.00" MinorSystemVersion="009.001.001" />
		</Private>
		<Private type="COMPAS-ICDHeader">
			<compas:ICDHeader ICDSystemVersionUUID="IED4d4fe1a8cda64cf88a5ee4176a1a0eef" IEDType="SAMU" IEDinstance="3" IEDName="IedName2" VendorName="RTE" IEDmodel="ICDfromModeling" IEDredundancy="A" BayLabel="SITE1" hwRev="01.00.00" swRev="01.00.00" headerId="cacfa2df-bdb2-47a7-b7ea-07916586ebdd" headerVersion="01.00.00" headerRevision="01.00.00"></compas:ICDHeader>
		</Private>
		<AccessPoint name="PROCESS_AP">
			<Server>
				<Authentication />
				<LDevice inst="LDSUIED" ldName="IedName2LDSUIED" desc="IED Monitoring Function">
					<Private type="COMPAS-LDevice">
						<compas:LDevice xmlns:compas="https://www.lfenergy.org/compas/extension/v1" LDeviceID="IedName2LDSUIED" LDeviceStatus="INACTIVE"/>
					</Private>
					<LN0 lnClass="LLN0" inst="" lnType="LNType2" desc="Logical node zero">
						<DOI name="NamPlt">
							<DAI name="swRev">
								<Val>01.00.000</Val>
							</DAI>
							<DAI name="configRev">
								<Val>01.00.000</Val>
							</DAI>
						</DOI>
						<DOI name="Mod">
							<DAI name="stVal">
								<Val>on</Val>
							</DAI>
						</DOI>
					</LN0>
				</LDevice>
			</Server>
		</AccessPoint>
		<AccessPoint name="ADMIN_AP" />
	</IED>

	<IED name="IedName3" type="Valeur définie par le fournisseur" manufacturer="ADU" configVersion="ADU" originalSclVersion="2007" originalSclRevision="B" originalSclRelease="4" owner="ADU">
		<Private type="RTE-IEDType">SAMU</Private>
		<Private type="COMPAS-IEDType">SAMU</Private>
		<Private type="COMPAS-SystemVersion">
			<compas:SystemVersion MainSystemVersion="01.00" MinorSystemVersion="009.001.001" />
		</Private>
		<Private type="COMPAS-ICDHeader">
			<compas:ICDHeader ICDSystemVersionUUID="IED4d4fe1a8cda64cf88a5ee4176a1a0eef" IEDType="SAMU" IEDinstance="3" IEDName="IedName3" VendorName="RTE" IEDmodel="ICDfromModeling" IEDredundancy="A" BayLabel="SITE1" hwRev="01.00.00" swRev="01.00.00" headerId="cacfa2df-bdb2-47a7-b7ea-07916586ebdd" headerVersion="01.00.00" headerRevision="01.00.00"></compas:ICDHeader>
		</Private>
		<AccessPoint name="PROCESS_AP">
			<Server>
				<Authentication />
				<LDevice inst="LDSUIED" ldName="IedName3LDSUIED" desc="IED Monitoring Function">
					<Private type="COMPAS-LDevice">
						<compas:LDevice xmlns:compas="https://www.lfenergy.org/compas/extension/v1" LDeviceID="IedName3LDSUIED" LDeviceStatus="UNTESTED"/>
					</Private>
					<LN0 lnClass="LLN0" inst="" lnType="LNType3" desc="Logical node zero">
						<DOI name="NamPlt">
							<DAI name="swRev">
								<Val>01.00.000</Val>
							</DAI>
							<DAI name="configRev">
								<Val>01.00.000</Val>
							</DAI>
						</DOI>
					</LN0>
				</LDevice>
			</Server>
		</AccessPoint>
		<AccessPoint name="ADMIN_AP" />
	</IED>

	<DataTypeTemplates>
		<LNodeType lnClass="LLN0" id="LNType1">
			<DO name="NamPlt" type="DO1" transient="false" />
			<DO name="Mod" type="DO2" transient="false" />
			<DO name="Beh" type="DO2" transient="false"/>
		</LNodeType>
		<LNodeType lnClass="LLN0" id="LNType2">
			<DO name="NamPlt" type="DO1" transient="false" />
			<DO name="Mod" type="DO3" transient="false" />
			<DO name="Beh" type="DO3" transient="false"/>
		</LNodeType>
		<LNodeType lnClass="LLN0" id="LNType3">
			<DO name="NamPlt" type="DO1" transient="false" />
			<DO name="Mod" type="DO4" transient="false" />
			<DO name="Beh" type="DO4" transient="false"/>
		</LNodeType>
		<DOType cdc="LPL" id="DO1">
			<DA fc="DC" dchg="false" qchg="false" dupd="false" name="configRev" bType="VisString255" valKind="RO" valImport="false" />
			<DA fc="DC" dchg="false" qchg="false" dupd="false" name="swRev" bType="VisString255" valKind="RO" valImport="false" />
		</DOType>
		<DOType cdc="ENC" id="DO2">
			<DA fc="ST" dchg="true" qchg="false" dupd="false" name="stVal" bType="Enum" type="BehaviourModeKind" valImport="false" />
		</DOType>
		<DOType cdc="ENC" id="DO3">
			<DA fc="ST" dchg="true" qchg="false" dupd="false" name="stVal" bType="Enum" type="BehaviourModeKind2" valImport="false"/>
		</DOType>
		<DOType cdc="ENC" id="DO4">
			<DA fc="ST" dchg="true" qchg="false" dupd="false" name="stVal" bType="Enum" type="BehaviourModeKind3" valImport="false"/>
		</DOType>
		<EnumType id="BehaviourModeKind">
			<EnumVal ord="1">on</EnumVal>
			<EnumVal ord="2">off</EnumVal>
			<EnumVal ord="3">blocked</EnumVal>
			<EnumVal ord="4">test</EnumVal>
			<EnumVal ord="5">test/blocked</EnumVal>
		</EnumType>
		<EnumType id="BehaviourModeKind2">
			<EnumVal ord="1">on</EnumVal>
			<EnumVal ord="2">off</EnumVal>
			<EnumVal ord="3">blocked</EnumVal>
			<EnumVal ord="4">test</EnumVal>
			<EnumVal ord="5">test/blocked</EnumVal>
		</EnumType>
		<EnumType id="BehaviourModeKind3">
			<EnumVal ord="1">on</EnumVal>
			<EnumVal ord="2">off</EnumVal>
			<EnumVal ord="3">blocked</EnumVal>
			<EnumVal ord="4">test</EnumVal>
			<EnumVal ord="5">test/blocked</EnumVal>
		</EnumType>
	</DataTypeTemplates>
</SCL>
