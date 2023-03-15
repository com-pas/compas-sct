// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;

import java.util.Map;

public class DataTypeUtils {
    public static DaTypeName createDa(String nameRef, TFCEnum fc, boolean valImport, Map<Long, String> daiValues) {
        DaTypeName resultDa = new DaTypeName(nameRef);
        resultDa.setFc(fc);
        resultDa.setBType(TPredefinedBasicTypeEnum.INT_8);
        resultDa.setValImport(valImport);
        resultDa.setDaiValues(daiValues);
        return resultDa;
    }

    public static DoTypeName createDo(String nameRef, TPredefinedCDCEnum cdc) {
        DoTypeName resultDo = new DoTypeName(nameRef);
        resultDo.setCdc(cdc);
        return resultDo;
    }

    public static ResumedDataTemplate createResumedDataTemplate(DoTypeName doTypeName, DaTypeName daTypeName) {
        ResumedDataTemplate rDtt = new ResumedDataTemplate();
        rDtt.setDoName(doTypeName);
        rDtt.setDaName(daTypeName);
        return rDtt;
    }

    private DataTypeUtils() {
        throw new UnsupportedOperationException("This is a utility class, it should not be instantiated.");
    }
}
