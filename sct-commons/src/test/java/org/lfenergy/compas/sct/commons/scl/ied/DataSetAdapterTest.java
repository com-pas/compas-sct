// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DataSetAdapterTest {

    @Test
    void amChildElementRef_should_succeed() {
        //Given
        LN0 ln0 = new LN0();
        TDataSet dataSet = new TDataSet();
        ln0.getDataSet().add(dataSet);
        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);
        DataSetAdapter dataSetAdapter = new DataSetAdapter(ln0Adapter, dataSet);
        //When
        boolean result = dataSetAdapter.amChildElementRef();
        //Then
        assertThat(result).isTrue();
    }

    @Test
    void elementXPath_should_return_relative_xpath() {
        //Given
        TDataSet dataSet = new TDataSet();
        dataSet.setName("dataSetName");
        DataSetAdapter dataSetAdapter = new DataSetAdapter(null, dataSet);
        //When
        String elementXPath = dataSetAdapter.elementXPath();
        //Then,
        assertThat(elementXPath).isEqualTo("DataSet[@name=\"dataSetName\"]");
    }

    @Test
    void findFCDA_should_return_found_FCDA() {
        //Given
        TFCDA fcda = createFCDA();
        TDataSet dataSet = new TDataSet();
        dataSet.getFCDA().add(fcda);
        DataSetAdapter dataSetAdapter = new DataSetAdapter(null, dataSet);
        //When
        Optional<TFCDA> result = dataSetAdapter.findFCDA("LDINST", null, "LLN0", null, "DoName", "daName", TFCEnum.ST);
        //Then
        assertThat(result).isPresent();
    }

    @Test
    void findFCDA_when_no_FCDA_found_should_return_empty() {
        //Given
        TDataSet dataSet = new TDataSet();
        DataSetAdapter dataSetAdapter = new DataSetAdapter(null, dataSet);
        //When
        Optional<TFCDA> result = dataSetAdapter.findFCDA("any", "any", "any", "any", "any", "any", TFCEnum.ST);
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void createFCDAIfNotExists_should_create_new_FCDA() {
        //Given
        TDataSet dataSet = new TDataSet();
        DataSetAdapter dataSetAdapter = new DataSetAdapter(null, dataSet);
        //When
        TFCDA result = dataSetAdapter.createFCDAIfNotExists("LDINST", null, "LLN0", null, "DoName", "daName", TFCEnum.ST);
        //Then
        assertThat(dataSet.getFCDA()).hasSize(1)
            .first()
            .isSameAs(result);
        assertThat(dataSet.getFCDA().get(0))
            .extracting(TFCDA::getLdInst, TFCDA::isSetPrefix, TFCDA::getLnClass, TFCDA::isSetLnInst, TFCDA::getDoName, TFCDA::getDaName, TFCDA::getFc)
            .containsExactly("LDINST", false, List.of("LLN0"), false, "DoName", "daName", TFCEnum.ST);
    }

    @Test
    void createFCDAIfNotExists_when_FCDA_already_exists_should_not_create_FCDA() {
        //Given
        TDataSet dataSet = new TDataSet();
        TFCDA existingFcda = createFCDA();
        dataSet.getFCDA().add(existingFcda);
        DataSetAdapter dataSetAdapter = new DataSetAdapter(null, dataSet);
        //When
        TFCDA result = dataSetAdapter.createFCDAIfNotExists("LDINST", null, "LLN0", null, "DoName", "daName", TFCEnum.ST);
        //Then
        assertThat(dataSet.getFCDA()).hasSize(1)
            .first()
            .isSameAs(result)
            .isSameAs(existingFcda);
    }

    private static TFCDA createFCDA() {
        TFCDA existingFcda = new TFCDA();
        existingFcda.setLdInst("LDINST");
        existingFcda.setPrefix("");
        existingFcda.getLnClass().add("LLN0");
        existingFcda.setLnInst("");
        existingFcda.setDoName("DoName");
        existingFcda.setDaName("daName");
        existingFcda.setFc(TFCEnum.ST);
        return existingFcda;
    }
}
