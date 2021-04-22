// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.service;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.commons.MarshallerWrapper;
import org.lfenergy.compas.exception.ScdException;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.TAccessPoint;
import org.lfenergy.compas.scl.TDataTypeTemplates;
import org.lfenergy.compas.scl.TExtRef;
import org.lfenergy.compas.scl.TIED;
import org.lfenergy.compas.scl.TLDevice;
import org.lfenergy.compas.scl.TLLN0Enum;
import org.lfenergy.compas.scl.TLN;
import org.lfenergy.compas.scl.TSubNetwork;
import org.lfenergy.compas.sct.model.IConnectedApDTO;
import org.lfenergy.compas.sct.model.ISubNetworkDTO;
import org.lfenergy.compas.sct.model.dto.ExtRefInfo;
import org.lfenergy.compas.sct.model.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.model.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.model.dto.IedDTO;
import org.lfenergy.compas.sct.model.dto.LDeviceDTO;
import org.lfenergy.compas.sct.model.dto.LNodeDTO;
import org.lfenergy.compas.sct.model.dto.SubNetworkDTO;
import org.lfenergy.compas.sct.model.entity.SimpleScd;
import org.lfenergy.compas.sct.repository.SimpleScdRepository;
import org.lfenergy.compas.sct.service.scl.SclCommunicationManager;
import org.lfenergy.compas.sct.service.scl.SclDataTemplateManager;
import org.lfenergy.compas.sct.service.scl.SclHeaderManager;
import org.lfenergy.compas.sct.service.scl.SclIEDManager;
import org.lfenergy.compas.sct.service.scl.SclManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Slf4j
@Service
public class SimpleSqlScdService extends AbstractSqlScdService<SimpleScd, SimpleScdRepository>{

    private final MarshallerWrapper marshallerWrapper;
    @Autowired
    public SimpleSqlScdService(SimpleScdRepository repository, MarshallerWrapper marshallerWrapper) {
        super(repository);
        this.marshallerWrapper = marshallerWrapper;
    }

    @Override
    public SimpleScd initiateSCD(@NonNull String filename, @NonNull String hVersion, @NonNull String hRevision) throws ScdException {
        SimpleScd scdObj = new SimpleScd();
        scdObj.setFileName(filename);
        SCL scd = initiateScl(hVersion,hRevision);
        scdObj.setHeaderId(UUID.fromString(scd.getHeader().getId()));
        scdObj.setHeaderVersion(hVersion);
        scdObj.setHeaderRevision(hRevision);
        String rawXml = marshallerWrapper.marshall(scd);

        scdObj.setRawXml(rawXml.getBytes());
        return scdObj;
    }

    @Override
    public SimpleScd addHistoryItem(SimpleScd scdObj, String who, String what, String why) throws ScdException {
        byte[] raw = scdObj.getRawXml();
        SCL scd = marshallerWrapper.unmarshall(raw);
        SclHeaderManager sclHeaderManager = new SclHeaderManager(scd);
        scd = sclHeaderManager.addHistoryItem(who,what,why);
        byte[] rawXml = marshallerWrapper.marshall(scd).getBytes();
        scdObj.setRawXml(rawXml);
        return scdObj;
    }

    @Override
    public SimpleScd addIED(SimpleScd scdObj, String iedName, SCL icd) throws ScdException {
        SCL scd = marshallerWrapper.unmarshall(scdObj.getRawXml());
        SclManager sclManager = new SclManager(scd);
        sclManager.addIED(icd,iedName);
        scdObj.setRawXml(marshallerWrapper.marshall(sclManager.getReceiver()).getBytes());
        return scdObj;
    }

    @Override
    public SimpleScd addIED(SimpleScd scdObj, String iedName, byte[] rawIcd) throws ScdException {
        SCL icd =  marshallerWrapper.unmarshall(rawIcd);
        return addIED(scdObj,iedName,icd);
    }

