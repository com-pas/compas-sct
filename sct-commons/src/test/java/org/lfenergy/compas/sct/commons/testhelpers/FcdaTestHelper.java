/*
 * // SPDX-FileCopyrightText: 2023 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.testhelpers;

import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.sct.commons.model.cb_po.FCDAs;
import org.lfenergy.compas.sct.commons.model.cb_po.PO;
import org.lfenergy.compas.sct.commons.model.cb_po.TFcdaFilter;
import org.lfenergy.compas.sct.commons.model.cb_po.Tfc;

import java.util.List;

public final class FcdaTestHelper {

    public static PO createFcdaFilterList() {
        PO allowedFcdas = new PO();
        FCDAs fcdAs = new FCDAs();
        List.of(new FCDARecord("LD_INST21", "ANCR", "1", "", "DoName", "daNameST", TFCEnum.ST),
                        new FCDARecord("LD_INST21", "ANCR", "1", "", "DoName", "daNameMX", TFCEnum.MX),
                        new FCDARecord("LD_INST21", "ANCR", "1", "", "OtherDoName", "daNameST", TFCEnum.ST),
                        new FCDARecord("LD_INST21", "ANCR", "1", "", "DoWithInst", "daNameST", TFCEnum.ST),
                        new FCDARecord("LD_INST21", "ANCR", "1", "", "DoWithInst.subDo", "daNameST", TFCEnum.ST),
                        new FCDARecord("LD_INST21", "ANCR", "1", "", "FirstDo", "daNameST", TFCEnum.ST),
                        new FCDARecord("LD_INST21", "ANCR", "1", "", "SecondDo", "daNameST", TFCEnum.ST),
                        new FCDARecord("LD_INST21", "ANCR", "1", "", "ThirdDo", "daNameST", TFCEnum.ST))
                .forEach(fcdaRecord -> {
                    TFcdaFilter tFcdaFilter = new TFcdaFilter();
                    tFcdaFilter.setLdInst(fcdaRecord.ldInst());
                    tFcdaFilter.setLnClass(fcdaRecord.lnClass());
                    tFcdaFilter.setPrefix(fcdaRecord.prefix());
                    tFcdaFilter.setDoName(fcdaRecord.doName());
                    tFcdaFilter.setDaName(fcdaRecord.daName());
                    tFcdaFilter.setFc(Tfc.fromValue(fcdaRecord.fc().value()));
                    fcdAs.getFCDA().add(tFcdaFilter);
                });
        allowedFcdas.setFCDAs(fcdAs);
        return allowedFcdas;
    }
}
