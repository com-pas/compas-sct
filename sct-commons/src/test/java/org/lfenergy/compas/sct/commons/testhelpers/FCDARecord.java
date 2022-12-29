// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

/**
 * Record to easily compare FCDA with AssertJ (xjc does not implement an equals method for TFCDA class)
 */
public record FCDARecord(String ldInst, String lnClass, String lnInst, String prefix, String doName, String daName, TFCEnum fc) {
    static public FCDARecord toFCDARecord(TFCDA tfcda) {
        return new FCDARecord(
            tfcda.getLdInst(),
            tfcda.isSetLnClass() ? tfcda.getLnClass().get(0) : null,
            tfcda.getLnInst(),
            tfcda.getPrefix(),
            tfcda.getDoName(),
            tfcda.getDaName(),
            tfcda.getFc());
    }
}
