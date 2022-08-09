// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.sstation.FunctionAdapter;
import org.lfenergy.compas.sct.commons.scl.sstation.SubstationAdapter;
import org.lfenergy.compas.sct.commons.scl.sstation.VoltageLevelAdapter;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public final class SubstationService {

    private SubstationService() {
        throw new UnsupportedOperationException("This service class cannot be instantiated");
    }

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

    private static void updateBay(@NonNull VoltageLevelAdapter scdVoltageLevelAdapter, TBay tBay) {
        if (scdVoltageLevelAdapter.getBayAdapter(tBay.getName()).isPresent()) {
            scdVoltageLevelAdapter.getCurrentElem().getBay()
                .removeIf(t -> t.getName().equalsIgnoreCase(tBay.getName()));
            scdVoltageLevelAdapter.getCurrentElem().getBay().add(tBay);
        } else {
            scdVoltageLevelAdapter.getCurrentElem().getBay().add(tBay);
        }
    }

    public static void updateLNodeIEDNames(SCL scd) throws ScdException {
        if (!scd.isSetSubstation()) {
            return;
        }
        List<TFunction> functionsWithLNodes = scd.getSubstation().stream().filter(TSubstation::isSetVoltageLevel)
            .map(TSubstation::getVoltageLevel).flatMap(List::stream).filter(TVoltageLevel::isSetBay)
            .map(TVoltageLevel::getBay).flatMap(List::stream).filter(TBay::isSetFunction)
            .map(TBay::getFunction).flatMap(List::stream).filter(TLNodeContainer::isSetLNode)
            .collect(Collectors.toList());

        for (TFunction function : functionsWithLNodes) {
            FunctionAdapter functionAdapter = new FunctionAdapter(null, function);
            functionAdapter.updateLNodeIedNames();
        }
    }
}
