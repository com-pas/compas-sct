// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import lombok.experimental.UtilityClass;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.SclReport;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.Utils;
import org.opentest4j.AssertionFailedError;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Provides static methods to quickly retrieve SCL elements, to be used in writing tests.
 * Methods throw AssertionFailedError when element not found (equivalent of calling JUnit Assert.fail())
 * which stops the test nicely (compared to an exception error), with a message and a failed status.
 */
@UtilityClass
public class SclHelper {
    public static LDeviceAdapter findLDevice(SclRootAdapter sclRootAdapter, String iedName, String ldInst) {
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

    public static LN0Adapter findLn0(SclRootAdapter sclRootAdapter, String iedName, String ldInst) {
        LDeviceAdapter lDevice = findLDevice(sclRootAdapter, iedName, ldInst);
        if (!lDevice.hasLN0()) {
            throw new AssertionFailedError(String.format("LN0 not found in IED.name=%s,LDevice.inst=%s", iedName, ldInst));
        }
        return lDevice.getLN0Adapter();
    }

    public static LNAdapter findLn(SclRootAdapter sclRootAdapter, String iedName, String ldInst, String lnClass, String lnInst, String prefix) {
        LDeviceAdapter lDevice = findLDevice(sclRootAdapter, iedName, ldInst);
        if (!lDevice.getCurrentElem().isSetLN()) {
            throw new AssertionFailedError(String.format("No LN found in IED.name=%s,LDevice.inst=%s", iedName, ldInst));
        }
        return lDevice.getCurrentElem().getLN()
            .stream()
            .filter(tln -> Utils.lnClassEquals(tln.getLnClass(), lnClass)
                && tln.getInst().equals(lnInst)
                && Utils.equalsOrBothBlank(prefix, tln.getPrefix()))
            .findFirst()
            .map(tln -> new LNAdapter(lDevice, tln))
            .orElseThrow(
                ()-> new AssertionFailedError("LN found in IED.name=%s,LDevice.inst=%s with lnClass=%s, lnInst=%s, prefix=%s"
                    .formatted(iedName, ldInst, lnClass, lnInst, prefix))
            );
    }

    public static LDeviceAdapter findLDeviceByLdName(SclRootAdapter sclRootAdapter, String ldName) {
        return sclRootAdapter.streamIEDAdapters()
            .flatMap(IEDAdapter::streamLDeviceAdapters)
            .filter(lDeviceAdapter -> ldName.equals(lDeviceAdapter.getLdName()))
            .findFirst()
            .orElseThrow(()-> new AssertionFailedError("LDevice with ldName=%s not found in SCD".formatted(ldName)));
    }

    public static DataSetAdapter findDataSet(SclRootAdapter sclRootAdapter, String iedName, String ldInst, String dataSetName) {
        LN0Adapter ln0 = findLn0(sclRootAdapter, iedName, ldInst);
        return ln0.findDataSetByName(dataSetName)
            .orElseThrow(() -> new AssertionFailedError(String.format("DataSet.name=%s not found in IED.name=%s,LDevice.inst=%s,LN0",
                dataSetName, iedName, ldInst)));
    }

    public static DataSetAdapter findDataSet(SclReport sclReport, String iedName, String ldInst, String dataSetName) {
        return findDataSet(sclReport.getSclRootAdapter(), iedName, ldInst, dataSetName);
    }

    public static ControlBlockAdapter findControlBlock(SclRootAdapter sclRootAdapter, String iedName, String ldInst, String cbName,
                                                       ControlBlockEnum controlBlockEnum) {
        LN0Adapter ln0 = findLn0(sclRootAdapter, iedName, ldInst);
        return ln0.findControlBlock(cbName, controlBlockEnum)
            .orElseThrow(() -> new AssertionFailedError(String.format("%s name=%s not found in IED.name=%s,LDevice.inst=%s,LN0",
                controlBlockEnum.getControlBlockClass().getSimpleName(), cbName, iedName, ldInst)));
    }

    public static void assertControlBlockExists(SclReport sclReport, String iedName, String ldInst, String cbName,
                                                       String datSet, String id, ControlBlockEnum controlBlockEnum) {
        ControlBlockAdapter controlBlock = findControlBlock(sclReport.getSclRootAdapter(), iedName, ldInst, cbName, controlBlockEnum);
        assertThat(controlBlock.getCurrentElem().getDatSet()).isEqualTo(datSet);
        assertThat(getControlBlockId(controlBlock.getCurrentElem())).isEqualTo(id);
    }

    private String getControlBlockId(TControl tControl){
        if (tControl instanceof TGSEControl tgseControl){
            return tgseControl.getAppID();
        }
        if (tControl instanceof TSampledValueControl tSampledValueControl){
            return tSampledValueControl.getSmvID();
        }
        if (tControl instanceof TReportControl tReportControl){
            return tReportControl.getRptID();
        }
        throw new AssertionFailedError("Cannot get Id for ControlBlock of type " + tControl.getClass().getSimpleName());
    }

    public static Stream<TDataSet> streamAllDataSets(SclRootAdapter sclRootAdapter) {
        return streamAllLn0Adapters(sclRootAdapter)
            .map(ln0Adapter -> ln0Adapter.getCurrentElem().getDataSet())
            .flatMap(List::stream);
    }

    public static Stream<LN0Adapter> streamAllLn0Adapters(SclRootAdapter sclRootAdapter) {
        return sclRootAdapter
            .streamIEDAdapters()
            .flatMap(IEDAdapter::streamLDeviceAdapters)
            .filter(LDeviceAdapter::hasLN0)
            .map(LDeviceAdapter::getLN0Adapter);
    }

    public static Stream<TExtRef> streamAllExtRef(SclRootAdapter sclRootAdapter) {
        return streamAllLn0Adapters(sclRootAdapter)
            .filter(AbstractLNAdapter::hasInputs)
            .map(LN0Adapter::getInputsAdapter)
            .map(InputsAdapter::getCurrentElem)
            .map(TInputs::getExtRef)
            .flatMap(List::stream);
    }

}
