// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.scl2007b4.model.TLNodeType;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LnodeTypeServiceTest {

    @Test
    void getLnodeTypes() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDataTypeTemplates dataTypeTemplates = std.getDataTypeTemplates();
        LnodeTypeService lnodeTypeService = new LnodeTypeService();

        //When
        List<TLNodeType> tlnodeTypes = lnodeTypeService.getLnodeTypes(dataTypeTemplates).toList();

        //Then
        assertThat(tlnodeTypes)
                .hasSize(15)
                .flatExtracting(TLNodeType::getLnClass)
                .containsExactly("LPAI",
                        "LLN0",
                        "LLN0",
                        "TCTR",
                        "LSET",
                        "LCCH",
                        "LPCP",
                        "LGOS",
                        "LTMS",
                        "XSWI",
                        "GAPC",
                        "TVTR",
                        "LSYN",
                        "XSWI",
                        "LSET");
    }

    @Test
    void getFilteredLnodeTypes() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDataTypeTemplates dataTypeTemplates = std.getDataTypeTemplates();
        LnodeTypeService lnodeTypeService = new LnodeTypeService();

        //When
        List<TLNodeType> tlnodeTypes =
                lnodeTypeService.getFilteredLnodeTypes(dataTypeTemplates, tlNodeType -> tlNodeType.getLnClass().contains("LPAI")).toList();

        //Then
        assertThat(tlnodeTypes)
                .hasSize(1)
                .extracting(TLNodeType::getLnClass, TLNodeType::getId)
                .containsExactly(Tuple.tuple(List.of("LPAI"), "RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0"));
    }

    @Test
    void findLnodeType() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDataTypeTemplates dataTypeTemplates = std.getDataTypeTemplates();
        LnodeTypeService lnodeTypeService = new LnodeTypeService();

        //When
        TLNodeType tlnodeType =
                lnodeTypeService.findLnodeType(dataTypeTemplates, tlNodeType -> tlNodeType.getLnClass().contains("LPAI")).orElseThrow();

        //Then
        assertThat(tlnodeType)
                .extracting(TLNodeType::getLnClass, TLNodeType::getId)
                .containsExactly(List.of("LPAI"), "RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0");
    }
}