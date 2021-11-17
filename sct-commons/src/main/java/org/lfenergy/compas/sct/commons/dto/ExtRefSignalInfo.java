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



    public boolean isWrappedIn(TExtRef tExtRef) {
        ExtRefSignalInfo that = new ExtRefSignalInfo(tExtRef);
        return Objects.equals(desc, that.desc) &&
                Objects.equals(pLN, that.pLN) &&
                Objects.equals(pDO, that.pDO) &&
                Objects.equals(pDA, that.pDA) &&
                Objects.equals(intAddr, that.intAddr) &&
                pServT == that.pServT;
    }

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
}
