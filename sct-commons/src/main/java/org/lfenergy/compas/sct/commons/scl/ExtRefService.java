// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.experimental.UtilityClass;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.SclReport;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.InputsAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ExtRefService {

    /**
     * Updates iedName attribute of all ExtRefs in the Scd.
     * @return list of encountered errors
     */
    public static SclReport updateAllExtRefIedNames(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);

        List<SclReport.ErrorDescription> errorDescriptions = sclRootAdapter.streamIEDAdapters()
            .flatMap(IEDAdapter::streamLDeviceAdapters)
            .filter(LDeviceAdapter::hasLN0)
            .map(LDeviceAdapter::getLN0Adapter)
            .filter(LN0Adapter::hasInputs)
            .map(LN0Adapter::getInputsAdapter)
            .map(InputsAdapter::updateAllExtRefIedNames)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        return SclReport.builder()
            .sclRootAdapter(sclRootAdapter)
            .errorDescriptionList(errorDescriptions).build();
    }




}
