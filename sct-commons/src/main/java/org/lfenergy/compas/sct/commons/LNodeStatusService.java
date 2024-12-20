// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.RequiredArgsConstructor;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.domain.DataAttribute;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDa;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDaFilter;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.util.CommonConstants;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;
import org.lfenergy.compas.sct.commons.util.SclConstructorHelper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class LNodeStatusService {

    private static final String LNODE_STATUS_PRIVATE_TYPE = "COMPAS-LNodeStatus";
    private static final List<String> LN_LNS_POSSIBLE_VALUES = List.of("off;on", "on;off", "on", "off");
    private static final List<String> LNODE_LNS_POSSIBLE_VALUES = List.of("on", "off");
    private final LdeviceService ldeviceService;
    private final LnService lnService;
    private final DataTypeTemplatesService dataTypeTemplatesService;

    public List<SclReportItem> updateLnModStValBasedOnLNodeStatus(SCL scl) {
        return scl.getSubstation().stream()
                .flatMap(tSubstation -> tSubstation.getVoltageLevel().stream())
                .flatMap(tVoltageLevel -> tVoltageLevel.getBay().stream())
                .flatMap(tBay -> tBay.getFunction().stream())
                .flatMap(tFunction -> tFunction.getLNode().stream())
                .map(tlNode -> updateSingleLnModStValBasedOnLNodeStatus(scl, tlNode))
                .filter(Objects::nonNull)
                .toList();
    }

    private SclReportItem updateSingleLnModStValBasedOnLNodeStatus(SCL scl, TLNode tlNode) {
        String lNodeLNS = PrivateUtils.extractStringPrivate(tlNode, LNODE_STATUS_PRIVATE_TYPE).orElse(null);
        if (lNodeLNS == null || !LNODE_LNS_POSSIBLE_VALUES.contains(lNodeLNS)) {
            return SclReportItem.error(lNodePath(tlNode), "The private %s of the LNode has invalid value. Expecting one of [on, off] but got : %s".formatted(LNODE_STATUS_PRIVATE_TYPE, lNodeLNS));
        }
        TAnyLN anyLn = findLn(scl, tlNode.getIedName(), tlNode.getLdInst(), tlNode.getLnClass().getFirst(), tlNode.getLnInst(), tlNode.getPrefix()).orElse(null);
        if (anyLn == null) {
            return SclReportItem.error(lNodePath(tlNode), "LNode in Substation section does not have a matching LN in IED section");
        }
        String anyLnLNS = PrivateUtils.extractStringPrivate(anyLn, LNODE_STATUS_PRIVATE_TYPE).orElse(null);
        if (anyLnLNS == null || !LN_LNS_POSSIBLE_VALUES.contains(anyLnLNS)) {
            return SclReportItem.error(lnPath(tlNode), "The private %s of the LN has invalid value. Expecting one of %s but got : %s".formatted(LNODE_STATUS_PRIVATE_TYPE, LN_LNS_POSSIBLE_VALUES, anyLnLNS));

        }
        if (!anyLnLNS.contains(lNodeLNS)) {
            return SclReportItem.error(lnPath(tlNode), "Cannot set DAI Mod.stVal to %s, because LN private %s is set to %s".formatted(lNodeLNS, LNODE_STATUS_PRIVATE_TYPE, anyLnLNS));
        }
        TDAI daiModStVal = lnService.getDaiModStVal(anyLn).orElse(null);
        if (daiModStVal == null) {
            return null; // do nothing if DAI Mod.stVal is missing
        }
        List<String> modStValEnumValues = getModStValEnumValues(scl.getDataTypeTemplates(), anyLn.getLnType()).toList();
        if (!modStValEnumValues.contains(lNodeLNS)) {
            return SclReportItem.error(lnPath(tlNode), "Cannot set DAI Mod.stVal to '%s' because value is not in EnumType %s".formatted(lNodeLNS, modStValEnumValues));
        }
        daiModStVal.getVal().clear();
        daiModStVal.getVal().add(SclConstructorHelper.newVal(lNodeLNS));
        return null; // no error
    }

    private static String lnPath(TLNode tlNode) {
        return "IED(%s)/LD(%s)/LN[%s,%s,%s]".formatted(tlNode.getIedName(), tlNode.getLdInst(), tlNode.getLnClass().getFirst(), tlNode.getLnInst(), tlNode.getPrefix());
    }

    private static String lNodePath(TLNode tlNode) {
        return "LNode(iedName=%s, ldInst=%s, lnClass=%s, lnInst=%s, prefix=%s)".formatted(tlNode.getIedName(), tlNode.getLdInst(), tlNode.getLnClass().getFirst(), tlNode.getLnInst(), tlNode.getPrefix());
    }

    private Stream<String> getModStValEnumValues(TDataTypeTemplates dataTypeTemplates, String lnType) {
        return dataTypeTemplatesService.findDoLinkedToDa(dataTypeTemplates, lnType, DoLinkedToDaFilter.from(CommonConstants.MOD_DO_NAME, CommonConstants.STVAL_DA_NAME))
                .map(DoLinkedToDa::dataAttribute)
                .filter(dataAttribute -> TPredefinedBasicTypeEnum.ENUM.equals(dataAttribute.getBType()))
                .map(DataAttribute::getType)
                .flatMap(enumId ->
                        dataTypeTemplates.getEnumType().stream()
                                .filter(tEnumType -> tEnumType.getId().equals(enumId))
                                .findFirst())
                .stream()
                .flatMap(tEnumType -> tEnumType.getEnumVal().stream())
                .map(TEnumVal::getValue);
    }

    private Optional<TAnyLN> findLn(SCL scl, String iedName, String ldInst, String lnClass, String lnInst, String prefix) {
        return scl.getIED().stream()
                .filter(tied -> iedName.equals(tied.getName()))
                .findFirst()
                .flatMap(tied -> ldeviceService.findLdevice(tied, tlDevice -> ldInst.equals(tlDevice.getInst())))
                .flatMap(tlDevice -> lnService.findAnyLn(tlDevice, tAnyLN -> lnService.matchesLn(tAnyLN, lnClass, lnInst, prefix)));

    }
}
