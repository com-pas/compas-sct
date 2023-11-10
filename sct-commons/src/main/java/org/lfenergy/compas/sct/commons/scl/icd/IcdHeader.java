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
public class IcdHeader {

    private final String icdSystemVersionUUID;
    private final TCompasIEDType iedType;
    @EqualsAndHashCode.Exclude
    private final BigInteger iedSubstationinstance;
    @EqualsAndHashCode.Exclude
    private final BigInteger iedSystemVersioninstance;
    @EqualsAndHashCode.Exclude
    private final String iedName;
    private final String vendorName;
    private final String iedModel;
    private final TCompasIEDRedundancy iedRedundancy;
    @EqualsAndHashCode.Exclude
    private final String bayLabel;
    private final String hwRev;
    private final String swRev;
    private final String headerId;
    private final String headerVersion;
    private final String headerRevision;

    public IcdHeader(TCompasICDHeader compasICDHeader) {
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
        this.headerId = compasICDHeader.getHeaderId();
        this.headerVersion = compasICDHeader.getHeaderVersion();
        this.headerRevision = compasICDHeader.getHeaderRevision();
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
        tCompasICDHeader.setHeaderId(headerId);
        tCompasICDHeader.setHeaderVersion(headerVersion);
        tCompasICDHeader.setHeaderRevision(headerRevision);
        return tCompasICDHeader;
    }

    public String getIcdSystemVersionUUID() {
        return Optional.of(icdSystemVersionUUID)
                .orElseThrow(() -> new ScdException(ICD_SYSTEM_VERSION_UUID + " is not present in COMPAS-ICDHeader in LNode"));
    }

    @Override
    public String toString() {
        return HEADER_ID + " = " + headerId + ", " +
                HEADER_VERSION + " = " + headerVersion + ", " +
                HEADER_REVISION + " = " + headerRevision +
                " and " + ICD_SYSTEM_VERSION_UUID + " = " + icdSystemVersionUUID;
    }
}
