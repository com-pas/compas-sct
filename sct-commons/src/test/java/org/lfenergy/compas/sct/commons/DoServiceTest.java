// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.scl2007b4.model.TLNodeType;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DoServiceTest {

    @Test
    void getDos() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLNodeType lnodeType = std.getDataTypeTemplates().getLNodeType().get(0);
        DoService doService = new DoService();

        //When
        List<TDO> dos = doService.getDos(lnodeType).toList();

        //Then
        assertThat(dos)
                .hasSize(14)
                .extracting(TDO::getName)
                .containsExactly("NumInput1",
                        "PolConnRef1",
                        "BrdPos",
                        "ConnName1",
                        "PhyHealth",
                        "ConnRef1",
                        "NamPlt",
                        "RfHz1",
                        "PhyNam",
                        "Beh",
                        "BrdNum",
                        "ARtgLow1",
                        "ARtgHigh1",
                        "AnIn1");
    }

    @Test
    void getFilteredDos() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLNodeType lnodeType = std.getDataTypeTemplates().getLNodeType().get(0);
        DoService doService = new DoService();

        //When
        List<TDO> dos = doService.getFilteredDos(lnodeType, tdo -> "NumInput1".equals(tdo.getName())).toList();

        //Then
        assertThat(dos)
                .hasSize(1)
                .extracting(TDO::getName, TDO::getType)
                .containsExactly(Tuple.tuple("NumInput1", "RTE_X_X_X_553F8AE90EC0448B1518B00F5EAABB58_ING_V1.0.0"));
    }

    @Test
    void findDo() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLNodeType lnodeType = std.getDataTypeTemplates().getLNodeType().get(0);
        DoService doService = new DoService();

        //When
        TDO tdo1 = doService.findDo(lnodeType, tdo -> "NumInput1".equals(tdo.getName())).orElseThrow();

        //Then
        assertThat(tdo1)
                .extracting(TDO::getName, TDO::getType)
                .containsExactly("NumInput1", "RTE_X_X_X_553F8AE90EC0448B1518B00F5EAABB58_ING_V1.0.0");
    }
}