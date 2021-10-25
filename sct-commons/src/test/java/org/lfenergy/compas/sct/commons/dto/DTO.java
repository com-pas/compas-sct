// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.lfenergy.compas.scl2007b4.model.*;

import java.util.List;
import java.util.UUID;

public class DTO {

    /*-----------------------------------------------*/
    /*                   ExtRefInfo                  */
    /*-----------------------------------------------*/
    public static final String DESC = "DESC";
    public static final String P_DA = "d";
    public static final String P_DO = "FACntRs1.res";
    public static final String P_LN = TLLN0Enum.LLN_0.value();
    public static final String P_SERV_T = "Report";
    public static final String INT_ADDR = "INT_ADDR";

    public static final String IED_NAME = "IED_NAME";
    public static final String LD_INST= "PIOC";
    public static final String LN_INST= "LN";
    public static final String LN_CLASS= "LN_CLASS";
    public static final String DA_NAME = P_DA;
    public static final String DO_NAME = P_DO;
    public static final String PREFIX = "PR";
    public static final String SERVICE_TYPE = P_SERV_T;

    public static final String SRC_LD_INST= LD_INST;
    public static final String SRC_LN_INST= LN_INST;
    public static final String SRC_LN_CLASS= LN_CLASS;
    public static final String SRC_PREFIX = PREFIX;
    public static final String SRC_CB_NAME = "SRC_CB_NAME";

    public static TExtRef createExtRef(){
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc(DESC);
        tExtRef.setPDA(P_DA);
        tExtRef.setPDO(P_DO);
        tExtRef.getPLN().add(P_LN);
        tExtRef.setPServT(TServiceType.fromValue(P_SERV_T));
        tExtRef.setIntAddr(INT_ADDR);

        tExtRef.setIedName(IED_NAME);
        tExtRef.setLdInst(LD_INST);
        tExtRef.setLnInst(LN_INST);
        tExtRef.getLnClass().add(LN_CLASS);
        tExtRef.setDaName(DA_NAME);
        tExtRef.setDoName(DO_NAME);
        tExtRef.setPrefix(PREFIX);
        tExtRef.setServiceType(TServiceType.fromValue(SERVICE_TYPE));

        tExtRef.setSrcLDInst(SRC_LD_INST);
        tExtRef.setSrcLNInst(SRC_LN_INST);
        tExtRef.getSrcLNClass().add(SRC_LN_CLASS);
        tExtRef.setSrcPrefix(SRC_PREFIX);
        tExtRef.setSrcCBName(SRC_CB_NAME);

        return tExtRef;
    }


    /*-----------------------------------------------*/
    /*                   ResumedDataTemplate         */
    /*-----------------------------------------------*/
    public static final String LN_TYPE = "LN_TYPE";

    public static final String CDC = TPredefinedCDCEnum.WYE.value();
    public static final String FC = TFCEnum.CF.value();
    public static ResumedDataTemplate createRTT(){
        ResumedDataTemplate rDTT = new ResumedDataTemplate();
        rDTT.setCdc(TPredefinedCDCEnum.fromValue(CDC));
        rDTT.setFc(TFCEnum.fromValue(FC));
        rDTT.setLnType(LN_TYPE);
        rDTT.setLnClass(TLLN0Enum.LLN_0.value());
        rDTT.setDoName(DO_NAME);
        rDTT.setDaName(DA_NAME);

        return rDTT;
    }

    public static ResumedDataTemplate createRTT(String prefix, String lnClass, String lnInst){
        ResumedDataTemplate rDTT = new ResumedDataTemplate();
        rDTT.setCdc(TPredefinedCDCEnum.fromValue(CDC));
        rDTT.setFc(TFCEnum.fromValue(FC));
        rDTT.setLnType(LN_TYPE);
        rDTT.setLnClass(lnClass);
        rDTT.setLnInst(lnInst);
        rDTT.setPrefix(prefix);
        rDTT.setType("type");
        rDTT.setDoName("do");
        rDTT.getSdoNames().addAll(List.of("sdo1","sdo2"));
        rDTT.setDaName("da");
        rDTT.getBdaNames().addAll(List.of("bda1","bda2"));
        rDTT.setValImport(true);
        rDTT.getDaiValues().put(1L,"toto");

        return rDTT;
    }

    /*-----------------------------------------------*/
    /*                   Control block               */
    /*-----------------------------------------------*/

    public static final String CB_ID = UUID.randomUUID().toString();
    public static final String CB_DATASET_REF = "DATASET_REF";
    public static final String CB_NAME = "NAME";
    public static final String CB_DESC = "DESCRIPTION";
    public static final String RPT_DESC = "RPT DESCRIPTION";
    public static final String RPT_TEXT = "RPT TEXT";


    public static TRptEnabled createTRptEnabled() {
        TRptEnabled rptEnabled = new TRptEnabled();

        rptEnabled.setDesc(RPT_DESC);
        rptEnabled.setMax(2L);
        TText tText = new TText();
        tText.setSource(RPT_TEXT);
        rptEnabled.setText(tText);

        return rptEnabled;
    }

    public static TReportControl.OptFields createOptFields(){
        TReportControl.OptFields optFields = new TReportControl.OptFields();
        optFields.setBufOvfl(true);
        optFields.setBufOvfl(true);
        optFields.setDataRef(true);
        return optFields;
    }
}
