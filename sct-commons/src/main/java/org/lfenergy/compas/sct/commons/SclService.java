// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.DataTypeTemplateReader;
import org.lfenergy.compas.sct.commons.api.SclEditor;
import org.lfenergy.compas.sct.commons.domain.DaVal;
import org.lfenergy.compas.sct.commons.domain.DataRef;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;
import org.lfenergy.compas.sct.commons.scl.com.SubNetworkAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.LNodeTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;
import org.lfenergy.compas.sct.commons.scl.icd.IcdHeader;
import org.lfenergy.compas.sct.commons.scl.ied.DAITracker;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.util.MonitoringLnClassEnum;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.IED_TEST_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.LDEVICE_LDSUIED;
import static org.lfenergy.compas.sct.commons.util.PrivateEnum.COMPAS_ICDHEADER;
import static org.lfenergy.compas.sct.commons.util.Utils.copyLn;

@Slf4j
@RequiredArgsConstructor
public class SclService implements SclEditor {

    private static final String DO_GOCBREF = "GoCBRef";
    private static final String DO_SVCBREF = "SvCBRef";
    private static final String DA_SETSRCREF = "setSrcRef";

    private final IedService iedService;
    private final LdeviceService ldeviceService;
    private final LnService lnService;
    private final ExtRefReaderService extRefReaderService;
    private final DataTypeTemplateReader dataTypeTemplateService;

    @Getter
    private final ThreadLocal<List<SclReportItem>> errorHandler = ThreadLocal.withInitial(ArrayList::new);

    @Override
    public SCL initScl(final UUID hId, final String hVersion, final String hRevision) throws ScdException {
        SclRootAdapter scdAdapter = new SclRootAdapter(hId.toString(), hVersion, hRevision);
        scdAdapter.addPrivate(PrivateUtils.createPrivate(TCompasSclFileType.SCD));
        return scdAdapter.getCurrentElem();
    }

