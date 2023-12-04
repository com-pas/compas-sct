// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.TCompasSystemVersion;
import org.lfenergy.compas.scl2007b4.model.TDurationInMilliSec;
import org.lfenergy.compas.sct.commons.api.ControlBlockEditor;
import org.lfenergy.compas.sct.commons.dto.FcdaForDataSetsCreation;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.model.cbcom.*;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.ControlBlockAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.SclConstructorHelper;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControlBlockService implements ControlBlockEditor {

    private static final int MAX_VLAN_ID = 0x0FFF;
    private static final int MAX_VLAN_PRIORITY = 7;
    private static final String NONE = "none";

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
    public List<SclReportItem> configureNetworkForAllControlBlocks(SCL scd, CBCom cbCom) {
        List<SclReportItem> sclReportItems = new ArrayList<>();
        sclReportItems.addAll(configureNetworkForControlBlocks(scd, cbCom, TCBType.GOOSE));
        sclReportItems.addAll(configureNetworkForControlBlocks(scd, cbCom, TCBType.SV));
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

    private List<SclReportItem> configureNetworkForControlBlocks(SCL scd, CBCom cbCom, TCBType tcbType) {
        TRange appIdRange = Optional.ofNullable(cbCom.getAppIdRanges()).map(AppIdRanges::getAppIdRange).stream()
                .flatMap(Collection::stream)
                .filter(tRange -> tcbType.equals(tRange.getCBType()))
                .findFirst()
                .orElseThrow(() -> new ScdException("Control Block Communication setting files does not contain AppIdRange for cbType " + tcbType.value()));

        TRange macRange = Optional.ofNullable(cbCom.getMacRanges()).map(MacRanges::getMacRange).stream()
                .flatMap(Collection::stream)
                .filter(tRange -> tcbType.equals(tRange.getCBType()))
                .findFirst()
                .orElseThrow(() -> new ScdException("Control Block Communication setting files does not contain MacRange for cbType " + tcbType.value()));

        PrimitiveIterator.OfLong appIdIterator = Utils.sequence(Long.parseLong(appIdRange.getStart(), 16), Long.parseLong(appIdRange.getEnd(), 16));
        Iterator<String> macAddressIterator = Utils.macAddressSequence(macRange.getStart(), macRange.getEnd());

        ControlBlockEnum controlBlockEnum = switch (tcbType) {
            case GOOSE -> ControlBlockEnum.GSE;
            case SV -> ControlBlockEnum.SAMPLED_VALUE;
            default -> throw new ScdException("Control Block type not supported by communication configuration process : " + tcbType.value());
        };

        Map<Criteria, Settings> comSettingsByCriteria = Optional.ofNullable(cbCom.getVlans()).map(Vlans::getVlan).stream()
                .flatMap(Collection::stream)
                .filter(vlan -> tcbType.equals(vlan.getCBType()))
                .collect(Collectors.toMap(
                        this::vlanToCriteria,
                        this::vlanToSetting
                ));

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.streamIEDAdapters()
                .flatMap(iedAdapter ->
                        iedAdapter.streamLDeviceAdapters()
                                .filter(LDeviceAdapter::hasLN0)
                                .map(LDeviceAdapter::getLN0Adapter)
                                .flatMap(ln0Adapter -> ln0Adapter.streamControlBlocks(controlBlockEnum))
                                .map(controlBlockAdapter -> configureControlBlockNetwork(comSettingsByCriteria, appIdIterator, macAddressIterator, controlBlockAdapter)))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<SclReportItem> configureControlBlockNetwork(Map<Criteria, Settings> comSettingsByCriteria, PrimitiveIterator.OfLong appIdIterator, Iterator<String> macAddressIterator, ControlBlockAdapter controlBlockAdapter) {
        SettingsOrError settingsOrError = getNetworkSettings(controlBlockAdapter, comSettingsByCriteria);
        if (settingsOrError.errorMessage() != null) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure communication for this ControlBlock because: " + settingsOrError.errorMessage()));
        }
        Settings settings = settingsOrError.settings();
        if (settings == null) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure communication for this ControlBlock because no settings was provided"));
        }
        if (settings.vlanId() == null) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure communication for this ControlBlock because no Vlan Id was provided in the settings"));
        }
        if (!appIdIterator.hasNext()) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure communication for this ControlBlock because range of appId is exhausted"));
        }
        if (!macAddressIterator.hasNext()) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure communication for this ControlBlock because range of MAC Address is exhausted"));
        }

        return controlBlockAdapter.configureNetwork(appIdIterator.nextLong(), macAddressIterator.next(), settings.vlanId(), settings.vlanPriority(),
                settings.minTime(), settings.maxTime());
    }

    public SettingsOrError getNetworkSettings(ControlBlockAdapter controlBlockAdapter, Map<Criteria, Settings> comSettingsByCriteria) {
        IEDAdapter iedAdapter = controlBlockAdapter.getParentIedAdapter();
        Optional<TCompasSystemVersion> compasSystemVersion = iedAdapter.getCompasSystemVersion();
        if (compasSystemVersion.isEmpty()) {
            return new SettingsOrError(null, "No private COMPAS-SystemVersion found in this IED");
        }
        if (StringUtils.isBlank(compasSystemVersion.get().getMainSystemVersion())
                || (StringUtils.isBlank(compasSystemVersion.get().getMinorSystemVersion()))) {
            return new SettingsOrError(null, "No communication setting found for this Control Block");
        }
        String systemVersionWithoutV = removeVFromSystemVersion(compasSystemVersion.get());
        Optional<TCompasICDHeader> compasICDHeader = iedAdapter.getCompasICDHeader();
        if (compasICDHeader.isEmpty()) {
            return new SettingsOrError(null, "No private COMPAS-ICDHeader found in this IED");
        }
        if (compasICDHeader.get().getIEDSystemVersioninstance() == null) {
            return new SettingsOrError(null, "No IEDSystemVersioninstance in the COMPAS-ICDHeader of this IED");
        }
        TCBType cbType = switch (controlBlockAdapter.getControlBlockEnum()) {
            case GSE -> TCBType.GOOSE;
            case SAMPLED_VALUE -> TCBType.SV;
            default -> throw new ScdException("Unsupported Control Block type " + controlBlockAdapter.getControlBlockEnum());
        };
        TBayIntOrExt bayIntOrExt = controlBlockAdapter.getName().endsWith("I") ? TBayIntOrExt.BAY_INTERNAL : TBayIntOrExt.BAY_EXTERNAL;

        Criteria criteria = new Criteria(cbType,
                systemVersionWithoutV,
                TIEDType.fromValue(compasICDHeader.get().getIEDType().value()),
                TIEDRedundancy.fromValue(compasICDHeader.get().getIEDredundancy().value()),
                compasICDHeader.get().getIEDSystemVersioninstance(),
                bayIntOrExt);
        Settings settings = comSettingsByCriteria.get(criteria);
        return new SettingsOrError(settings, settings != null ? null : "No controlBlock communication configuration found for this these criteria " + criteria);
    }

    private String removeVFromSystemVersion(TCompasSystemVersion compasSystemVersion) {
        if (StringUtils.isBlank(compasSystemVersion.getMainSystemVersion())
                || (StringUtils.isBlank(compasSystemVersion.getMinorSystemVersion()))) {
            return null;
        }
        String[] minorVersionParts = compasSystemVersion.getMinorSystemVersion().split("\\.");
        return (minorVersionParts.length == 3) ?
                compasSystemVersion.getMainSystemVersion() + "." + minorVersionParts[0] + "." + minorVersionParts[1]
                : null;
    }

    private Criteria vlanToCriteria(TVlan vlan) {
        if (Objects.isNull(vlan.getCBType())
                || StringUtils.isBlank(vlan.getXY())
                || StringUtils.isBlank(vlan.getZW())
                || Objects.isNull(vlan.getIEDType())
                || Objects.isNull(vlan.getIEDRedundancy())
                || StringUtils.isBlank(vlan.getIEDSystemVersionInstance())
                || Objects.isNull(vlan.getBayIntOrExt())
        ) {
            throw new ScdException("At least one criteria is missing in vlan " + vlan);
        }
        return new Criteria(
                vlan.getCBType(),
                vlan.getXY() + "." + vlan.getZW(),
                vlan.getIEDType(),
                vlan.getIEDRedundancy(),
                new BigInteger(vlan.getIEDSystemVersionInstance()),
                vlan.getBayIntOrExt()
        );
    }

    private Settings vlanToSetting(TVlan vlan) {
        return new Settings(toVLanId(vlan.getVlanId()), toVlanPriority(vlan.getVlanPriority()), toDurationInMilliSec(vlan.getMinTime()), toDurationInMilliSec(vlan.getMaxTime()));
    }

    private Integer toVLanId(String strVlanId) {
        if (StringUtils.isBlank(strVlanId) || NONE.equalsIgnoreCase(strVlanId)) {
            return null;
        }
        int vlanId = Integer.parseInt(strVlanId);
        if (vlanId < 0 || vlanId > MAX_VLAN_ID) {
            throw new ScdException("VLAN ID must be between 0 and %d, but got : %d".formatted(MAX_VLAN_ID, vlanId));
        }
        return vlanId;
    }

    private static Byte toVlanPriority(String strVlanPriority) {
        if (StringUtils.isBlank(strVlanPriority) || NONE.equalsIgnoreCase(strVlanPriority)) {
            return null;
        }
        byte vlanPriority = Byte.parseByte(strVlanPriority);
        if (vlanPriority < 0 || vlanPriority > MAX_VLAN_PRIORITY) {
            throw new ScdException("VLAN PRIORITY must be between 0 and %d, but got : %d".formatted(MAX_VLAN_PRIORITY, vlanPriority));
        }
        return vlanPriority;
    }

    private TDurationInMilliSec toDurationInMilliSec(String duration) {
        if (StringUtils.isBlank(duration) || NONE.equalsIgnoreCase(duration)) {
            return null;
        }
        return SclConstructorHelper.newDurationInMilliSec(Long.parseLong(duration));
    }

    /**
     * Network settings for ControlBlock communication
     *
     * @param vlanId       id of the vlan
     * @param vlanPriority priority for the vlan
     * @param minTime      minTime for GSE communication element
     * @param maxTime      maxTime for GSE communication element
     */
    public record Settings(Integer vlanId, Byte vlanPriority, TDurationInMilliSec minTime, TDurationInMilliSec maxTime) {
    }

    /**
     * Network settings for ControlBlock communication or Error message
     *
     * @param settings     Network settings for ControlBlock communication. Can be null when errorMessage is provided
     * @param errorMessage should be null if settings is provided
     */
    public record SettingsOrError(Settings settings, String errorMessage) {
    }

    public record Criteria(TCBType cbType, String systemVersionWithoutV, TIEDType iedType, TIEDRedundancy iedRedundancy, BigInteger iedSystemVersionInstance, TBayIntOrExt bayIntOrExt) {
    }


}
