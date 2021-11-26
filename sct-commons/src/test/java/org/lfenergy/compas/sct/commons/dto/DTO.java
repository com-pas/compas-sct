// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.lfenergy.compas.scl2007b4.model.*;

import java.util.UUID;

public class DTO {
    /*-----------------------------------------------*/
    /*                   ConnectedAPDTO              */
    /*-----------------------------------------------*/
    public static final String AP_NAME = "AP_NAME";
    public static ConnectedApDTO createCapDTO(){

        ConnectedApDTO cap = new ConnectedApDTO();
        cap.setApName(AP_NAME);
        cap.setIedName(HOLDER_IED_NAME);

        return cap;
    }

    /*-----------------------------------------------*/
    /*                   ExtRefInfo                  */
    /*-----------------------------------------------*/
    public static final String HOLDER_IED_NAME = "IED_NAME";
    public static final String HOLDER_LD_INST = "PIOC";
    public static final String HOLDER_LN_INST = "1";
    public static final String HOLDER_LN_CLASS = "LN_CLASS";
    public static final String HOLDER_LN_PREFIX = "PR";

    public static final String DESC = "DESC";
    public static final String P_DA = "d";
    public static final String P_DO = "FACntRs1.res";
    public static final String P_LN = TLLN0Enum.LLN_0.value();
    public static final String P_SERV_T = "Report";
    public static final String INT_ADDR = "INT_ADDR";

    public static final String REMOTE_IED_NAME = "IED_NAME_R";
    public static final String REMOTE_LD_INST = "ANCR";
    public static final String REMOTE_LN_INST = "1";
    public static final String REMOTE_LN_CLASS = "LN_CLASS";
    public static final String DA_NAME = P_DA;
    public static final String DO_NAME = P_DO;
    public static final String REMOTE_LN_PREFIX = "PR";
    public static final String SERVICE_TYPE = P_SERV_T;

    public static final String SRC_LD_INST= REMOTE_LD_INST;
    public static final String SRC_LN_INST= REMOTE_LN_INST;
    public static final String SRC_LN_CLASS= REMOTE_LN_CLASS;
    public static final String SRC_PREFIX = REMOTE_LN_PREFIX;
    public static final String SRC_CB_NAME = "SRC_CB_NAME";

    public static TExtRef createExtRef(){
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc(DESC);
        tExtRef.setPDA(P_DA);
        tExtRef.setPDO(P_DO);
        tExtRef.getPLN().add(P_LN);
        tExtRef.setPServT(TServiceType.fromValue(P_SERV_T));
        tExtRef.setIntAddr(INT_ADDR);

        tExtRef.setIedName(REMOTE_IED_NAME);
        tExtRef.setLdInst(REMOTE_LD_INST);
        tExtRef.setLnInst(REMOTE_LN_INST);
        tExtRef.getLnClass().add(REMOTE_LN_CLASS);
        tExtRef.setDaName(DA_NAME);
        tExtRef.setDoName(DO_NAME);
        tExtRef.setPrefix(REMOTE_LN_PREFIX);
        tExtRef.setServiceType(TServiceType.fromValue(SERVICE_TYPE));

        tExtRef.setSrcLDInst(SRC_LD_INST);
        tExtRef.setSrcLNInst(SRC_LN_INST);
        tExtRef.getSrcLNClass().add(SRC_LN_CLASS);
        tExtRef.setSrcPrefix(SRC_PREFIX);
        tExtRef.setSrcCBName(SRC_CB_NAME);

        return tExtRef;
    }


    public static ExtRefSignalInfo createExtRefSignalInfo(){
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setDesc(DESC);
        signalInfo.setPDA(P_DA);
        signalInfo.setPDO(P_DO);
        signalInfo.setPLN(P_LN);
        signalInfo.setPServT(TServiceType.fromValue(P_SERV_T));
        signalInfo.setIntAddr(INT_ADDR);

        return signalInfo;
    }

    public static ExtRefBindingInfo createExtRefBindingInfo(){
        ExtRefBindingInfo bindingInfo = new ExtRefBindingInfo();
        bindingInfo.setIedName(REMOTE_IED_NAME);
        bindingInfo.setLdInst(REMOTE_LD_INST);
        bindingInfo.setLnInst(REMOTE_LN_INST);
        bindingInfo.setLnClass(REMOTE_LN_CLASS);
        bindingInfo.setDaName(new DaTypeName(DA_NAME));
        bindingInfo.setDoName(new DoTypeName(DO_NAME));
        bindingInfo.setPrefix(REMOTE_LN_PREFIX);
        bindingInfo.setServiceType(TServiceType.fromValue(SERVICE_TYPE));

        return bindingInfo;
    }

