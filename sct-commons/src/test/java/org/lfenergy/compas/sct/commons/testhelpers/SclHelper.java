// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import lombok.experimental.UtilityClass;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.dto.SclReport;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.InputsAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter;
import org.opentest4j.AssertionFailedError;

/**
 * Provides static methods to quickly retrieve SCL elements, to be used in writing tests.
 * Methods throw AssertionFailedError when element not found (equivalent of calling JUnit Assert.fail())
 * which stops the test nicely (compared to an exception error), with a message and a failed status.
 */
@UtilityClass
public class SclHelper {
    private static LDeviceAdapter findLDevice(SclRootAdapter sclRootAdapter, String iedName, String ldInst) {
        return sclRootAdapter.findIedAdapterByName(iedName)
            .orElseThrow(() -> new AssertionFailedError(String.format("IED.name=%s not found", iedName))).findLDeviceAdapterByLdInst(ldInst).orElseThrow(() -> new AssertionFailedError(String.format("LDevice.inst=%s not found in IED.name=%s", ldInst, iedName)));
    }

    public static InputsAdapter findInputs(SclRootAdapter sclRootAdapter, String iedName, String ldInst) {
        LDeviceAdapter lDevice = findLDevice(sclRootAdapter, iedName, ldInst);
        if (!lDevice.hasLN0()) {
            throw new AssertionFailedError(String.format("IED.name=%s, LDevice.inst=%s has no LN0 element", ldInst, iedName));
        }
        LN0Adapter ln0Adapter = lDevice.getLN0Adapter();
        if (!ln0Adapter.hasInputs()) {
            throw new AssertionFailedError(String.format("IED.name=%s, LDevice.inst=%s, LN0 has no Inputs element", ldInst, iedName));
        }
        return ln0Adapter.getInputsAdapter();
    }

    public static TExtRef findExtRef(SclRootAdapter sclRootAdapter, String iedName, String ldInst, String extRefDesc) {
        return findInputs(sclRootAdapter, iedName, ldInst)
            .getCurrentElem()
            .getExtRef()
            .stream()
            .filter(extRef -> extRefDesc.equals(extRef.getDesc()))
            .findFirst()
            .orElseThrow(() -> new AssertionFailedError(String.format("ExtRef.des=%s not found in IED.name=%s,LDevice.inst=%s", extRefDesc, iedName, ldInst)));
    }

    public static TExtRef findExtRef(SclReport sclReport, String iedName, String ldInst, String extRefDesc) {
        return findExtRef(sclReport.getSclRootAdapter(), iedName, ldInst, extRefDesc);
    }

    public static LDeviceAdapter findLDevice(SclReport sclReport, String iedName, String ldInst) {
        return findLDevice(sclReport.getSclRootAdapter(), iedName, ldInst);
    }

}
