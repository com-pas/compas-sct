// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.Settings;
import org.lfenergy.compas.sct.commons.dto.SclReport;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ied.ControlBlockAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.ControlBlockNetworkSettingsCsvHelper;
import org.lfenergy.compas.sct.commons.util.PrivateEnum;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.io.Reader;
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
                        iedAdapter -> iedAdapter.getCompasICDHeader()
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
                            Optional<TCompasICDHeader> compasPrivate = iedAdapter.getCompasICDHeader();
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

    /**
     * Create All DataSet and ControlBlock in the SCL based on the ExtRef
     *
     * @param scd input SCD object. It could be modified by adding new DataSet and ControlBlocks
     * @return a report with all errors encountered
     */
    public static SclReport createDataSetAndControlBlocks(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        Stream<LDeviceAdapter> lDeviceAdapters = sclRootAdapter.streamIEDAdapters().flatMap(IEDAdapter::streamLDeviceAdapters);
        return createDataSetAndControlBlocks(sclRootAdapter, lDeviceAdapters);
    }

    /**
     * Create All DataSet and ControlBlock for the ExtRef in given IED
     *
     * @param scd           input SCD object. The object will be modified with the new DataSet and ControlBlocks
     * @param targetIedName the name of the IED where the ExtRef are
     * @return a report with all the errors encountered
     */
    public static SclReport createDataSetAndControlBlocks(SCL scd, String targetIedName) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(targetIedName);
        return createDataSetAndControlBlocks(sclRootAdapter, iedAdapter.streamLDeviceAdapters());

    }

    /**
     * Create All DataSet and ControlBlock for the ExtRef in this LDevice
     *
     * @param scd               input SCD object. The object will be modified with the new DataSet and ControlBlocks
     * @param targetIedName     the name of the IED where the ExtRef are
     * @param targetLDeviceInst the name of the LDevice where the ExtRef are
     * @return a report with all encountered errors
     */
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

    /**
     * Shortcut for {@link ExtRefService#configureNetworkForAllControlBlocks(SCL, ControlBlockNetworkSettings, long, long, String, String, ControlBlockEnum)}
     * using a CSV file to provide ControlBlockNetworkSettings.
     *
     * @param scd           input SCD object. The object will be modified with the new DataGSESet and SMV elements
     * @param csvSource     a Reader for the CSV file, as specified in {@link ControlBlockNetworkSettingsCsvHelper}.
     * @param appIdMin      range start for APPID (inclusive)
     * @param appIdMax      range end for APPID (inclusive)
     * @param macAddressMin range start for MAC-Addresses (inclusive). Ex: "01-0C-CD-01-00-00"
     * @param macAddressMax range end for MAC-Addresses (inclusive). Ex: "01-0C-CD-01-01-FF"
     * @return a report with all the errors encountered
     * @see ControlBlockNetworkSettingsCsvHelper
     * @see ControlBlockNetworkSettings
     * @see ExtRefService#configureNetworkForAllControlBlocks(SCL, ControlBlockNetworkSettings, long, long, String, String, ControlBlockEnum)
     */
    public static SclReport configureNetworkForAllControlBlocks(SCL scd, Reader csvSource,
                                                                long appIdMin, long appIdMax, String macAddressMin, String macAddressMax, ControlBlockEnum controlBlockEnum) {
        ControlBlockNetworkSettings controlBlockNetworkSettings = new ControlBlockNetworkSettingsCsvHelper(csvSource);
        return configureNetworkForAllControlBlocks(scd, controlBlockNetworkSettings, appIdMin, appIdMax, macAddressMin, macAddressMax, controlBlockEnum);
    }

    /**
     * Configure the network for all the ControlBlocks.
     * Create (or update if already existing) these elements
     * - the Communication/SubNetwork/ConnectedAP/GSE element, for the GSEControl blocks
     * - the Communication/SubNetwork/ConnectedAP/SMV element, for the SampledValueControl blocks
     *
     * @param scd                         input SCD object. The object will be modified with the new DataGSESet and SMV elements
     * @param controlBlockNetworkSettings a method tha gives the network configuration information for a given ControlBlock
     * @param appIdMin                    range start for APPID (inclusive)
     * @param appIdMax                    range end for APPID (inclusive)
     * @param macAddressMin               range start for MAC-Addresses (inclusive). Ex: "01-0C-CD-01-00-00"
     * @param macAddressMax               range end for MAC-Addresses (inclusive). Ex: "01-0C-CD-01-01-FF"
     * @return a report with all the errors encountered
     * @see Utils#macAddressToLong(String) for the expected MAC address format
     * @see ControlBlockNetworkSettings
     */
    public static SclReport configureNetworkForAllControlBlocks(SCL scd, ControlBlockNetworkSettings controlBlockNetworkSettings,
                                                                long appIdMin, long appIdMax, String macAddressMin, String macAddressMax, ControlBlockEnum controlBlockEnum) {
        PrimitiveIterator.OfLong appIdIterator = Utils.sequence(appIdMin, appIdMax);
        Iterator<String> macAddressIterator = Utils.macAddressSequence(macAddressMin, macAddressMax);

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        List<SclReportItem> sclReportItems =
                sclRootAdapter.streamIEDAdapters()
                        .flatMap(iedAdapter ->
                                iedAdapter.streamLDeviceAdapters()
                                        .filter(LDeviceAdapter::hasLN0)
                                        .map(LDeviceAdapter::getLN0Adapter)
                                        .flatMap(ln0Adapter -> ln0Adapter.streamControlBlocks(controlBlockEnum))
                                        .map(controlBlockAdapter -> configureControlBlockNetwork(controlBlockNetworkSettings, appIdIterator, macAddressIterator, controlBlockAdapter)))
                        .flatMap(Optional::stream)
                        .toList();
        return new SclReport(sclRootAdapter, sclReportItems);
    }

    private static Optional<SclReportItem> configureControlBlockNetwork(ControlBlockNetworkSettings controlBlockNetworkSettings, PrimitiveIterator.OfLong appIdIterator, Iterator<String> macAddressIterator, ControlBlockAdapter controlBlockAdapter) {
        Settings settings = controlBlockNetworkSettings.getNetworkSettings(controlBlockAdapter);

        if (settings == null) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure network for this ControlBlock because no settings was provided"));
        }
        if (settings.vlanId() == null) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure network for this ControlBlock because no Vlan Id was provided in the settings"));
        }
        if (!appIdIterator.hasNext()) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure network for this ControlBlock because range of appId is exhausted"));
        }
        if (!macAddressIterator.hasNext()) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure network for this ControlBlock because range of MAC Address is exhausted"));
        }

        return controlBlockAdapter.configureNetwork(appIdIterator.nextLong(), macAddressIterator.next(), settings.vlanId(), settings.vlanPriority(),
                settings.minTime(), settings.maxTime());
    }
}