    public static ExtRefSourceInfo createExtRefSourceInfo(){
        ExtRefSourceInfo sourceInfo = new ExtRefSourceInfo();
        sourceInfo.setSrcLDInst(SRC_LD_INST);
        sourceInfo.setSrcLNInst(SRC_LN_INST);
        sourceInfo.setSrcLNClass(SRC_LN_CLASS);
        sourceInfo.setSrcPrefix(SRC_PREFIX);
        sourceInfo.setSrcCBName(SRC_CB_NAME);

        return sourceInfo;
    }

    public static ExtRefInfo createExtRefInfo(){
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIedName(HOLDER_IED_NAME);
        extRefInfo.setHolderLdInst(HOLDER_LD_INST);
        extRefInfo.setHolderLnInst(HOLDER_LN_INST);
        extRefInfo.setHolderLnClass(HOLDER_LN_CLASS);
        extRefInfo.setHolderPrefix(HOLDER_LN_PREFIX);
        extRefInfo.setSourceInfo(DTO.createExtRefSourceInfo());
        extRefInfo.setBindingInfo(DTO.createExtRefBindingInfo());
        extRefInfo.setSignalInfo(DTO.createExtRefSignalInfo());

        return extRefInfo;
    }


    /*-----------------------------------------------*/
    /*                   ResumedDataTemplate         */
    /*-----------------------------------------------*/
    public static final String LN_TYPE = "LN_TYPE";

    public static final String CDC = TPredefinedCDCEnum.WYE.value();
    public static final String FC = TFCEnum.CF.value();
    public static ResumedDataTemplate createRTT(){
        ResumedDataTemplate rDTT = new ResumedDataTemplate();

        rDTT.setLnType(LN_TYPE);
        rDTT.setLnClass(TLLN0Enum.LLN_0.value());
        rDTT.setDoName(DO_NAME);
        rDTT.setDaName(DA_NAME);
        rDTT.getDoName().setCdc(TPredefinedCDCEnum.fromValue(CDC));
        rDTT.getDaName().setFc(TFCEnum.fromValue(FC));

        return rDTT;
    }

    public static ResumedDataTemplate createRTT(String prefix, String lnClass, String lnInst){
        ResumedDataTemplate rDTT = new ResumedDataTemplate();

        rDTT.setLnType(LN_TYPE);
        rDTT.setLnClass(lnClass);
        rDTT.setLnInst(lnInst);
        rDTT.setPrefix(prefix);

        rDTT.setDoName("do.sdo1.sdo2");
        rDTT.setDaName("da.bda1.bda2");
        rDTT.getDoName().setCdc(TPredefinedCDCEnum.fromValue(CDC));
        rDTT.getDaName().setFc(TFCEnum.fromValue(FC));
        rDTT.getDaName().setBType("bType");
        rDTT.getDaName().setType("type");
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

    /*-----------------------------------------------*/
    /*                   LNodeDTO                    */
    /*-----------------------------------------------*/
    public static LNodeDTO createLNodeDTO(){
        return new LNodeDTO(HOLDER_LN_INST, HOLDER_LN_CLASS,null,LN_TYPE);
    }

    /*-----------------------------------------------*/
    /*                   LDeviceDTO                  */
    /*-----------------------------------------------*/
    public static final String LD_NAME = "LDPO";
    public static LDeviceDTO createLdDTO(){

        LDeviceDTO lDeviceDTO = new LDeviceDTO();
        lDeviceDTO.setLdInst(HOLDER_LD_INST);
        lDeviceDTO.setLdName(LD_NAME);

        lDeviceDTO.addLNode(createLNodeDTO());

        return lDeviceDTO;
    }

    /*-----------------------------------------------*/
    /*                   IedDTO                      */
    /*-----------------------------------------------*/

    public static IedDTO createIedDTO(){

        IedDTO iedDTO = new IedDTO();
        iedDTO.setName(HOLDER_IED_NAME);
        iedDTO.addLDevice(createLdDTO());
        return iedDTO;
    }
}
