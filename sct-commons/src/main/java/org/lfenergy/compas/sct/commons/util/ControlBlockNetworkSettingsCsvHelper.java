// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import com.opencsv.bean.CsvBindByPosition;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.TCompasIEDRedundancy;
import org.lfenergy.compas.scl2007b4.model.TCompasIEDType;
import org.lfenergy.compas.scl2007b4.model.TDurationInMilliSec;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings;
import org.lfenergy.compas.sct.commons.scl.ied.ControlBlockAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;

import java.io.Reader;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class is an implementation example for interface ControlBlockNetworkSettings.
 * It relies on a CSV file.
 * The first columns of the CSV file are the criteria to match the ControlBlock (controlBlockEnum, systemVersionWithoutV, iedType, iedRedundancy,
 * isBayInternal),
 * The last columns are the network settings for the matched ControlBlock (as described in {@link ControlBlockNetworkSettings.Settings}).
 *
 * @see CsvUtils
 */
public class ControlBlockNetworkSettingsCsvHelper implements ControlBlockNetworkSettings {

    private static final int MAX_VLAN_ID = 0x0FFF;
    private static final int MAX_VLAN_PRIORITY = 7;
    private static final String NONE = "none";

    private final Map<Criteria, Settings> settings;

    /**
     * Constructor
     * Provide the CSV file as a Reader. For example, you can create a reader like this :
     * <code>new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8);</code>
     *
     * @param csvSource a reader that provides the data as CSV. For example :
     */
    public ControlBlockNetworkSettingsCsvHelper(Reader csvSource) {
        settings = readCsvFile(csvSource);
    }

    private Map<Criteria, Settings> readCsvFile(Reader csvSource) {
        return CsvUtils.parseRows(csvSource, Row.class).stream()
                .distinct()
                .collect(Collectors.toMap(
                        ControlBlockNetworkSettingsCsvHelper::rowToCriteria,
                        ControlBlockNetworkSettingsCsvHelper::rowToSetting
                ));
    }

    @Override
    public Settings getNetworkSettings(ControlBlockAdapter controlBlockAdapter) {
        ControlBlockEnum controlBlockEnum = controlBlockAdapter.getControlBlockEnum();
        IEDAdapter iedAdapter = controlBlockAdapter.getParentIedAdapter();
        String systemVersion = iedAdapter.getCompasSystemVersion()
                .map(version -> version.getMainSystemVersion() + "." + version.getMinorSystemVersion())
                .orElse(null);
        String systemVersionWithoutV = removeVFromSystemVersion(systemVersion);
        TCompasIEDType iedType = iedAdapter.getCompasICDHeader().map(TCompasICDHeader::getIEDType).orElse(null);
        TCompasIEDRedundancy iedRedundancy = iedAdapter.getCompasICDHeader().map(TCompasICDHeader::getIEDredundancy).orElse(null);
        boolean isBayInternal = controlBlockAdapter.getName().endsWith("I");
        return findSettings(new Criteria(controlBlockEnum, systemVersionWithoutV, iedType, iedRedundancy, isBayInternal));
    }

    private Settings findSettings(Criteria criteria) {
        Objects.requireNonNull(criteria);
        if (criteria.systemVersionWithoutV() == null
                || criteria.iedType() == null
                || criteria.iedRedundancy() == null) {
            return null;
        }
        return settings.get(criteria);
    }

    private static String removeVFromSystemVersion(String systemVersion) {
        if (systemVersion == null) {
            return null;
        }
        String[] systemVersionParts = systemVersion.split("\\.");
        if (systemVersionParts.length < 4) {
            return systemVersion;
        }
        return systemVersionParts[0] + "." + systemVersionParts[1] + "." + systemVersionParts[2] + "." + systemVersionParts[3];
    }

    private static Criteria rowToCriteria(Row row) {
        if (StringUtils.isBlank(row.cbType)
                || StringUtils.isBlank(row.xy)
                || StringUtils.isBlank(row.zw)
                || StringUtils.isBlank(row.iedType)
                || StringUtils.isBlank(row.bindingType)
        ) {
            throw new IllegalArgumentException("At least one criteria (cbType, xy, zw, iedType, bindingType) is blank");
        }
        ControlBlockEnum controlBlockEnum = switch (row.cbType) {
            case "GOOSE" -> ControlBlockEnum.GSE;
            case "SV" -> ControlBlockEnum.SAMPLED_VALUE;
            default -> throw new IllegalArgumentException("Unsupported Control Block Type : " + row.cbType);
        };
        return new Criteria(
                controlBlockEnum,
                row.xy + "." + row.zw,
                TCompasIEDType.fromValue(row.iedType),
                TCompasIEDRedundancy.fromValue(row.iedRedundancy),
                row.bindingType.equals("BAY_INTERNAL")
        );
    }

    private static Settings rowToSetting(Row row) {
        Integer vlanId = toVLanId(row.vlanId);
        Byte vlanPriority = toVlanPriority(row.vlanPriority);
        TDurationInMilliSec minTime = toDurationInMilliSec(row.minTime);
        TDurationInMilliSec maxTime = toDurationInMilliSec(row.maxTime);
        return new Settings(vlanId, vlanPriority, minTime, maxTime);
    }

    private static Byte toVlanPriority(String strVlanPriority) {
        if (StringUtils.isBlank(strVlanPriority) || NONE.equalsIgnoreCase(strVlanPriority)) {
            return null;
        }
        byte vlanPriority = Byte.parseByte(strVlanPriority);
        if (vlanPriority < 0 || vlanPriority > MAX_VLAN_PRIORITY) {
            throw new IllegalArgumentException("VLAN PRIORITY must be between 0 and %d, but got : %d".formatted(MAX_VLAN_PRIORITY, vlanPriority));
        }
        return vlanPriority;
    }

    private static Integer toVLanId(String strVlanId) {
        if (StringUtils.isBlank(strVlanId) || NONE.equalsIgnoreCase(strVlanId)) {
            return null;
        }
        int vlanId = Integer.parseInt(strVlanId);
        if (vlanId < 0 || vlanId > MAX_VLAN_ID) {
            throw new IllegalArgumentException("VLAN ID must be between 0 and %d, but got : %d".formatted(MAX_VLAN_ID, vlanId));
        }
        return vlanId;
    }

    private static TDurationInMilliSec toDurationInMilliSec(String duration) {
        if (StringUtils.isBlank(duration) || NONE.equalsIgnoreCase(duration)) {
            return null;
        }
        return SclConstructorHelper.newDurationInMilliSec(Long.parseLong(duration));
    }

    private record Criteria(
            ControlBlockEnum controlBlockEnum,
            String systemVersionWithoutV,
            TCompasIEDType iedType,
            TCompasIEDRedundancy iedRedundancy,
            boolean isBayInternal) {
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Row {
        @CsvBindByPosition(position = 0)
        private String cbType;
        @CsvBindByPosition(position = 1)
        private String xy;
        @CsvBindByPosition(position = 2)
        private String zw;
        @CsvBindByPosition(position = 3)
        private String iedType;
        @CsvBindByPosition(position = 4)
        private String iedRedundancy;
        @CsvBindByPosition(position = 5)
        private String bindingType;
        @CsvBindByPosition(position = 6)
        private String vlanId;
        @CsvBindByPosition(position = 7)
        private String vlanPriority;
        @CsvBindByPosition(position = 8)
        private String minTime;
        @CsvBindByPosition(position = 9)
        private String maxTime;
    }

}
