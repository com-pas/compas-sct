// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.NonNull;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TBay;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.scl2007b4.model.TVoltageLevel;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.sstation.SubstationAdapter;
import org.lfenergy.compas.sct.commons.scl.sstation.VoltageLevelAdapter;

/**
 * A representation of the <em><b>{@link SubstationService SubstationService}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link SubstationService#addSubstation(SCL, SCL) <em>Adds the <b>Substation </b> object from given <b>SCL </b> object</em>}</li>
 *   <li>{@link SubstationService#updateVoltageLevel(SubstationAdapter, TVoltageLevel) <em>Adds the <b>TVoltageLevel </b> element under <b>TSubstation </b> reference object</em>}</li>
 *   <li>{@link SubstationService#updateBay(VoltageLevelAdapter, TBay) <em>Adds the <b>TBay </b> element under <b>TVoltageLevel </b> reference object</em>}</li>
 * </ul>
 * @see org.lfenergy.compas.sct.commons.scl.sstation.SubstationAdapter
 * @see org.lfenergy.compas.sct.commons.scl.sstation.VoltageLevelAdapter
 * @see org.lfenergy.compas.sct.commons.scl.sstation.BayAdapter
 * @see org.lfenergy.compas.sct.commons.scl.sstation.FunctionAdapter
 * @see org.lfenergy.compas.sct.commons.scl.sstation.LNodeAdapter
 * @see org.lfenergy.compas.sct.commons.scl.PrivateService
 */
public final class SubstationService {

    /**
     * Private Controlller, should not be instanced
     */
    private SubstationService() {
        throw new UnsupportedOperationException("This service class cannot be instantiated");
    }

    /**
     * Adds or Updates Substation section in SCL
     * @param scd SCL file in which Substation should be added/updated
     * @param ssd SCL file from which Substation should be copied
     * @return <em>SclRootAdapter</em> object as SCD file
     * @throws ScdException throws when SCD contents already another Substation, or with different name, or contents
     * more than one Substation
     */
    public static SclRootAdapter addSubstation(@NonNull SCL scd, @NonNull SCL ssd) throws ScdException {
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        SclRootAdapter ssdRootAdapter = new SclRootAdapter(ssd);
        if (scdRootAdapter.getCurrentElem().getSubstation().size() > 1) {
            throw new ScdException(String.format("SCD file must have 0 or 1 Substation, but got %d",
                scdRootAdapter.getCurrentElem().getSubstation().size()));
        }
        if (ssdRootAdapter.getCurrentElem().getSubstation().size() != 1) {
            throw new ScdException(String.format("SSD file must have exactly 1 Substation, but got %d",
                ssdRootAdapter.getCurrentElem().getSubstation().size()));
        }
        TSubstation ssdTSubstation = ssdRootAdapter.currentElem.getSubstation().get(0);
        if (scdRootAdapter.getCurrentElem().getSubstation().isEmpty()) {
            scdRootAdapter.getCurrentElem().getSubstation().add(ssdTSubstation);
            return scdRootAdapter;
        } else {
            TSubstation scdTSubstation = scdRootAdapter.currentElem.getSubstation().get(0);
            if (scdTSubstation.getName().equalsIgnoreCase(ssdTSubstation.getName())) {
                SubstationAdapter scdSubstationAdapter = scdRootAdapter.getSubstationAdapter(scdTSubstation.getName());
                for (TVoltageLevel tvl : ssdTSubstation.getVoltageLevel()) {
                    updateVoltageLevel(scdSubstationAdapter, tvl);
                }
            } else
                throw new ScdException("SCD file must have only one Substation and the Substation name from SSD file is" +
                    " different from the one in SCD file. The files are rejected.");
        }
        return scdRootAdapter;
    }

    /**
     * Creates new VoltageLevel section or updates VoltageLevel contents
     * @param scdSubstationAdapter Substation in which VoltageLevel should be created/updated
     * @param vl VoltageLevel to create/update
     * @throws ScdException throws when unable to create new VoltageLevel section which is not already present in Substation
     */
    private static void updateVoltageLevel(@NonNull SubstationAdapter scdSubstationAdapter, TVoltageLevel vl) throws ScdException {
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
    private static void updateBay(@NonNull VoltageLevelAdapter scdVoltageLevelAdapter, TBay tBay) {
        if (scdVoltageLevelAdapter.getBayAdapter(tBay.getName()).isPresent()) {
            scdVoltageLevelAdapter.getCurrentElem().getBay()
                .removeIf(t -> t.getName().equalsIgnoreCase(tBay.getName()));
            scdVoltageLevelAdapter.getCurrentElem().getBay().add(tBay);
        } else {
            scdVoltageLevelAdapter.getCurrentElem().getBay().add(tBay);
        }
    }

}
