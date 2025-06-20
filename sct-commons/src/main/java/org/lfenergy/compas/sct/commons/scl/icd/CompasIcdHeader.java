// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.icd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.TCompasIEDRedundancy;
import org.lfenergy.compas.scl2007b4.model.TCompasIEDType;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.math.BigInteger;
import java.util.Optional;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;

/**
 * Object representing '<compas:ICDHeader' , but with utility methods to manipulate the business
 * Do not include "currentElements" and so on inside this class
 */
@Getter
@EqualsAndHashCode
public class CompasIcdHeader {

    private String icdSystemVersionUUID;
    private final TCompasIEDType iedType;
    @EqualsAndHashCode.Exclude
    private BigInteger iedSubstationinstance;
    @EqualsAndHashCode.Exclude
    private BigInteger iedSystemVersioninstance;
    @EqualsAndHashCode.Exclude
    private String iedName;
    private final String vendorName;
    private final String iedModel;
    private TCompasIEDRedundancy iedRedundancy;
    @EqualsAndHashCode.Exclude
    private String bayLabel;
    private final String hwRev;
    private final String swRev;
    private final IcdHeader icdHeader;

    public CompasIcdHeader(String hwRev, String swRev, String iedType, String iedModel, String vendorName, IcdHeader icdHeader) {
        this.hwRev = hwRev;
        this.swRev = swRev;
        this.iedType = TCompasIEDType.fromValue(iedType);
        this.iedModel = iedModel;
        this.vendorName = vendorName;
        this.icdHeader = icdHeader;

    }
    public CompasIcdHeader(TCompasICDHeader compasICDHeader) {
        this.icdSystemVersionUUID = compasICDHeader.getICDSystemVersionUUID();
        this.iedType = compasICDHeader.getIEDType();
        this.iedSubstationinstance = compasICDHeader.getIEDSubstationinstance();
        this.iedSystemVersioninstance = compasICDHeader.getIEDSystemVersioninstance();
        this.iedName = compasICDHeader.getIEDName();
        this.vendorName = compasICDHeader.getVendorName();
        this.iedModel = compasICDHeader.getIEDmodel();
        this.iedRedundancy = compasICDHeader.getIEDredundancy();
        this.bayLabel = compasICDHeader.getBayLabel();
        this.hwRev = compasICDHeader.getHwRev();
        this.swRev = compasICDHeader.getSwRev();
        this.icdHeader = new IcdHeader(compasICDHeader.getHeaderId(), compasICDHeader.getHeaderVersion(), compasICDHeader.getHeaderRevision());
    }

    public TCompasICDHeader toTCompasICDHeader() {
        TCompasICDHeader tCompasICDHeader = new TCompasICDHeader();
        tCompasICDHeader.setICDSystemVersionUUID(icdSystemVersionUUID);
        tCompasICDHeader.setIEDType(iedType);
        tCompasICDHeader.setIEDSubstationinstance(iedSubstationinstance);
        tCompasICDHeader.setIEDSystemVersioninstance(iedSystemVersioninstance);
        tCompasICDHeader.setIEDName(iedName);
        tCompasICDHeader.setVendorName(vendorName);
        tCompasICDHeader.setIEDmodel(iedModel);
        tCompasICDHeader.setIEDredundancy(iedRedundancy);
        tCompasICDHeader.setBayLabel(bayLabel);
        tCompasICDHeader.setHwRev(hwRev);
        tCompasICDHeader.setSwRev(swRev);
        tCompasICDHeader.setHeaderId(icdHeader.headerId());
        tCompasICDHeader.setHeaderVersion(icdHeader.headerVersion());
        tCompasICDHeader.setHeaderRevision(icdHeader.headerRevision());
        return tCompasICDHeader;
    }

    public String getIcdSystemVersionUUID() {
        return Optional.of(icdSystemVersionUUID)
                .orElseThrow(() -> new ScdException(ICD_SYSTEM_VERSION_UUID + " is not present in COMPAS-ICDHeader in LNode"));
    }

    @Override
    public String toString() {
        return HEADER_ID + " = " + icdHeader.headerId() + ", " +
                HEADER_VERSION + " = " + icdHeader.headerVersion() + ", " +
                HEADER_REVISION + " = " + icdHeader.headerRevision() +
                " and " + ICD_SYSTEM_VERSION_UUID + " = " + icdSystemVersionUUID;
    }
}
