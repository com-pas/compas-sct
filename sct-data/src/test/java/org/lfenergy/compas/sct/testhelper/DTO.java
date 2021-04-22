// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.testhelper;

import org.lfenergy.compas.scl.TConnectedAP;
import org.lfenergy.compas.scl.TExtRef;
import org.lfenergy.compas.scl.TFCEnum;
import org.lfenergy.compas.scl.TLLN0Enum;
import org.lfenergy.compas.scl.TPredefinedCDCEnum;
import org.lfenergy.compas.scl.TServiceType;
import org.lfenergy.compas.scl.TSubNetwork;
import org.lfenergy.compas.sct.model.dto.ConnectedApDTO;
import org.lfenergy.compas.sct.model.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.model.dto.ExtRefInfo;
import org.lfenergy.compas.sct.model.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.model.dto.ExtRefSourceInfo;
import org.lfenergy.compas.sct.model.dto.IedDTO;
import org.lfenergy.compas.sct.model.dto.LDeviceDTO;
import org.lfenergy.compas.sct.model.dto.LNodeDTO;
import org.lfenergy.compas.sct.model.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.model.dto.ScdDTO;
import org.lfenergy.compas.sct.model.dto.SubNetworkDTO;
import org.lfenergy.compas.sct.model.entity.SimpleScd;

import java.util.UUID;

public class DTO {

    /*-----------------------------------------------*/
    /*                   Scd/ScdDTO                  */
    /*-----------------------------------------------*/
    public static final String HEADER_REVISION = "1.0";
    public static final String HEADER_VERSION = "1.0";
    public static final String FILE_NAME = "filename";
    public static final String DUMMY_PAYLOAD = "blablabla";

    public static SimpleScd createScd(UUID id){
        SimpleScd scd = new SimpleScd();
        scd.setId(id);
        scd.setRawXml(DUMMY_PAYLOAD.getBytes());
        scd.setFileName(FILE_NAME);
        scd.setHeaderId(id);
        scd.setHeaderRevision(HEADER_REVISION);
        scd.setHeaderVersion(HEADER_VERSION);

        return scd;
    }
    public static ScdDTO createScdDTO(UUID id){
        ScdDTO scdDTO = new ScdDTO();
        scdDTO.setId(id);
        scdDTO.setHeaderId(id);
        scdDTO.setHeaderRevision(HEADER_REVISION);
        scdDTO.setHeaderVersion(HEADER_VERSION);
        scdDTO.setFileName(FILE_NAME);
        scdDTO.setWhat("WHAT");
        scdDTO.setWhy("WHY");
        scdDTO.setWho("WHO");

        return scdDTO;
    }

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
        bindingInfo.setIedName(IED_NAME);
        bindingInfo.setLdInst(LD_INST);
        bindingInfo.setLnInst(LN_INST);
        bindingInfo.setLnClass(LN_CLASS);
        bindingInfo.setDaName(DA_NAME);
        bindingInfo.setDoName(DO_NAME);
        bindingInfo.setPrefix(PREFIX);
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
        extRefInfo.setSourceInfo(DTO.createExtRefSourceInfo());
        extRefInfo.setBindingInfo(DTO.createExtRefBindingInfo());
        extRefInfo.setSignalInfo(DTO.createExtRefSignalInfo());

        return extRefInfo;
    }
    /*-----------------------------------------------*/
    /*                   ResumedDataTemplate         */
    /*-----------------------------------------------*/
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

    /*-----------------------------------------------*/
    /*                   LNodeDTO                    */
    /*-----------------------------------------------*/
    public static final String LN_TYPE = "LN_TYPE";
    public static LNodeDTO createLNodeDTO(){
        return new  LNodeDTO(LN_INST,LN_CLASS,LN_TYPE);
    }
    /*-----------------------------------------------*/
    /*                   ConnectedAPDTO              */
    /*-----------------------------------------------*/
    public static final String AP_NAME = "AP_NAME";
    public static ConnectedApDTO createCapDTO(boolean byStep){
        if(byStep){
            ConnectedApDTO cap = new ConnectedApDTO();
            cap.setApName(AP_NAME);
            cap.setIedName(IED_NAME);
        }
        return new ConnectedApDTO(IED_NAME,AP_NAME);
    }
    public static TConnectedAP createCap(){

        TConnectedAP cap = new TConnectedAP();
        cap.setApName(AP_NAME);
        cap.setIedName(IED_NAME);
        return cap;
    }
    /*-----------------------------------------------*/
    /*                   SubNetworkDTO               */
    /*-----------------------------------------------*/
    public static final String SN_NAME = "SN_NAME";
    public static SubNetworkDTO createSnDTO(boolean byStep){
        SubNetworkDTO subNetworkDTO = null;
        if(byStep){
            subNetworkDTO = new SubNetworkDTO();
            subNetworkDTO.setName(SN_NAME);
            subNetworkDTO.setType("8-MMS");
        } else {
            subNetworkDTO = new SubNetworkDTO(SN_NAME, SubNetworkDTO.SubnetworkType.MMS.toString());
            subNetworkDTO.addConnectedAPs(createCapDTO(true));
        }
        return subNetworkDTO;
    }

    public static TSubNetwork createSn(){
        TSubNetwork subNetwork = new TSubNetwork();

        subNetwork.setName(SN_NAME);
        subNetwork.setType("8-MMS");
        subNetwork.getConnectedAP().add(createCap());

        return subNetwork;
    }
    /*-----------------------------------------------*/
    /*                   LDeviceDTO                  */
    /*-----------------------------------------------*/
    public static final String LD_NAME = "LDPO";
    public static LDeviceDTO createLdDTO(){

        LDeviceDTO lDeviceDTO = new LDeviceDTO();
        lDeviceDTO.setLdInst(LD_INST);
        lDeviceDTO.setLdName(LD_NAME);

        lDeviceDTO.addLNode(createLNodeDTO());

        return lDeviceDTO;
    }
    /*-----------------------------------------------*/
    /*                   IedDTO                      */
    /*-----------------------------------------------*/

    public static IedDTO createIedDTO(){

        IedDTO iedDTO = new IedDTO();
        iedDTO.setName(IED_NAME);
        iedDTO.addLDevice(createLdDTO());
        return iedDTO;
    }
}
