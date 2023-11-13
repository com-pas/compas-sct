// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.ControlBlockEditor;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.NetworkRanges;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.RangesPerCbType;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.Settings;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.SettingsOrError;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.util.*;

import java.util.*;
import java.util.stream.Stream;

public class ControlBlockService implements ControlBlockEditor {


    @Override
    public List<SclReportItem> analyzeDataGroups(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.streamIEDAdapters()
                .map(iedAdapter -> {
                    List<SclReportItem> list = new ArrayList<>();
                    list.addAll(iedAdapter.checkDataGroupCoherence());
                    list.addAll(iedAdapter.checkBindingDataGroupCoherence());
                    return list;
                }).flatMap(Collection::stream).toList();
    }

    @Override
    public List<SclReportItem> createDataSetAndControlBlocks(SCL scd, Set<FcdaForDataSetsCreation> allowedFcdas) {
        checkFcdaInitDataPresence(allowedFcdas);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        Stream<LDeviceAdapter> lDeviceAdapters = sclRootAdapter.streamIEDAdapters().flatMap(IEDAdapter::streamLDeviceAdapters);
        return createDataSetAndControlBlocks(lDeviceAdapters, allowedFcdas);
    }

    @Override
    public List<SclReportItem> createDataSetAndControlBlocks(SCL scd, String targetIedName, Set<FcdaForDataSetsCreation> allowedFcdas) {
        checkFcdaInitDataPresence(allowedFcdas);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(targetIedName);
        return createDataSetAndControlBlocks(iedAdapter.streamLDeviceAdapters(), allowedFcdas);

    }

    @Override
    public List<SclReportItem> createDataSetAndControlBlocks(SCL scd, String targetIedName, String targetLDeviceInst, Set<FcdaForDataSetsCreation> allowedFcdas) {
        if (StringUtils.isBlank(targetIedName)) {
            throw new ScdException("IED.name parameter is missing");
        }
        checkFcdaInitDataPresence(allowedFcdas);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(targetIedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(targetLDeviceInst);
        return createDataSetAndControlBlocks(Stream.of(lDeviceAdapter), allowedFcdas);
    }

    private void checkFcdaInitDataPresence(Set<FcdaForDataSetsCreation> allowedFcdas) {
        if (allowedFcdas == null || allowedFcdas.isEmpty()) {
            throw new ScdException("Accepted FCDAs list is empty, you should initialize allowed FCDA lists with CsvHelper class before");
        }
    }

    private List<SclReportItem> createDataSetAndControlBlocks(Stream<LDeviceAdapter> lDeviceAdapters, Set<FcdaForDataSetsCreation> allowedFcdas) {
        return lDeviceAdapters
                .map(lDeviceAdapter -> lDeviceAdapter.createDataSetAndControlBlocks(allowedFcdas))
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public List<SclReportItem> configureNetworkForAllControlBlocks(SCL scd, ControlBlockNetworkSettings controlBlockNetworkSettings,
                                                                   RangesPerCbType rangesPerCbType) {
        List<SclReportItem> sclReportItems = new ArrayList<>();
        sclReportItems.addAll(configureNetworkForControlBlocks(scd, controlBlockNetworkSettings, rangesPerCbType.gse(), ControlBlockEnum.GSE));
        sclReportItems.addAll(configureNetworkForControlBlocks(scd, controlBlockNetworkSettings, rangesPerCbType.sampledValue(), ControlBlockEnum.SAMPLED_VALUE));
        return sclReportItems;
    }

    @Override
    public void removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(final SCL scl) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scl);
        List<LDeviceAdapter> lDeviceAdapters = sclRootAdapter.streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters).toList();
        // LN0
        lDeviceAdapters.stream()
                .map(LDeviceAdapter::getLN0Adapter)
                .forEach(ln0 -> {
                    ln0.removeAllControlBlocksAndDatasets();
                    ln0.removeAllExtRefSourceBindings();
                });
        // Other LN
        lDeviceAdapters.stream()
                .map(LDeviceAdapter::getLNAdapters).flatMap(List::stream)
                .forEach(LNAdapter::removeAllControlBlocksAndDatasets);
    }


    private List<SclReportItem> configureNetworkForControlBlocks(SCL scd, ControlBlockNetworkSettings controlBlockNetworkSettings,
                                                                 NetworkRanges networkRanges, ControlBlockEnum controlBlockEnum) {
        PrimitiveIterator.OfLong appIdIterator = Utils.sequence(networkRanges.appIdStart(), networkRanges.appIdEnd());
        Iterator<String> macAddressIterator = Utils.macAddressSequence(networkRanges.macAddressStart(), networkRanges.macAddressEnd());

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.streamIEDAdapters()
                .flatMap(iedAdapter ->
                        iedAdapter.streamLDeviceAdapters()
                                .filter(LDeviceAdapter::hasLN0)
                                .map(LDeviceAdapter::getLN0Adapter)
                                .flatMap(ln0Adapter -> ln0Adapter.streamControlBlocks(controlBlockEnum))
                                .map(controlBlockAdapter -> configureControlBlockNetwork(controlBlockNetworkSettings, appIdIterator, macAddressIterator, controlBlockAdapter)))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<SclReportItem> configureControlBlockNetwork(ControlBlockNetworkSettings controlBlockNetworkSettings, PrimitiveIterator.OfLong appIdIterator, Iterator<String> macAddressIterator, ControlBlockAdapter controlBlockAdapter) {
        SettingsOrError settingsOrError = controlBlockNetworkSettings.getNetworkSettings(controlBlockAdapter);
        if (settingsOrError.errorMessage() != null) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure network for this ControlBlock because: " + settingsOrError.errorMessage()));
        }
        Settings settings = settingsOrError.settings();
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
