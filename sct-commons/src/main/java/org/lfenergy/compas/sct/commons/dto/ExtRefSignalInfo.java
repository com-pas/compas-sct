// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TServiceType;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A representation of the model object <em><b>ExtRef Signal Information</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ExtRefSignalInfo#getDesc <em>Desc</em>}</li>
 *   <li>{@link ExtRefSignalInfo#getPLN <em>PLN</em>}</li>
 *   <li>{@link ExtRefSignalInfo#getPDO <em>PDO</em>}</li>
 *   <li>{@link ExtRefSignalInfo#getPDA <em>PDA</em>}</li>
 *   <li>{@link ExtRefSignalInfo#getPServT <em>PServ T</em>}</li>
 *   <li>{@link ExtRefSignalInfo#getIntAddr <em>Int Addr</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TExtRef
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class ExtRefSignalInfo{
    private String desc = ""; // empty SHOULD/MUST be the default value!
    private String pLN;
    private String pDO;
    private String pDA;
    private String intAddr;
    private TServiceType pServT;

    /**
     * Constructor
     * @param tExtRef input
     */
    public ExtRefSignalInfo(TExtRef tExtRef){
        desc = tExtRef.getDesc();
        if(!tExtRef.getPLN().isEmpty()) {
            pLN = tExtRef.getPLN().get(0);
        }
        pDO = tExtRef.getPDO();
        pDA = tExtRef.getPDA();
        intAddr = tExtRef.getIntAddr();
        pServT = tExtRef.getPServT();
    }


    /**
     * Initialize ExtRef
     * @param signalInfo object containing ExtRef data's'
     * @return ExtRef object
     */
    public static TExtRef initExtRef(ExtRefSignalInfo signalInfo) {

        TExtRef extRef = new TExtRef();
        extRef.setPDO(signalInfo.pDO);
        extRef.setPDA(signalInfo.pDA);
        extRef.getPLN().add(signalInfo.pLN);
        extRef.setPServT(signalInfo.pServT);
        extRef.setDesc(signalInfo.desc);
        extRef.setIntAddr(signalInfo.intAddr);

        return extRef;
    }

    /**
     * Check that if TExtRef object is wrappen in ExtRefSignalInfo object vice versa
     * @param tExtRef object containing ExtRef data's'
     * @return wrapper state
     */
    public boolean isWrappedIn(TExtRef tExtRef) {
        ExtRefSignalInfo that = new ExtRefSignalInfo(tExtRef);
        return Objects.equals(desc, that.desc) &&
                Objects.equals(pLN, that.pLN) &&
                Objects.equals(pDO, that.pDO) &&
                Objects.equals(pDA, that.pDA) &&
                Objects.equals(intAddr, that.intAddr) &&
                pServT == that.pServT;
    }

    /**
     * Check validity of pDO and pDA values
     * @return validity state
     */
    public boolean isValid() {
        if(StringUtils.isBlank(pDO) || StringUtils.isBlank(intAddr)){
            return false;
        }
        String validationRegex = DoTypeName.VALIDATION_REGEX;

        Pattern pattern = Pattern.compile(validationRegex,Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(pDO);
        matcher.find();
        int lastIdx;
        try {
            lastIdx = matcher.end();
        } catch (IllegalStateException ex){
            log.error("Regex matcher error",ex);
            return false;
        }

        if(pDO.length() != lastIdx){
            return false;
        }
        if(!StringUtils.isBlank(pDA)) {
            validationRegex = DaTypeName.VALIDATION_REGEX;
            pattern = Pattern.compile(validationRegex, Pattern.MULTILINE);
            matcher = pattern.matcher(pDA);
            matcher.find();
            return pDA.length() == matcher.end();
        }

        return true;
    }

    /**
     * Convert ExtRefSignalInfo to string value
     * @return ExtRefSignalInfo formatted to string
     */
    @Override
    public String toString() {
        return "ExtRefSignalInfo{" +
                "desc='" + desc + '\'' +
                ", pLN='" + pLN + '\'' +
                ", pDO='" + pDO + '\'' +
                ", pDA='" + pDA + '\'' +
                ", intAddr='" + intAddr + '\'' +
                ", pServT=" + pServT.value() +
                '}';
    }
}
