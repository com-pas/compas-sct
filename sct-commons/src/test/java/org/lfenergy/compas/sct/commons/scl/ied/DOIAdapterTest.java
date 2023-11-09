// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.CommonConstants;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newVal;

class DOIAdapterTest {

    @Test
    // Test should be modified to reflect each test case and remove no concerned test and assertions.
    @Tag("issue-321")
    void testConstructor() {
        // Given
        LN0 ln0 = new LN0();
        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);

        TDOI tdoi = new TDOI();
        tdoi.setName("Do");
        ln0.getDOI().add(tdoi);
        // test amChildElement
        // When Then
        DOIAdapter doiAdapter = assertDoesNotThrow(() -> new DOIAdapter(ln0Adapter, tdoi));

        // test tree map
        // Given
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo2");
        tdoi.getSDIOrDAI().add(tsdi);
        // When Then
        assertThatCode(() -> doiAdapter.getStructuredDataAdapterByName("sdo2")).doesNotThrowAnyException();
        // When Then
        assertThatThrownBy(() -> doiAdapter.getStructuredDataAdapterByName("sdo3")).isInstanceOf(ScdException.class);
        // Given
        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tdoi.getSDIOrDAI().add(tdai);
        // When Then
        assertThatCode(() -> doiAdapter.getDataAdapterByName("angRef")).doesNotThrowAnyException();
        // When Then
        assertThatThrownBy(() -> doiAdapter.getStructuredDataAdapterByName("bda")).isInstanceOf(ScdException.class);
        // When Then
        assertThatThrownBy(() -> doiAdapter.getDataAdapterByName("bda")).isInstanceOf(ScdException.class);
    }

    @Test
    @Tag("issue-321")
    void testInnerDAIAdapter() {
        // Given
        String doName = "Do";
        String daName = "angRef";
        // When
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter(doName, daName);
        // Then
        assertThat(daiAdapter.getCurrentElem().isSetValImport()).isFalse();
        // Given
        daiAdapter.setValImport(true);
        // When Then
        assertThat(daiAdapter.getCurrentElem().isSetValImport()).isTrue();

    }

    @Test
    @Tag("issue-321")
    void testInnerDAIAdapterTestUpdateWithMapAsArg() {
        // Given
        final String TOTO = "toto";
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        daiAdapter.setValImport(true);
        final Map<Long, String> vals = Collections.singletonMap(0L, TOTO);
        // update DAI val
        // When Then
        assertThatCode(() -> daiAdapter.update(vals)).doesNotThrowAnyException();
        assertThat(daiAdapter.getCurrentElem().getVal()).isNotEmpty();
        TVal tVal = daiAdapter.getCurrentElem().getVal().get(0);
        assertThat(tVal.isSetSGroup()).isFalse();
        // Given
        final Map<Long, String> vals2 = new HashMap<>();
        vals2.put(1L, TOTO);
        vals2.put(0L, TOTO);
        // When Then
        assertThatCode(() -> daiAdapter.update(vals2)).doesNotThrowAnyException();
        assertThat(daiAdapter.getCurrentElem().getVal()).isNotEmpty();
        tVal = daiAdapter.getCurrentElem().getVal().get(0);
        assertThat(tVal.isSetSGroup()).isFalse();
    }

    @Test
    @Tag("issue-321")
    void testInnerDAIAdapterTestUpdate() {
        // Given
        final String TOTO = "toto";
        // When
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        daiAdapter.setValImport(false);
        // When Then
        assertThatThrownBy(() -> daiAdapter.update(0L, TOTO)).isInstanceOf(ScdException.class);
        // Given
        daiAdapter.setValImport(true);
        // When Then
        assertThatCode(() -> daiAdapter.update(0L, TOTO)).doesNotThrowAnyException();
        // Given
        final Map<Long, String> vals2 = new HashMap<>();
        vals2.put(1L, TOTO);
        vals2.put(2L, TOTO);
        // When Then
        assertThatCode(() -> daiAdapter.update(vals2)).doesNotThrowAnyException();
        // Given
        vals2.put(2L, TOTO + "1");
        // When Then
        assertThatCode(() -> daiAdapter.update(vals2)).doesNotThrowAnyException();
    }

    @Test
    void DAIAdapter_update_when_valImport_is_set_to_true_should_update_val() {
        // Given
        String newValue = "newValue";
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        daiAdapter.setValImport(true);
        daiAdapter.getCurrentElem().getVal().add(newVal("oldValue"));
        // When
        daiAdapter.update(0L, newValue);
        // Then
        assertThat(daiAdapter.getCurrentElem().getVal()).hasSize(1)
                .first().extracting(TVal::getValue, TVal::isSetSGroup)
                .containsExactly("newValue", false);
    }

    @Test
    void DAIAdapter_update_when_valImport_is_not_set_should_update_val() {
        // Given
        String newValue = "newValue";
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        daiAdapter.getCurrentElem().unsetValImport();
        daiAdapter.getCurrentElem().getVal().add(newVal("oldValue"));
        // When
        daiAdapter.update(0L, newValue);
        // Then
        assertThat(daiAdapter.getCurrentElem().getVal()).hasSize(1)
                .first().extracting(TVal::getValue, TVal::isSetSGroup)
                .containsExactly("newValue", false);
    }

    @Test
    void DAIAdapter_update_when_sGroup_is_set_should_update_single_val() {
        // Given
        String newValue = "newValue";
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        daiAdapter.setValImport(true);
        long sGroup1 = 1;
        long sGroup2 = 2;
        daiAdapter.getCurrentElem().getVal().add(newVal("oldValue1", sGroup1));
        daiAdapter.getCurrentElem().getVal().add(newVal("oldValue2", sGroup2));
        // When
        daiAdapter.update(sGroup2, newValue);
        // Then
        assertThat(daiAdapter.getCurrentElem().getVal()).extracting(TVal::getValue, TVal::getSGroup)
                .containsExactly(
                        Tuple.tuple("oldValue1", sGroup1),
                        Tuple.tuple("newValue", sGroup2));
    }

    @Test
    void DAIAdapter_update_when_valImport_is_set_to_false_should_throw_exception() {
        // Given
        String newValue = "newValue";
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        daiAdapter.setValImport(false);
        // When & Then
        assertThatThrownBy(() -> daiAdapter.update(0L, newValue))
                .isInstanceOf(ScdException.class);
    }

    @Test
    void DAIAdapter_update_when_valImport_is_set_to_false_but_da_is_Mod_StVal_should_update_value() {
        // Given
        String newValue = "newValue";
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter(CommonConstants.MOD_DO_NAME, CommonConstants.STVAL_DA_NAME);
        daiAdapter.setValImport(false);
        // When
        daiAdapter.update(0L, newValue);
        // Then
        assertThat(daiAdapter.getCurrentElem().getVal()).hasSize(1)
                .first().extracting(TVal::getValue, TVal::isSetSGroup)
                .containsExactly("newValue", false);
    }

    @Test
    @Tag("issue-321")
    void testFindDeepestMatch() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        // When Then
        DOIAdapter doiAdapter = assertDoesNotThrow(() -> ln0Adapter.getDOIAdapterByName("Do"));
        // Given
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        DaTypeName daTypeName = new DaTypeName("antRef.bda1.bda2.bda3");
        Pair<? extends IDataAdapter, Integer> pair = doiAdapter.findDeepestMatch(
                doTypeName.getStructNames(), 0, false
        );
        // When
        SDIAdapter lastSDOIAdapter = (SDIAdapter) pair.getLeft();
        // Then
        assertThat(pair.getRight()).isEqualTo(1);
        assertThat(lastSDOIAdapter)
                .isNotNull()
                .isInstanceOf(SDIAdapter.class);
        // When
        IDataParentAdapter firstDAIAdapter = lastSDOIAdapter.getStructuredDataAdapterByName(daTypeName.getName());

        // When
        pair = firstDAIAdapter.findDeepestMatch(
                daTypeName.getStructNames(), 0, true
        );

        // Then
        assertThat(pair.getRight()).isEqualTo(2);
        assertThat(pair.getLeft()).isNotNull();
        assertThat(pair.getLeft()).isInstanceOf(SDIAdapter.DAIAdapter.class);
    }

    private DOIAdapter.DAIAdapter initInnerDAIAdapter(String doName, String daName) {
        TDOI tdoi = new TDOI();
        tdoi.setName(doName);
        DOIAdapter doiAdapter = new DOIAdapter(null, tdoi);

        TDAI tdai = new TDAI();
        tdai.setName(daName);
        tdoi.getSDIOrDAI().add(tdai);

        return new DOIAdapter.DAIAdapter(doiAdapter, tdai);
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        // Given
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        assertThat(daiAdapter.getCurrentElem().getPrivate()).isEmpty();

        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        // When
        daiAdapter.addPrivate(tPrivate);
        // Then
        assertThat(daiAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }

    @Test
    void DOI_elementXPath_should_return_expected_xpath_value() {
        // Given
        TDOI tdoi = new TDOI();
        tdoi.setName("doName");
        DOIAdapter doiAdapter = new DOIAdapter(null, new TDOI());
        DOIAdapter namedDoiAdapter = new DOIAdapter(null, tdoi);
        // When
        String elementXPathResult = doiAdapter.elementXPath();
        String namedElementXPathResult = namedDoiAdapter.elementXPath();
        // Then
        assertThat(elementXPathResult).isEqualTo("DOI[not(@name)]");
        assertThat(namedElementXPathResult).isEqualTo("DOI[@name=\"doName\"]");
    }

    @Test
    void DAI_elementXPath_should_return_expected_xpath_value() {
        // Given
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        // When
        String result = daiAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DAI[@name=\"da\"]");
    }

    @Test
    void findDataAdapterByName_when_DA_name_exist_should_return_DAIAdapter() {
        // Given
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        DOIAdapter doiAdapter = daiAdapter.getParentAdapter();

        // When
        Optional<AbstractDAIAdapter<?>> result = doiAdapter.findDataAdapterByName("da");

        // Then
        assertThat(result)
                .isPresent()
                .map(daiAdapter1 -> daiAdapter1.getCurrentElem().getName())
                .isEqualTo(Optional.of("da"));
    }

    @Test
    void findDataAdapterByName_when_DA_name_not_exist_should_return_Empty() {
        // Given
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        DOIAdapter doiAdapter = daiAdapter.getParentAdapter();

        // When
        Optional<AbstractDAIAdapter<?>> result = doiAdapter.findDataAdapterByName("wrong");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void updateDaiFromExtRef_when_ExtRef_desc_suffix_ends_with_1_should_update_setSrcXX_values() {
        // Given
        DOIAdapter doiAdapter = createDOIAdapterInScl();
        TDAI daiSrcRef = new TDAI();
        daiSrcRef.setName(SETSRCREF_DA_NAME);
        TDAI daiSrcCb = new TDAI();
        daiSrcCb.setName(SETSRCCB_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcRef);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcCb);

        TExtRef extRef1 = givenExtRef(1, true);

        // When
        List<SclReportItem> sclReportItems = doiAdapter.updateDaiFromExtRef(List.of(extRef1));

        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCREF_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_1LD_INST_1/PREFIX_1ANCR1.DO_NAME_1");
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCCB_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_1LD_INST_1/SRC_PREFIX_1LLN0SRC_LN_INST_1.CB_NAME_1");
    }

    @Test
    void updateDaiFromExtRef_when_ExtRef_not_contains_CB_should_update_setSrcRef_value_but_not_setSrcCB() {
        // Given
        DOIAdapter doiAdapter = createDOIAdapterInScl();
        TDAI daiSrcRef = new TDAI();
        daiSrcRef.setName(SETSRCREF_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcRef);

        TExtRef extRef1 = givenExtRef(1, false);

        // When
        List<SclReportItem> sclReportItems = doiAdapter.updateDaiFromExtRef(List.of(extRef1));

        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCREF_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_1LD_INST_1/PREFIX_1ANCR1.DO_NAME_1");
        assertThatThrownBy(() -> getDaiValOfDoi(doiAdapter, SETSRCCB_DA_NAME)).isInstanceOf(ScdException.class);
    }

    @Test
    void updateDaiFromExtRef_when_ExtRef_desc_suffix_ends_with_1_and_3_should_update_setSrcXX_and_setTstXX_values() {
        // Given
        DOIAdapter doiAdapter = createDOIAdapterInScl();
        TDAI daiSrcRef = new TDAI();
        daiSrcRef.setName(SETSRCREF_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcRef);
        TDAI daiSrcCb = new TDAI();
        daiSrcCb.setName(SETSRCCB_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcCb);
        TDAI daiTstRef = new TDAI();
        daiTstRef.setName(SETTSTREF_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiTstRef);
        TDAI daiTstCb = new TDAI();
        daiTstCb.setName(SETTSTCB_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiTstCb);

        TExtRef extRef1 = givenExtRef(1, true);
        TExtRef extRef3 = givenExtRef(3, true);

        // When
        List<SclReportItem> sclReportItems = doiAdapter.updateDaiFromExtRef(List.of(extRef1, extRef3));

        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCREF_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_1LD_INST_1/PREFIX_1ANCR1.DO_NAME_1");
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCCB_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_1LD_INST_1/SRC_PREFIX_1LLN0SRC_LN_INST_1.CB_NAME_1");
        assertThat(getDaiValOfDoi(doiAdapter, SETTSTREF_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_3LD_INST_3/PREFIX_3ANCR3.DO_NAME_3");
        assertThat(getDaiValOfDoi(doiAdapter, SETTSTCB_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_3LD_INST_3/SRC_PREFIX_3LLN0SRC_LN_INST_3.CB_NAME_3");
    }

    @Test
    void updateDaiFromExtRef_when_ExtRef_desc_suffix_ends_with_1_and_3_without_CB_should_update_only_setSrcRef_and_setTstRef_values() {
        // Given
        DOIAdapter doiAdapter = createDOIAdapterInScl();
        TDAI daiSrcRef = new TDAI();
        daiSrcRef.setName(SETSRCREF_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcRef);
        TDAI daiSrcCb = new TDAI();
        daiSrcCb.setName(SETSRCCB_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcCb);
        TDAI daiTstRef = new TDAI();
        daiTstRef.setName(SETTSTREF_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiTstRef);
        TDAI daiTstCb = new TDAI();
        daiTstCb.setName(SETTSTCB_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiTstCb);

        TExtRef extRef1 = givenExtRef(1, false);
        TExtRef extRef3 = givenExtRef(3, false);

        // When
        List<SclReportItem> sclReportItems = doiAdapter.updateDaiFromExtRef(List.of(extRef1, extRef3));

        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCREF_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_1LD_INST_1/PREFIX_1ANCR1.DO_NAME_1");
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCCB_DA_NAME))
                .isNotPresent();
        assertThat(getDaiValOfDoi(doiAdapter, SETTSTREF_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_3LD_INST_3/PREFIX_3ANCR3.DO_NAME_3");
        assertThat(getDaiValOfDoi(doiAdapter, SETTSTCB_DA_NAME))
                .isNotPresent();
    }

    @Test
    void updateDaiFromExtRef_when_none_ExtRef_endin_with_1_should_return_warning_report() {
        // Given
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        DOIAdapter doiAdapter = daiAdapter.getParentAdapter();
        TDAI daiSrcRef = new TDAI();
        daiSrcRef.setName(SETSRCREF_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcRef);

        TExtRef extRef3 = givenExtRef(3, false);

        // When
        List<SclReportItem> sclReportItems = doiAdapter.updateDaiFromExtRef(List.of(extRef3));

        // Then
        assertThat(sclReportItems)
                .isNotEmpty()
                .extracting(SclReportItem::message)
                .contains("The DOI /DOI[@name=\"Do\"] can't be bound with an ExtRef");
        assertThat(doiAdapter.getDataAdapterByName(SETSRCREF_DA_NAME)).isNotNull();
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCREF_DA_NAME)).isNotPresent();
    }

    @Test
    void updateDaiFromExtRef_when_no_DAI_name_setSrcRef_should_create_DAI() {
        // Given
        DOIAdapter doiAdapter = createDOIAdapterInScl();

        TExtRef extRef1 = givenExtRef(1, false);

        // When
        List<SclReportItem> sclReportItems = doiAdapter.updateDaiFromExtRef(List.of(extRef1));

        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(doiAdapter.getDataAdapterByName(SETSRCREF_DA_NAME)).isNotNull();
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCREF_DA_NAME))
                .isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_1LD_INST_1/PREFIX_1ANCR1.DO_NAME_1");
    }

    @Test
    void updateDaiFromExtRef_when_no_ExtRef_in_LNode_should_return_filled_ReportItem() {
        // Given
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        DOIAdapter doiAdapter = daiAdapter.getParentAdapter();

        // When
        List<SclReportItem> sclReportItems = doiAdapter.updateDaiFromExtRef(List.of());

        // Then
        assertThat(sclReportItems)
                .isNotEmpty();
        assertThat(sclReportItems)
                .extracting(SclReportItem::message)
                .contains("The DOI /DOI[@name=\"Do\"] can't be bound with an ExtRef");
    }

    @Test
    void updateDaiFromExtRef_when_optional_ExtRef_attributes_are_missing_should_compose_correct_name() {
        // Given
        DOIAdapter doiAdapter = createDOIAdapterInScl();
        TDAI daiSrcRef = new TDAI();
        daiSrcRef.setName(SETSRCREF_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcRef);
        TDAI daiSrcCb = new TDAI();
        daiSrcCb.setName(SETSRCCB_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcCb);
        TDAI daiTstRef = new TDAI();
        daiTstRef.setName(SETTSTREF_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiTstRef);
        TDAI daiTstCb = new TDAI();
        daiTstCb.setName(SETTSTCB_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiTstCb);

        TExtRef extRef1 = new TExtRef();
        extRef1.setDesc("ExtRef_desc_1");
        extRef1.setIedName("IED_NAME_1");
        extRef1.setLdInst("LD_INST_1");
        extRef1.getLnClass().add("LN_CLASS_1");
        extRef1.setDoName("DO_NAME_1");
        TExtRef extRef3 = new TExtRef();
        extRef3.setDesc("ExtRef_desc_3");
        extRef3.setIedName("IED_NAME_3");
        extRef3.setLdInst("LD_INST_3");
        extRef3.getLnClass().add("LN_CLASS_3");
        extRef3.setDoName("DO_NAME_3");

        // When
        List<SclReportItem> sclReportItems = doiAdapter.updateDaiFromExtRef(List.of(extRef1, extRef3));

        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCREF_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_1LD_INST_1/LN_CLASS_1.DO_NAME_1");
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCCB_DA_NAME))
                .isNotPresent();
        assertThat(getDaiValOfDoi(doiAdapter, SETTSTREF_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("IED_NAME_3LD_INST_3/LN_CLASS_3.DO_NAME_3");
        assertThat(getDaiValOfDoi(doiAdapter, SETTSTCB_DA_NAME))
                .isNotPresent();
    }

    @Test
    void updateDaiFromExtRef_when_ExtRef_desc_not_end_with_1_should_throw_exception() {
        // Given
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do", "da");
        DOIAdapter doiAdapter = daiAdapter.getParentAdapter();
        TDAI daiSrcRef = new TDAI();
        daiSrcRef.setName(SETSRCREF_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcRef);
        TDAI daiSrcCb = new TDAI();
        daiSrcCb.setName(SETSRCCB_DA_NAME);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcCb);

        TExtRef extRef1 = new TExtRef();
        extRef1.setDesc("ExtRefDesc");
        List<TExtRef> extRefList = List.of(extRef1);

        // When Then
        assertThatThrownBy(() -> doiAdapter.updateDaiFromExtRef(extRefList))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void updateDai_when_dai_not_present_in_do_and_is_updatable_should_create_and_update_value() {
        // Given
        DOIAdapter doiAdapter = createDOIAdapterInScl();
        // When
        Optional<SclReportItem> sclReportItems = doiAdapter.updateDAI(SETSRCREF_DA_NAME, "new value");
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCREF_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("new value");
    }

    @Test
    void updateDai_when_dai_present_in_do_and_is_updatable_should_update_value() {
        // Given
        DOIAdapter doiAdapter = createDOIAdapterInScl();
        TVal tVal = new TVal();
        tVal.setValue("old value");
        TDAI daiSrcRef = new TDAI();
        daiSrcRef.setName(SETSRCREF_DA_NAME);
        daiSrcRef.getVal().add(tVal);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcRef);

        // When
        Optional<SclReportItem> sclReportItems = doiAdapter.updateDAI(SETSRCREF_DA_NAME, "new value");

        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCREF_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isNotEqualTo("old value")
                .isEqualTo("new value");
    }

    @Test
    void updateDai_when_dai_is_not_updatable_should_not_update_value_and_return_warning_message() {
        // Given
        DOIAdapter doiAdapter = createDOIAdapterInScl();
        TVal tVal = new TVal();
        tVal.setValue("old value");
        TDAI daiSrcRef = new TDAI();
        daiSrcRef.setName(SETSRCREF_DA_NAME);
        daiSrcRef.getVal().add(tVal);
        daiSrcRef.setValImport(false);
        doiAdapter.getCurrentElem().getSDIOrDAI().add(daiSrcRef);

        // When
        Optional<SclReportItem> sclReportItems = doiAdapter.updateDAI(SETSRCREF_DA_NAME, "new value");

        // Then
        assertThat(sclReportItems).isPresent()
                .get().extracting(SclReportItem::message)
                .isEqualTo("The DAI setSrcRef cannot be updated");
        assertThat(getDaiValOfDoi(doiAdapter, SETSRCREF_DA_NAME)).isPresent()
                .get().extracting(TVal::getValue)
                .isEqualTo("old value");
    }

    private static Optional<TVal> getDaiValOfDoi(DOIAdapter doiAdapter, String daName) {
        return doiAdapter.getDataAdapterByName(daName).getCurrentElem().getVal().stream().findFirst();
    }

    private static TExtRef givenExtRef(int num, boolean withCbName) {
        TExtRef extRef1 = new TExtRef();
        extRef1.setIedName("IED_NAME_" + num);
        extRef1.setDesc("ExtRef_desc_" + num);
        extRef1.setLdInst("LD_INST_" + num);
        extRef1.setSrcPrefix("SRC_PREFIX_" + num);
        extRef1.setSrcLNInst("SRC_LN_INST_" + num);
        extRef1.getLnClass().add("ANCR");
        extRef1.setLnInst(Integer.toString(num));
        extRef1.setPrefix("PREFIX_" + num);
        extRef1.setDoName("DO_NAME_" + num);
        if (withCbName) {
            extRef1.setSrcCBName("CB_NAME_" + num);
        }
        return extRef1;
    }

    private DOIAdapter createDOIAdapterInScl() {
        TDOI tdoi = new TDOI();
        tdoi.setName("InRef");

        LN0 ln0 = new LN0();
        ln0.setLnType("T1");
        ln0.getDOI().add(tdoi);
        TLDevice tlDevice = new TLDevice();
        tlDevice.setInst("Inst");
        tlDevice.setLN0(ln0);
        TServer tServer = new TServer();
        tServer.getLDevice().add(tlDevice);
        TAccessPoint tAccessPoint = new TAccessPoint();
        tAccessPoint.setName("AP_NAME");
        tAccessPoint.setServer(tServer);
        TIED tied = new TIED();
        tied.setName("IED_NAME");
        tied.getAccessPoint().add(tAccessPoint);
        //SCL file
        SCL scd = new SCL();
        scd.getIED().add(tied);
        THeader tHeader = new THeader();
        tHeader.setRevision("1");
        scd.setHeader(tHeader);
        // DataTypeTemplate
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("T1");
        tlNodeType.getLnClass().add("LLN0");
        TDO tdo = new TDO();
        tdo.setName("InRef");
        tdo.setType("REF");
        tlNodeType.getDO().add(tdo);
        TDOType tdoType = new TDOType();
        tdoType.setId("REF");
        TDA tda1 = createDa(SETSRCREF_DA_NAME);
        TDA tda2 = createDa(SETSRCCB_DA_NAME);
        TDA tda3 = createDa(SETTSTREF_DA_NAME);
        TDA tda4 = createDa(SETTSTCB_DA_NAME);
        tdoType.getSDOOrDA().addAll(List.of(tda1, tda2, tda3, tda4));

        TDataTypeTemplates tDataTypeTemplates = new TDataTypeTemplates();
        tDataTypeTemplates.getLNodeType().add(tlNodeType);
        tDataTypeTemplates.getDOType().add(tdoType);
        scd.setDataTypeTemplates(tDataTypeTemplates);

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LN0Adapter ln0Adapter = sclRootAdapter.getIEDAdapterByName("IED_NAME").getLDeviceAdapterByLdInst("Inst").getLN0Adapter();

        return new DOIAdapter(ln0Adapter, tdoi);
    }

    private TDA createDa(String daName) {
        TDA tda1 = new TDA();
        tda1.setName(daName);
        tda1.setValImport(true);
        tda1.setBType(TPredefinedBasicTypeEnum.OBJ_REF);
        tda1.setFc(TFCEnum.SP);
        return tda1;
    }
}
