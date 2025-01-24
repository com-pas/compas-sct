// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.SubstationEditor;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.sstation.BayAdapter;
import org.lfenergy.compas.sct.commons.scl.sstation.VoltageLevelAdapter;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SubstationService implements SubstationEditor {

    private final VoltageLevelService voltageLevelService;

    @Override
    public void addSubstation(@NonNull SCL scd, @NonNull SCL ssd) throws ScdException {
        if (scd.getSubstation().size() > 1) {
            throw new ScdException(String.format("SCD file must have 0 or 1 Substation, but got %d", scd.getSubstation().size()));
        }
        if (ssd.getSubstation().size() != 1) {
            throw new ScdException(String.format("SSD file must have exactly 1 Substation, but got %d", ssd.getSubstation().size()));
        }
        TSubstation ssdTSubstation = ssd.getSubstation().getFirst();
        if (scd.getSubstation().isEmpty()) {
            scd.getSubstation().add(ssdTSubstation);
        } else {
            TSubstation scdTSubstation = scd.getSubstation().getFirst();
            if (scdTSubstation.getName().equalsIgnoreCase(ssdTSubstation.getName())){
                for (TVoltageLevel tvl : ssdTSubstation.getVoltageLevel()) {
                    updateVoltageLevel(scd, tvl);
                }
            } else {
                throw new ScdException("SCD file must have only one Substation and the Substation name from SSD file is" +
                    " different from the one in SCD file. The files are rejected.");
            }
        }
    }

    /**
     * Gets a pair of IedName and LDevice inst from Substation LNodes for LN0 type object
     * @return a pair of Ied name and LDevice inst attributes
     */
    public List<TLNode> getLNodes(SCL scd) {
        return scd.getSubstation().getFirst()
                .getVoltageLevel().stream()
                .flatMap(tVoltageLevel -> tVoltageLevel.getBay().stream())
                .flatMap(tBay -> tBay.getFunction().stream())
                .flatMap(tFunction -> tFunction.getLNode().stream())
                .filter(tlNode -> tlNode.getLnClass().contains(TLLN0Enum.LLN_0.value()))
                .toList();
    }


    /**
     * Creates new VoltageLevel section or updates VoltageLevel contents
     * @param scd SCL contain Substation in which VoltageLevel should be created/updated
     * @param vl VoltageLevel to create/update
     * @throws ScdException throws when unable to create new VoltageLevel section which is not already present in Substation
     */
    private void updateVoltageLevel(@NonNull SCL scd, TVoltageLevel vl) throws ScdException {
        voltageLevelService.findVoltageLevel(scd, tVoltageLevel -> tVoltageLevel.getName().equals(vl.getName()))
                .ifPresentOrElse(tVoltageLevel -> vl.getBay().forEach(tBay -> updateBay(tVoltageLevel, tBay)),
                        ()-> scd.getSubstation().getFirst().getVoltageLevel().add(vl));
    }


    /**
     * Adds new Bay in VoltageLevel or if already exist removes and replaces it
     * @param tVoltageLevel VoltageLevel in which Bay should be created/updated
     * @param tBay Bay to add
     */
    private void updateBay(@NonNull TVoltageLevel tVoltageLevel, TBay tBay) {
        tVoltageLevel.getBay()
                .stream().filter(tBay1 -> tBay1.getName().equals(tBay.getName()))
                .findFirst()
                .ifPresentOrElse(tBay1 -> {
                    tVoltageLevel.getBay().removeIf(t -> t.getName().equalsIgnoreCase(tBay.getName()));
                    tVoltageLevel.getBay().add(tBay);
                }, ()-> tVoltageLevel.getBay().add(tBay));
    }

}