    @Override
    public void addHistoryItem(SCL scd, String who, String what, String why) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        headerAdapter.addHistoryItem(who, what, why);
    }

    @Override
    public void updateHeader(@NonNull SCL scd, @NonNull HeaderDTO headerDTO) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        boolean hUpdated = false;
        String hVersion = headerDTO.getVersion();
        String hRevision = headerDTO.getRevision();
        if (hVersion != null && !hVersion.equals(headerAdapter.getHeaderVersion())) {
            headerAdapter.updateVersion(hVersion);
            hUpdated = true;
        }
        if (hRevision != null && !hRevision.equals(headerAdapter.getHeaderRevision())) {
            headerAdapter.updateRevision(hRevision);
            hUpdated = true;
        }
        if (hUpdated && !headerDTO.getHistoryItems().isEmpty()) {
            headerAdapter.addHistoryItem(
                    headerDTO.getHistoryItems().get(0).getWho(),
                    headerDTO.getHistoryItems().get(0).getWhat(),
                    headerDTO.getHistoryItems().get(0).getWhy()
            );
        }
    }

    @Override
    public void addIED(SCL scd, String iedName, SCL icd) throws ScdException {
         new SclRootAdapter(scd).addIED(icd, iedName);
    }

    @Override
    public void addSubnetworks(SCL scd, List<SubNetworkDTO> subNetworks, SCL icd) throws ScdException {
        if (!subNetworks.isEmpty()) {
            CommunicationAdapter communicationAdapter = new SclRootAdapter(scd).getCommunicationAdapter(true);
            for (SubNetworkDTO subNetworkDTO : subNetworks) {
                String snName = subNetworkDTO.getName();
                String snType = subNetworkDTO.getType();
                for (ConnectedApDTO accessPoint : subNetworkDTO.getConnectedAPs()) {
                    String iedName = accessPoint.iedName();
                    String apName = accessPoint.apName();
                    communicationAdapter.addSubnetwork(snName, snType, iedName, apName);

                    Optional<SubNetworkAdapter> subNetworkAdapter = communicationAdapter.getSubnetworkByName(snName);
                    if (subNetworkAdapter.isPresent()) {
                        ConnectedAPAdapter connectedAPAdapter = subNetworkAdapter.get().getConnectedAPAdapter(iedName, apName);
                        connectedAPAdapter.copyAddressAndPhysConnFromIcd(icd);
                    }
                }
            }
        }
    }

    @Override
    public void addSubnetworks(SCL scd, SCL icd, String iedName) throws ScdException {
        Optional.ofNullable(icd.getCommunication()).ifPresent(tCommunication ->
                tCommunication.getSubNetwork().forEach(icdSubNetwork ->
                        icdSubNetwork.getConnectedAP().forEach(icdConnectedAP -> {
                            // init Communication if not exist
                            CommunicationAdapter communicationAdapter = new SclRootAdapter(scd).getCommunicationAdapter(true);
                            // add SubNetwork if not exist, add ConnectedAP to SubNetwork if not exist
                            SubNetworkAdapter subNetworkAdapter = communicationAdapter
                                    .addSubnetwork(icdSubNetwork.getName(), icdSubNetwork.getType(), iedName, icdConnectedAP.getApName());
                            // copy Address And PhysConn From Icd to Scd
                            subNetworkAdapter.getConnectedAPAdapter(iedName, icdConnectedAP.getApName())
                                    .copyAddressAndPhysConnFromIcd(icd);
                        })
                ));
    }

    @Override
    public void updateDAI(SCL scd, String iedName, String ldInst, DataAttributeRef dataAttributeRef) throws ScdException {
        long startTime = System.nanoTime();
        log.info(Utils.entering());
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        DataTypeTemplateAdapter dttAdapter = sclRootAdapter.getDataTypeTemplateAdapter();
        LNodeTypeAdapter lNodeTypeAdapter = dttAdapter.getLNodeTypeAdapterById(dataAttributeRef.getLnType())
                .orElseThrow(() -> new ScdException("Unknown LNodeType : " + dataAttributeRef.getLnType()));
        lNodeTypeAdapter.checkDoAndDaTypeName(dataAttributeRef.getDoName(), dataAttributeRef.getDaName());

        if (TPredefinedBasicTypeEnum.OBJ_REF == dataAttributeRef.getBType()) {
            Long sGroup = dataAttributeRef.getDaName().getDaiValues().keySet().stream().findFirst().orElse(-1L);
            String val = sGroup < 0 ? null : dataAttributeRef.getDaName().getDaiValues().get(sGroup);
            sclRootAdapter.checkObjRef(val);
        }

        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.findLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(() -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName)));

        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(dataAttributeRef.getLnClass())
                .withLnInst(dataAttributeRef.getLnInst())
                .withLnPrefix(dataAttributeRef.getPrefix())
                .build();

        if (TPredefinedCDCEnum.ING == dataAttributeRef.getCdc() || TPredefinedCDCEnum.ASG == dataAttributeRef.getCdc()) {
            DAITracker daiTracker = new DAITracker(lnAdapter, dataAttributeRef.getDoName(), dataAttributeRef.getDaName());
            daiTracker.validateBoundedDAI();
        }
        lnAdapter.updateDAI(dataAttributeRef);
        log.info(Utils.leaving(startTime));
    }

    @Override
    public void importSTDElementsInSCD(SCL scd, List<SCL> stds) throws ScdException {

        //Check SCD and STD compatibilities
        Map<String, PrivateLinkedToStds> mapICDSystemVersionUuidAndSTDFile = PrivateUtils.createMapICDSystemVersionUuidAndSTDFile(stds);
        PrivateUtils.checkSTDCorrespondanceWithLNodeCompasICDHeader(mapICDSystemVersionUuidAndSTDFile);
        // List all Private and remove duplicated one with same iedName
        // For each Private.ICDSystemVersionUUID and Private.iedName find STD File
        List<String> iedNamesUsed = new ArrayList<>();
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        PrivateUtils.streamIcdHeaders(scd)
                .forEach(icdHeader -> {
                    if (!iedNamesUsed.contains(icdHeader.getIedName())) {
                        String iedName = icdHeader.getIedName();
                        iedNamesUsed.add(iedName);
                        String icdSysVerUuid = icdHeader.getIcdSystemVersionUUID();
                        if (!mapICDSystemVersionUuidAndSTDFile.containsKey(icdSysVerUuid))
                            throw new ScdException("There is no STD file found corresponding to " + icdHeader);
                        // import /ied /dtt in Scd
                        SCL std = mapICDSystemVersionUuidAndSTDFile.get(icdSysVerUuid).stdList().get(0);
                        SclRootAdapter stdRootAdapter = new SclRootAdapter(std);
                        IEDAdapter stdIedAdapter = new IEDAdapter(stdRootAdapter, std.getIED().get(0));
                        Optional<TPrivate> optionalTPrivate = stdIedAdapter.getPrivateHeader(COMPAS_ICDHEADER.getPrivateType());
                        if (optionalTPrivate.isPresent() && optionalTPrivate.flatMap(PrivateUtils::extractCompasICDHeader).map(IcdHeader::new).get().equals(icdHeader)) {
                            PrivateUtils.copyCompasICDHeaderFromLNodePrivateIntoSTDPrivate(optionalTPrivate.get(), icdHeader.toTCompasICDHeader());
                        } else throw new ScdException("COMPAS-ICDHeader is not the same in Substation and in IED");
                        scdRootAdapter.addIED(std, iedName);

                        //import connectedAP and rename ConnectedAP/@iedName
                        addSubnetworks(scdRootAdapter.getCurrentElem(), std, iedName);
                    }
                });
    }

    @Override
    public List<SclReportItem> updateDoInRef(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters)
                .map(LDeviceAdapter::getLN0Adapter)
                .map(LN0Adapter::updateDoInRef)
                .flatMap(List::stream)
                .toList();
    }


    @Override
    public List<SclReportItem> manageMonitoringLns(SCL scd) {
        errorHandler.get().clear();
        try {
            //Preprocessing : clean LSVS/LGOS if inst!=1 and monitor them if needed
            removeLsvsLgos(scd);
            iedService.getFilteredIeds(scd, ied -> !ied.getName().contains(IED_TEST_NAME))
                    .forEach(tied -> {
                        Map<TServiceType, List<IedSource>> serviceTypeToIedSource = ldeviceService.getLdevices(tied)
                                .flatMap(tlDevice -> extRefReaderService.getExtRefs(tlDevice.getLN0()))
                                .filter(tExtRef -> tExtRef.isSetServiceType() && tExtRef.isSetSrcCBName() && (tExtRef.getServiceType().equals(TServiceType.GOOSE) || tExtRef.getServiceType().equals(TServiceType.SMV)))
                                .collect(Collectors.groupingBy(tExtRef -> new IedSource(tExtRef.getIedName(), tExtRef.getSrcCBName(), tExtRef.getSrcLDInst(), tExtRef.getServiceType())))
                                .keySet()
                                .stream()
                                .collect(Collectors.groupingBy(IedSource::serviceType));
                        ldeviceService.findLdevice(tied, LDEVICE_LDSUIED).ifPresent(ldSUIEDLDevice -> {
                            Optional.ofNullable(serviceTypeToIedSource.get(TServiceType.GOOSE))
                                    .ifPresent(iedSourceKeys -> manageMonitoringLns(iedSourceKeys, scd, tied, ldSUIEDLDevice, DO_GOCBREF, MonitoringLnClassEnum.LGOS));
                            Optional.ofNullable(serviceTypeToIedSource.get(TServiceType.SMV))
                                    .ifPresent(iedSourceKeys -> manageMonitoringLns(iedSourceKeys, scd, tied, ldSUIEDLDevice, DO_SVCBREF, MonitoringLnClassEnum.LSVS));
                        });
                    });
            return errorHandler.get();
        } finally {
            errorHandler.remove();
        }
    }

    private void removeLsvsLgos(SCL scd) {
        scd.getIED().stream().flatMap(tied -> ldeviceService.findLdevice(tied, LDEVICE_LDSUIED).stream())
                .forEach(tlDevice -> {
                    List<TLN> tlnList = tlDevice.getLN();
                    tlnList.removeIf(tln -> (tln.getLnClass().contains("LGOS") || tln.getLnClass().contains("LSVS")) && !tln.getInst().equals("1"));
                });
    }

    private void manageMonitoringLns(List<IedSource> iedSources, SCL scd, TIED tied, TLDevice ldsuiedLdevice, String doName, MonitoringLnClassEnum monitoringLnClassEnum) {
        List<TLN> lgosOrLsvsLns = lnService.getFilteredLns(ldsuiedLdevice, tln -> monitoringLnClassEnum.value().equals(tln.getLnClass().getFirst())).toList();
        if (lgosOrLsvsLns.isEmpty())
            errorHandler.get().add(SclReportItem.warning(tied.getName() + "/" + LDEVICE_LDSUIED + "/" + monitoringLnClassEnum.value(), "There is no LN %s present in LDevice".formatted(monitoringLnClassEnum.value())));
        DataRef dataRef = new DataRef(doName, List.of(), DA_SETSRCREF, List.of());
        lgosOrLsvsLns.forEach(lgosOrLsvs -> dataTypeTemplateService.findDoLinkedToDa(scd.getDataTypeTemplates(), lgosOrLsvs.getLnType(), dataRef)
                .map(doLinkedToDa -> lnService.getDoLinkedToDaCompletedFromDAI(tied, LDEVICE_LDSUIED, lgosOrLsvs, doLinkedToDa))
                .filter(doLinkedToDa -> {
                    if (!doLinkedToDa.isUpdatable())
                        errorHandler.get().add(SclReportItem.warning(tied.getName() + "/" + LDEVICE_LDSUIED + "/" + monitoringLnClassEnum.value() + "/DOI@name=\"" + doName + "\"/DAI@name=\"setSrcRef\"/Val", "The DAI cannot be updated"));
                    return doLinkedToDa.isUpdatable();
                })
                .ifPresent(doLinkedToDa -> {
                    log.info("Processing %d IED Source in LDName=%s for LN (lnClass=%s, inst=%s, prefix=%s)".formatted(iedSources.size(), ldsuiedLdevice.getLdName(), lgosOrLsvs.getLnClass().getFirst(), lgosOrLsvs.getInst(), lgosOrLsvs.getPrefix()));
                    for (int i = 0; i < iedSources.size(); i++) {
                        TLN lnToAdd = copyLn(lgosOrLsvs); //duplicate actual LGOS or LSVS in order to add LDSUIED with extRefs properties
                        IedSource iedSource = iedSources.get(i);
                        TIED sourceIed = iedService.findByName(scd, iedSource.iedName()).orElseThrow(() -> new ScdException("IED.name '" + iedSource.iedName() + "' not found in SCD"));
                        String sourceLdName = ldeviceService.findLdevice(sourceIed, iedSource.srcLdInst()).orElseThrow(() -> new ScdException(String.format("LDevice.inst '%s' not found in IED '%s'", iedSource.srcLdInst(), iedSource.iedName()))).getLdName();
                        lnToAdd.setInst(String.valueOf(i + 1));
                        DaVal newVal = new DaVal(null, sourceLdName + "/" + TLLN0Enum.LLN_0.value() + "." + iedSource.srcCBName());
                        doLinkedToDa.dataAttribute().getDaiValues().clear();
                        doLinkedToDa.dataAttribute().getDaiValues().add(newVal);
                        lnService.updateOrCreateDOAndDAInstances(lnToAdd, doLinkedToDa);
                        log.info("Processing %d IED Source in LDName=%s  - added LN (lnClass=%s, inst=%s, prefix=%s) - update DOI(name=%s)/DAI(name=%s) with value=%s".formatted(iedSources.size(), ldsuiedLdevice.getLdName(), lgosOrLsvs.getLnClass().getFirst(), String.valueOf(i + 1), lgosOrLsvs.getPrefix(), doName, DA_SETSRCREF, newVal.val()));
                        ldsuiedLdevice.getLN().add(lnToAdd);
                    }
                    ldsuiedLdevice.getLN().remove(lgosOrLsvs); //We can remove this LGOS or LSVS as we already added new ones
                }));
    }

    record IedSource(String iedName, String srcCBName, String srcLdInst, TServiceType serviceType){}

}
