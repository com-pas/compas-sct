// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.sct.commons.dto.SclReport;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter;
import org.lfenergy.compas.sct.commons.util.PrivateEnum;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class ExtRefService {

    private static final String MESSAGE_MISSING_IED_NAME_PARAMETER = "IED.name parameter is missing";

    /**
     * Updates iedName attribute of all ExtRefs in the Scd.
     *
     * @return list of encountered errors
     */
    public static SclReport updateAllExtRefIedNames(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        List<SclReportItem> iedErrors = validateIed(sclRootAdapter);
        if (!iedErrors.isEmpty()) {
            return new SclReport(sclRootAdapter, iedErrors);
        }
        Map<String, IEDAdapter> icdSystemVersionToIed = sclRootAdapter.streamIEDAdapters()
            .collect(Collectors.toMap(
                iedAdapter -> PrivateService.extractCompasPrivate(iedAdapter.getCurrentElem(), TCompasICDHeader.class)
                    .map(TCompasICDHeader::getICDSystemVersionUUID)
                    .orElseThrow(), // Value presence is checked by method validateIed called above
                Function.identity()
            ));

        List<SclReportItem> extRefErrors = sclRootAdapter.streamIEDAdapters()
            .flatMap(IEDAdapter::streamLDeviceAdapters)
            .filter(LDeviceAdapter::hasLN0)
            .map(LDeviceAdapter::getLN0Adapter)
            .filter(LN0Adapter::hasInputs)
            .map(LN0Adapter::getInputsAdapter)
            .map(inputsAdapter -> inputsAdapter.updateAllExtRefIedNames(icdSystemVersionToIed))
            .flatMap(List::stream).toList();

        return new SclReport(sclRootAdapter, extRefErrors);
    }

    private static List<SclReportItem> validateIed(SclRootAdapter sclRootAdapter) {
        List<SclReportItem> iedErrors = new ArrayList<>(checkIedCompasIcdHeaderAttributes(sclRootAdapter));
        iedErrors.addAll(checkIedUnityOfIcdSystemVersionUuid(sclRootAdapter));
        return iedErrors;
    }

    private static List<SclReportItem> checkIedCompasIcdHeaderAttributes(SclRootAdapter sclRootAdapter) {
        return sclRootAdapter.streamIEDAdapters()
            .map(iedAdapter -> {
                    Optional<TCompasICDHeader> compasPrivate = PrivateService.extractCompasPrivate(iedAdapter.getCurrentElem(), TCompasICDHeader.class);
                    if (compasPrivate.isEmpty()) {
                        return iedAdapter.buildFatalReportItem(String.format("IED has no Private %s element", PrivateEnum.COMPAS_ICDHEADER.getPrivateType()));
                    }
                    if (StringUtils.isBlank(compasPrivate.get().getICDSystemVersionUUID())
                        || StringUtils.isBlank(compasPrivate.get().getIEDName())) {
                        return iedAdapter.buildFatalReportItem(String.format("IED private %s as no icdSystemVersionUUID or iedName attribute",
                            PrivateEnum.COMPAS_ICDHEADER.getPrivateType()));
                    }
                    return null;
                }
            ).filter(Objects::nonNull)
            .toList();
    }

    private static List<SclReportItem> checkIedUnityOfIcdSystemVersionUuid(SclRootAdapter sclRootAdapter) {
        Map<String, List<TIED>> systemVersionToIedList = sclRootAdapter.getCurrentElem().getIED().stream()
            .collect(Collectors.groupingBy(ied -> PrivateService.extractCompasPrivate(ied, TCompasICDHeader.class)
                .map(TCompasICDHeader::getICDSystemVersionUUID)
                .orElse("")));

        return systemVersionToIedList.entrySet().stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
            .filter(entry -> entry.getValue().size() > 1)
            .map(entry -> SclReportItem.fatal(entry.getValue().stream()
                    .map(tied -> new IEDAdapter(sclRootAdapter, tied))
                    .map(IEDAdapter::getXPath)
                    .collect(Collectors.joining(", ")),
                "/IED/Private/compas:ICDHeader[@ICDSystemVersionUUID] must be unique" +
                    " but the same ICDSystemVersionUUID was found on several IED."))
            .toList();
    }

    public static SclReport createDataSetAndControlBlocks(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        Stream<LDeviceAdapter> lDeviceAdapters = sclRootAdapter.streamIEDAdapters().flatMap(IEDAdapter::streamLDeviceAdapters);
        return createDataSetAndControlBlocks(sclRootAdapter, lDeviceAdapters);
    }

    public static SclReport createDataSetAndControlBlocks(SCL scd, String targetIedName) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(targetIedName);
        return createDataSetAndControlBlocks(sclRootAdapter, iedAdapter.streamLDeviceAdapters());

    }

    public static SclReport createDataSetAndControlBlocks(SCL scd, String targetIedName, String targetLDeviceInst) {
        if (StringUtils.isBlank(targetIedName)) {
            throw new ScdException(MESSAGE_MISSING_IED_NAME_PARAMETER);
        }
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(targetIedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(targetLDeviceInst);
        return createDataSetAndControlBlocks(sclRootAdapter, Stream.of(lDeviceAdapter));
    }

    private static SclReport createDataSetAndControlBlocks(SclRootAdapter sclRootAdapter, Stream<LDeviceAdapter> lDeviceAdapters) {
        List<SclReportItem> sclReportItems = lDeviceAdapters
            .map(LDeviceAdapter::createDataSetAndControlBlocks)
            .flatMap(List::stream)
            .toList();
        return new SclReport(sclRootAdapter, sclReportItems);
    }
}
