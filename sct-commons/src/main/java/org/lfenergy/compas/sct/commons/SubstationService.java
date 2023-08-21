// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.NonNull;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TBay;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.scl2007b4.model.TVoltageLevel;
import org.lfenergy.compas.sct.commons.api.SubstationEditor;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.sstation.SubstationAdapter;
import org.lfenergy.compas.sct.commons.scl.sstation.VoltageLevelAdapter;

public class SubstationService implements SubstationEditor {

    @Override
    public void addSubstation(@NonNull SCL scd, @NonNull SCL ssd) throws ScdException {
        if (scd.getSubstation().size() > 1) {
            throw new ScdException(String.format("SCD file must have 0 or 1 Substation, but got %d", scd.getSubstation().size()));
        }
        if (ssd.getSubstation().size() != 1) {
            throw new ScdException(String.format("SSD file must have exactly 1 Substation, but got %d", ssd.getSubstation().size()));
        }
        TSubstation ssdTSubstation = ssd.getSubstation().get(0);
        if (scd.getSubstation().isEmpty()) {
            scd.getSubstation().add(ssdTSubstation);
        } else {
            TSubstation scdTSubstation = scd.getSubstation().get(0);
            if (scdTSubstation.getName().equalsIgnoreCase(ssdTSubstation.getName())) {
                SubstationAdapter scdSubstationAdapter = new SclRootAdapter(scd).getSubstationAdapter(scdTSubstation.getName());
                for (TVoltageLevel tvl : ssdTSubstation.getVoltageLevel()) {
                    updateVoltageLevel(scdSubstationAdapter, tvl);
                }
            } else
                throw new ScdException("SCD file must have only one Substation and the Substation name from SSD file is" +
                    " different from the one in SCD file. The files are rejected.");
        }
    }

    /**
     * Creates new VoltageLevel section or updates VoltageLevel contents
     * @param scdSubstationAdapter Substation in which VoltageLevel should be created/updated
     * @param vl VoltageLevel to create/update
     * @throws ScdException throws when unable to create new VoltageLevel section which is not already present in Substation
     */
    private void updateVoltageLevel(@NonNull SubstationAdapter scdSubstationAdapter, TVoltageLevel vl) throws ScdException {
        if (scdSubstationAdapter.getVoltageLevelAdapter(vl.getName()).isPresent()) {
            VoltageLevelAdapter scdVoltageLevelAdapter = scdSubstationAdapter.getVoltageLevelAdapter(vl.getName())
                .orElseThrow(() -> new ScdException("Unable to create VoltageLevelAdapter"));
            for (TBay tbay : vl.getBay()) {
                updateBay(scdVoltageLevelAdapter, tbay);
            }
        } else {
            scdSubstationAdapter.getCurrentElem().getVoltageLevel().add(vl);
        }
    }

    /**
     * Adds new Bay in VoltageLevel or if already exist removes and replaces it
     * @param scdVoltageLevelAdapter VoltageLevel in which Bay should be created/updated
     * @param tBay Bay to add
     */
    private void updateBay(@NonNull VoltageLevelAdapter scdVoltageLevelAdapter, TBay tBay) {
        if (scdVoltageLevelAdapter.getBayAdapter(tBay.getName()).isPresent()) {
            scdVoltageLevelAdapter.getCurrentElem().getBay()
                .removeIf(t -> t.getName().equalsIgnoreCase(tBay.getName()));
            scdVoltageLevelAdapter.getCurrentElem().getBay().add(tBay);
        } else {
            scdVoltageLevelAdapter.getCurrentElem().getBay().add(tBay);
        }
    }

}