    @Override
    public IedDTO extractExtRefs(SimpleScd scdObj, String iedName, String ldInst) throws ScdException {
        SCL receiver = marshallerWrapper.unmarshall(scdObj.getRawXml());

        TIED ied = receiver.getIED()
                .stream()
                .filter(tied -> iedName.equals(tied.getName()))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown IED: " + iedName));

        TLDevice lDevice = SclIEDManager.getIEDLDevice(ied,ldInst)
                .orElseThrow(() -> new ScdException("Unknown LDevice " + iedName + "/" + ldInst));

        IedDTO iedDTO = new IedDTO();
        iedDTO.setName(iedName);

        LDeviceDTO lDeviceDTO = new LDeviceDTO();
        lDeviceDTO.setLdInst(ldInst);

        List<TExtRef> ln0ExtRefs = SclIEDManager.extractLN0ExtRefs(lDevice, null);
        LNodeDTO lN0DTO = new LNodeDTO();
        lN0DTO.setLNodeClass(TLLN0Enum.LLN_0.value());
        ln0ExtRefs.forEach((TExtRef tExtRef) -> {
            ExtRefInfo extRefDTO = new ExtRefInfo(tExtRef);
            lN0DTO.addExtRef(extRefDTO);
        });
        lDeviceDTO.addLNode(lN0DTO);
        iedDTO.addLDevice(lDeviceDTO);
        return iedDTO;
    }
    @Override
    public Set<IedDTO> extractExtRefBindingInfo(SimpleScd scdObj, String iedName, String ldInst, ExtRefSignalInfo extRef) throws ScdException {
        SCL receiver = marshallerWrapper.unmarshall(scdObj.getRawXml());
        SclIEDManager sclIEDManager = new SclIEDManager(receiver);
        sclIEDManager.getExtRef(iedName,ldInst,extRef);

        List<TIED> ieds = receiver.getIED();
        Set<IedDTO> iedDTOs = new HashSet<>();
        for(TIED tied : ieds){
            IedDTO iedDTO = new IedDTO();
            iedDTO.setName(tied.getName());
            List<TAccessPoint> accessPoints = tied.getAccessPoint();
            for(TAccessPoint accessPoint : accessPoints){
                if(accessPoint.getServer() == null) continue;
                List<TLDevice> deviceList = accessPoint.getServer().getLDevice();
                for(TLDevice tlDevice : deviceList) {
                    Set<LNodeDTO> lNodeDTOs = getExtRefBindingInfo(tlDevice,extRef, receiver.getDataTypeTemplates());
                    LDeviceDTO lDeviceDTO = new LDeviceDTO();
                    lDeviceDTO.setLdName(tlDevice.getLdName());
                    lDeviceDTO.setLdInst(tlDevice.getInst());
                    if(!lNodeDTOs.isEmpty()) {
                        lDeviceDTO.addAll(lNodeDTOs);
                        iedDTO.addLDevice(lDeviceDTO);
                    }
                }
            }
            if(!iedDTO.getLDevices().isEmpty()) {
                iedDTOs.add(iedDTO);
            }
        }
        return iedDTOs;
    }


    @Override
    public Set<SubNetworkDTO> getSubnetwork(SimpleScd scdObj) throws ScdException {

        Set<SubNetworkDTO> subNetworks = new HashSet<>();
        SCL scl =  marshallerWrapper.unmarshall(scdObj.getRawXml());
        if(scl.getCommunication() == null || scl.getCommunication().getSubNetwork().isEmpty()){
            return subNetworks;
        }
        List<TSubNetwork> tSubNetworks = scl.getCommunication().getSubNetwork();
        tSubNetworks.forEach((TSubNetwork subNetwork) -> {
            SubNetworkDTO subNetworkDTO = new SubNetworkDTO(subNetwork);

            subNetworks.add(subNetworkDTO);
        });

        return subNetworks;
    }

    @Override
    public SimpleScd addSubnetworks(SimpleScd scdObject, Set<? extends ISubNetworkDTO> subNetworks) throws ScdException {
        SCL receiver = marshallerWrapper.unmarshall(scdObject.getRawXml());
        SclCommunicationManager sclCommunicationManager = new SclCommunicationManager(receiver);
        for(ISubNetworkDTO subNetworkDTO : subNetworks) {
            String snName = subNetworkDTO.getName();
            String snType = subNetworkDTO.getType();
            for (IConnectedApDTO accessPoint : subNetworkDTO.getConnectedAPs()) {
                receiver = sclCommunicationManager.addSubnetwork(snName, snType,
                        accessPoint.getIedName(), accessPoint.getApName());
            }
        }
        scdObject.setRawXml(marshallerWrapper.marshall(receiver).getBytes());
        return scdObject;
    }

    /*------------------------------------------------------*/
    /* Handy methods                                        */
    /*------------------------------------------------------*/

    public Set<LNodeDTO> getExtRefBindingInfo(TLDevice tlDevice, ExtRefSignalInfo extRef, TDataTypeTemplates dtt) throws ScdException {
        List<TLN> lns = tlDevice.getLN();
        Set<LNodeDTO> lNodeDTOs = new HashSet<>();
        for(TLN ln : lns){
            if(extRef.getPLN() != null && !ln.getLnClass().contains(extRef.getPLN())) continue;
            LNodeDTO lNodeDTO = new LNodeDTO();
            lNodeDTO.setLNodeType(ln.getLnType());
            if(ln.getLnClass().isEmpty()){
                throw new ScdException("lnClass is mandatory");
            }
            lNodeDTO.setLNodeClass(ln.getLnClass().get(0));
            lNodeDTO.setInst(ln.getInst());
            ResumedDataTemplate resumedDataTemplate = null;
            try {
                resumedDataTemplate = SclDataTemplateManager.getResumedDTT(ln.getLnType(), extRef, dtt);
            } catch (ScdException e){
                // ln is not a source
            }
            if(resumedDataTemplate != null) {
                lNodeDTO.addResumedDataTemplate(resumedDataTemplate);
                lNodeDTOs.add(lNodeDTO);
            }
        }
        return lNodeDTOs;
    }
}
