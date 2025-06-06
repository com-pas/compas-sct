// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import org.assertj.core.api.Assertions;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.Utils;
import org.opentest4j.AssertionFailedError;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newConnectedAp;
import static org.lfenergy.compas.sct.commons.util.Utils.lnClassEquals;

/**
 * Provides static methods to quickly retrieve SCL elements, to be used in writing tests.
 * Methods throw AssertionFailedError when element not found (equivalent of calling JUnit Assert.fail())
 * which stops the test nicely (compared to an exception error), with a message and a failed status.
 */
public final class SclHelper {

    private SclHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String DO_GOCBREF = "GoCBRef";
    public static final String DO_SVCBREF = "SvCBRef";
    public static final String LD_LDSUIED = "LDSUIED";
    public static final String IED_NAME_1 = "IED_NAME_1";
    public static final String IED_NAME_2 = "IED_NAME_2";

    public static IEDAdapter findIed(SCL scl, String iedName) {
        return new SclRootAdapter(scl).findIedAdapterByName(iedName)
                .orElseThrow(() -> new AssertionFailedError(String.format("IED.name=%s not found", iedName)));
    }

    public static LDeviceAdapter findLDevice(SCL scl, String iedName, String ldInst) {
        return findIed(scl, iedName).findLDeviceAdapterByLdInst(ldInst).orElseThrow(() -> new AssertionFailedError(String.format("LDevice.inst=%s not found in IED.name=%s", ldInst, iedName)));
    }

    public static InputsAdapter findInputs(SCL scl, String iedName, String ldInst) {
        LDeviceAdapter lDevice = findLDevice(scl, iedName, ldInst);
        if (!lDevice.hasLN0()) {
            throw new AssertionFailedError(String.format("IED.name=%s, LDevice.inst=%s has no LN0 element", ldInst, iedName));
        }
        LN0Adapter ln0Adapter = lDevice.getLN0Adapter();
        if (!ln0Adapter.hasInputs()) {
            throw new AssertionFailedError(String.format("IED.name=%s, LDevice.inst=%s, LN0 has no Inputs element", ldInst, iedName));
        }
        return ln0Adapter.getInputsAdapter();
    }

    public static TExtRef findExtRef(SCL scl, String iedName, String ldInst, String extRefDesc) {
        return findInputs(scl, iedName, ldInst)
                .getCurrentElem()
                .getExtRef()
                .stream()
                .filter(extRef -> extRefDesc.equals(extRef.getDesc()))
                .findFirst()
                .orElseThrow(() -> new AssertionFailedError(String.format("ExtRef.desc=%s not found in IED.name=%s,LDevice.inst=%s", extRefDesc, iedName, ldInst)));
    }

    public static LN0Adapter findLn0(SCL scl, String iedName, String ldInst) {
        LDeviceAdapter lDevice = findLDevice(scl, iedName, ldInst);
        if (!lDevice.hasLN0()) {
            throw new AssertionFailedError(String.format("LN0 not found in IED.name=%s,LDevice.inst=%s", iedName, ldInst));
        }
        return lDevice.getLN0Adapter();
    }

    public static LNAdapter findLn(SCL scl, String iedName, String ldInst, String lnClass, String lnInst, String prefix) {
        LDeviceAdapter lDevice = findLDevice(scl, iedName, ldInst);
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
                        () -> new AssertionFailedError("LN found in IED.name=%s,LDevice.inst=%s with lnClass=%s, lnInst=%s, prefix=%s"
                                .formatted(iedName, ldInst, lnClass, lnInst, prefix))
                );
    }

    public static TAnyLN findAnyLn(SCL scl, String iedName, String ldInst, String lnClass, String lnInst, String prefix) {
        TIED ied = scl.getIED().stream().filter(tied -> tied.getName().equals(iedName))
                .findFirst().orElseThrow(() -> new AssertionFailedError("IED with name=%s not found".formatted(iedName)));
        TLDevice tlDevice = getLDevices(ied)
                .filter(tlDevice1 -> tlDevice1.getInst().equals(ldInst))
                .findFirst().orElseThrow(() -> new AssertionFailedError("LDevice with inst=%s not found".formatted(ldInst)));
        if (lnClass.equals(TLLN0Enum.LLN_0.value()) && tlDevice.isSetLN0()) {
            return tlDevice.getLN0();
        }
        return tlDevice.getLN().stream()
                .filter(tln -> Utils.lnClassEquals(tln.getLnClass(), lnClass) && trimToEmpty(tln.getInst()).equals(trimToEmpty(lnInst)) && trimToEmpty(tln.getPrefix()).equals(trimToEmpty(prefix)))
                .findFirst()
                .orElseThrow(() -> new AssertionFailedError("LN (lnClass=%s, lnInst=%s, lnPrefix=%s) not found".formatted(lnClass, lnInst, prefix)));
    }

    public static Stream<TLDevice> getLDevices(TIED tied) {
        return tied.getAccessPoint().stream()
                .filter(TAccessPoint::isSetServer)
                .flatMap(tAccessPoint -> tAccessPoint.getServer().getLDevice().stream());
    }

    public static IDataParentAdapter findDoiOrSdi(AbstractLNAdapter<?> lnAdapter, String dataTypeRef) {
        if (dataTypeRef.isEmpty()) {
            Assertions.fail("dataTypeRef must at least contain a DO, but got: " + dataTypeRef);
        }
        String[] names = dataTypeRef.split("\\.");
        IDataParentAdapter parentAdapter = lnAdapter.getDOIAdapterByName(names[0]);
        for (int i = 1; i < names.length; i++) {
            parentAdapter = parentAdapter.getStructuredDataAdapterByName(names[i]);
        }
        return parentAdapter;
    }

    public static Optional<TDAI> findDai(SCL scl, String iedName, String ldInst, String doiName, String daiName) {
        return findDai(scl, iedName, ldInst, "LLN0", "", "", doiName, daiName);
    }

    public static Optional<TDAI> findDai(SCL scl, String iedName, String ldInst, String lnClass, String lnInst, String lnPrefix, String doiName, String daiName) {
        return findAnyLn(scl, iedName, ldInst, lnClass, lnInst, lnPrefix)
                .getDOI().stream()
                .filter(tdoi -> tdoi.getName().equals(doiName))
                .flatMap(tdoi -> tdoi.getSDIOrDAI().stream().map(tUnNaming -> (TDAI) tUnNaming))
                .filter(tdai -> tdai.getName().equals(daiName))
                .findFirst();
    }

    public static AbstractDAIAdapter<?> findDai(AbstractLNAdapter<?> lnAdapter, String dataTypeRef) {
        String[] names = dataTypeRef.split("\\.");
        if (names.length < 2) {
            Assertions.fail("dataTypeRef must at least contain a DO and a DA name, but got: " + dataTypeRef);
        }

        IDataParentAdapter parentAdapter = findDoiOrSdi(lnAdapter, String.join(".", Arrays.asList(names).subList(0, names.length - 1)));
        return parentAdapter.getDataAdapterByName(names[names.length - 1]);
    }

    public static String getValue(AbstractDAIAdapter<?> daiAdapter) {
        return getValue(daiAdapter.getCurrentElem());
    }

    public static String getValue(TDAI tdai) {
        if (!tdai.isSetVal()) {
            Assertions.fail("No value found for DAI " + tdai.getName());
        } else if (tdai.getVal().size() > 1) {
            Assertions.fail("Expecting a single value for for DAI " + tdai.getName());
        }
        return tdai.getVal().getFirst().getValue();
    }

    public static DataSetAdapter findDataSet(SCL scl, String iedName, String ldInst, String dataSetName) {
        LN0Adapter ln0 = findLn0(scl, iedName, ldInst);
        return ln0.findDataSetByName(dataSetName)
                .orElseThrow(() -> new AssertionFailedError(String.format("DataSet.name=%s not found in IED.name=%s,LDevice.inst=%s,LN0",
                        dataSetName, iedName, ldInst)));
    }

    public static <T extends TControl> T findControlBlock(SCL scl, String iedName, String ldInst, String cbName, Class<T> controlBlockClass) {
        LN0Adapter ln0 = findLn0(scl, iedName, ldInst);
        return ln0.getTControlsByType(controlBlockClass).stream()
                .filter(t -> cbName.equals(t.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionFailedError(String.format("%s name=%s not found in IED.name=%s,LDevice.inst=%s,LN0",
                        controlBlockClass.getSimpleName(), cbName, iedName, ldInst)));
    }

    public static void assertControlBlockExists(SCL scd, String iedName, String ldInst, String cbName,
                                                String datSet, String id, ControlBlockEnum controlBlockEnum) {
        TControl controlBlock = findControlBlock(scd, iedName, ldInst, cbName, controlBlockEnum.getControlBlockClass());
        assertThat(controlBlock.getDatSet()).isEqualTo(datSet);
        assertThat(getControlBlockId(controlBlock)).isEqualTo(id);
    }

    private static String getControlBlockId(TControl tControl) {
        if (tControl instanceof TGSEControl tgseControl) {
            return tgseControl.getAppID();
        }
        if (tControl instanceof TSampledValueControl tSampledValueControl) {
            return tSampledValueControl.getSmvID();
        }
        if (tControl instanceof TReportControl tReportControl) {
            return tReportControl.getRptID();
        }
        throw new AssertionFailedError("Cannot get Id for ControlBlock of type " + tControl.getClass().getSimpleName());
    }

    public static Stream<TDataSet> streamAllDataSets(SCL scl) {
        return streamAllLn0Adapters(scl)
                .map(ln0Adapter -> ln0Adapter.getCurrentElem().getDataSet())
                .flatMap(List::stream);
    }

    public static Stream<LN0Adapter> streamAllLn0Adapters(SCL scl) {
        return new SclRootAdapter(scl)
                .streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters)
                .filter(LDeviceAdapter::hasLN0)
                .map(LDeviceAdapter::getLN0Adapter);
    }

    public static Stream<TExtRef> streamAllExtRef(SCL scl) {
        return streamAllLn0Adapters(scl)
                .filter(AbstractLNAdapter::hasInputs)
                .map(LN0Adapter::getInputsAdapter)
                .map(InputsAdapter::getCurrentElem)
                .map(TInputs::getExtRef)
                .flatMap(List::stream);
    }

    public static String getDaiValue(AbstractLNAdapter<?> ln, String doiName, String daiName) {
        return ln.getDOIAdapterByName(doiName).getDataAdapterByName(daiName).getCurrentElem().getVal().getFirst().getValue();
    }

    public static Stream<TDAI> getDai(TAnyLN tAnyLN, String doiName, String daiName) {
        return tAnyLN.getDOI().stream().filter(tdoi -> tdoi.getName().equals(doiName))
                .flatMap(tdoi -> tdoi.getSDIOrDAI().stream())
                .filter(TDAI.class::isInstance)
                .map(TDAI.class::cast)
                .filter(tdai -> daiName.equals(tdai.getName()));
    }

    public static TDAI getDai(TDOI tdoi, String daiName) {
        return  tdoi.getSDIOrDAI().stream()
                .filter(TDAI.class::isInstance)
                .map(TDAI.class::cast)
                .filter(tdai -> daiName.equals(tdai.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionFailedError(String.format("DAI %s not found in DOI %s", daiName, tdoi.getName())));
    }

    public static String getDaiValue(TAnyLN tAnyLN, String doiName, String daiName) {
        TDAI tdai = getDai(tAnyLN, doiName, daiName).findFirst()
                .orElseThrow(() -> new AssertionFailedError(String.format("DAI %s.%s not found in LN", doiName, daiName)));
        return getValue(tdai);
    }

    public static String getDaiValue(TDOI tdoi, String daiName) {
        return getValue(getDai(tdoi, daiName));
    }

    public static SclRootAdapter createSclRootAdapterWithIed(String iedName) {
        SCL scl = new SCL();
        scl.setHeader(new THeader());
        TIED ied = new TIED();
        ied.setName(iedName);
        scl.getIED().add(ied);
        return new SclRootAdapter(scl);
    }

    public static SclRootAdapter createSclRootWithConnectedAp(String iedName, String apName) {
        SclRootAdapter sclRootAdapter = createSclRootAdapterWithIed(iedName);
        SCL scl = sclRootAdapter.getCurrentElem();
        scl.setCommunication(new TCommunication());
        TSubNetwork subNetwork = new TSubNetwork();
        scl.getCommunication().getSubNetwork().add(subNetwork);
        subNetwork.getConnectedAP().add(newConnectedAp(iedName, apName));
        return sclRootAdapter;
    }

    public static TExtRef createExtRefExample(String cbName, TServiceType tServiceType) {
        TExtRef tExtRef = new TExtRef();
        tExtRef.setIedName("IED_NAME_2");
        tExtRef.setServiceType(tServiceType);
        tExtRef.setSrcLDInst("Inst_2");
        tExtRef.setSrcLNInst("LN");
        tExtRef.setSrcPrefix("Prefix");
        tExtRef.setSrcCBName(cbName);
        return tExtRef;
    }

    public static SclRootAdapter createIedsInScl(String lnClass, String doName) {
        // DataTypeTemplate
        TDO tdo = new TDO();
        tdo.setName(doName);
        tdo.setType("REF");
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("T1");
        tlNodeType.getLnClass().add(lnClass);
        tlNodeType.getDO().add(tdo);

        TDA tda = new TDA();
        tda.setName("setSrcRef");
        tda.setValImport(true);
        tda.setBType(TPredefinedBasicTypeEnum.OBJ_REF);
        tda.setFc(TFCEnum.SP);

        TDOType tdoType = new TDOType();
        tdoType.setId("REF");
        tdoType.getSDOOrDA().add(tda);

        TDataTypeTemplates tDataTypeTemplates = new TDataTypeTemplates();
        tDataTypeTemplates.getLNodeType().add(tlNodeType);
        tDataTypeTemplates.getDOType().add(tdoType);


        //ied Client
        TDOI tdoi = new TDOI();
        tdoi.setName(doName);
        TLDevice tlDevice = new TLDevice();
        tlDevice.setInst("LD_ADD");
        TInputs tInputs = new TInputs();
        LN0 ln0 = new LN0();
        ln0.setInputs(tInputs);
        tlDevice.setLN0(ln0);

        TLDevice tlDevice1 = new TLDevice();
        tlDevice1.setLN0(new LN0());
        tlDevice1.setInst(LD_LDSUIED);
        TLN tln1 = new TLN();
        tln1.getLnClass().add(lnClass);
        tln1.setLnType("T1");
        tln1.getDOI().add(tdoi);
        tlDevice1.getLN().add(tln1);
        TServer tServer1 = new TServer();
        tServer1.getLDevice().add(tlDevice1);
        tServer1.getLDevice().add(tlDevice);
        TAccessPoint tAccessPoint1 = new TAccessPoint();
        tAccessPoint1.setName("AP_NAME");
        tAccessPoint1.setServer(tServer1);
        TIED tied1 = new TIED();
        tied1.setName(IED_NAME_1);
        tied1.getAccessPoint().add(tAccessPoint1);

        //ied Source
        TLDevice tlDevice2 = new TLDevice();
        tlDevice2.setInst("Inst_2");
        tlDevice2.setLdName("LD_Name");
        tlDevice2.setLN0(new LN0());
        TServer tServer2 = new TServer();
        tServer2.getLDevice().add(tlDevice2);
        TAccessPoint tAccessPoint2 = new TAccessPoint();
        tAccessPoint2.setName("AP_NAME");
        tAccessPoint2.setServer(tServer2);
        TIED tied2 = new TIED();
        tied2.setName(IED_NAME_2);
        tied2.getAccessPoint().add(tAccessPoint2);
        //SCL file
        SCL scd = new SCL();
        scd.getIED().add(tied1);
        scd.getIED().add(tied2);
        THeader tHeader = new THeader();
        tHeader.setRevision("1");
        scd.setHeader(tHeader);
        scd.setDataTypeTemplates(tDataTypeTemplates);

        return new SclRootAdapter(scd);
    }

    public static List<TVal> getDaiValues(LDeviceAdapter lDeviceAdapter, String lnClass, String inst, String doName, String daName) {
        return getDAIAdapters(lDeviceAdapter, lnClass, inst, doName, daName)
                .map(daiAdapter -> daiAdapter.getCurrentElem().getVal())
                .flatMap(List::stream)
                .toList();
    }

    public static Stream<DOIAdapter.DAIAdapter> getDAIAdapters(LDeviceAdapter lDeviceAdapter, String lnClass, String inst, String doName, String daName) {
        return lDeviceAdapter.getLNAdapters().stream()
                .filter(lnAdapter -> lnClassEquals(lnAdapter.getCurrentElem().getLnClass(), lnClass)
                && lnAdapter.getCurrentElem().getInst().equals(inst))
                .map(lnAdapter -> lnAdapter.getDOIAdapterByName(doName))
                .map(doiAdapter -> (DOIAdapter.DAIAdapter) doiAdapter.getDataAdapterByName(daName));
    }
}
