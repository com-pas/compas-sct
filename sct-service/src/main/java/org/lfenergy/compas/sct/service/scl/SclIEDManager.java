package org.lfenergy.compas.sct.service.scl;


import org.lfenergy.compas.exception.ScdException;
import org.lfenergy.compas.scl.LN0;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.TAccessPoint;
import org.lfenergy.compas.scl.TAnyLN;
import org.lfenergy.compas.scl.TDataSet;
import org.lfenergy.compas.scl.TExtRef;
import org.lfenergy.compas.scl.TGSEControl;
import org.lfenergy.compas.scl.TIED;
import org.lfenergy.compas.scl.TLDevice;
import org.lfenergy.compas.scl.TLLN0Enum;
import org.lfenergy.compas.scl.TLN;
import org.lfenergy.compas.scl.TLN0;
import org.lfenergy.compas.scl.TReportControl;
import org.lfenergy.compas.scl.TSampledValueControl;
import org.lfenergy.compas.scl.TServer;
import org.lfenergy.compas.sct.model.IExtRefDTO;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SclIEDManager {

    private SCL receiver;

    public SclIEDManager(SCL receiver) {
        this.receiver = receiver;
    }

    public SCL addIed(SCL iedProvider, String iedName) throws ScdException {
        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        receiver = sclDataTemplateManager.importDTT(iedProvider,receiver,iedName);
        Map<String,String> lNodeTypeTracker = sclDataTemplateManager.getLNodeTypeTracker();
        receiver = addIed(iedProvider,iedName,lNodeTypeTracker);

        return receiver;
    }

    public SCL addIed(SCL iedProvider, String iedName, Map<String,String> lNodeTypeTracker) throws ScdException {
        if(iedProvider.getIED().isEmpty()) return receiver;
        TIED tied = iedProvider.getIED().get(0);
        return addIed(tied,iedName,lNodeTypeTracker);
    }

    public SCL addIed(TIED providerIED,String iedName,Map<String,String> lNodeTypeTracker)  throws ScdException{

        if(receiver.getIED().stream().anyMatch(tied -> tied.getName().equals(iedName))){
            throw new ScdException("SCL file already contains IED: " + iedName);
        }
        if(receiver.getDataTypeTemplates() == null){
            throw new ScdException("There is no DataTypeTemplates in the SCD file");
        }

        Set<String> receiverLNodeTypeIds = receiver.getDataTypeTemplates().getLNodeType()
                .stream()
                .map(tlNodeType -> tlNodeType.getId())
                .collect(Collectors.toSet());

        if(receiverLNodeTypeIds.isEmpty()){
            throw new ScdException("There is no LNodeType in the SCD file's DataTypeTemplates");
        }

        providerIED.setName(iedName);
        // LDevices
        String newLdName;
        List<TAccessPoint> accessPoints = providerIED.getAccessPoint();
        for(TAccessPoint accessPoint : accessPoints){
            TServer server = accessPoint.getServer();
            if(server == null) continue;
            List<TLDevice> lDevices = server.getLDevice();
            for(TLDevice lDevice : lDevices){
                newLdName = iedName + lDevice.getInst();
                if(newLdName.length() > 33){
                    throw new ScdException(newLdName + "(IED.name + LDevice.inst) has more than 33 characters");
                }
                // renaming ldName
                lDevice.setLdName(newLdName);
                //
                TLN0 tln0 = lDevice.getLN0();
                updateLN(tln0,receiverLNodeTypeIds,lNodeTypeTracker);

                List<TLN> lns = lDevice.getLN();
                for(TLN tln : lns){
                    updateLN(tln,receiverLNodeTypeIds,lNodeTypeTracker);
                }
            }
        }

        receiver.getIED().add(providerIED);
        return receiver;
    }

    public TAnyLN updateLN(@NonNull TAnyLN ln, @NonNull Set<String> receiverLNodeTypeIds,
                           @NonNull Map<String,String> lNodeTypeTracker) throws ScdException {
        if( (!receiverLNodeTypeIds.contains(ln.getLnType()) && !lNodeTypeTracker.containsKey(ln.getLnType())) ||
                (lNodeTypeTracker.containsKey(ln.getLnType())) &&
                        !receiverLNodeTypeIds.contains(lNodeTypeTracker.get(ln.getLnType()))){
            String lnInst = ln.getClass() == TLN.class ? ((TLN)ln).getInst() : TLLN0Enum.LLN_0.value();
            String errMsg = String.format("%s id (%s) missing in DataTypeTemplates",lnInst,ln.getLnType());
            throw new ScdException(errMsg);
        }

        if(lNodeTypeTracker.containsKey(ln.getLnType())){
            ln.setLnType(lNodeTypeTracker.get(ln.getLnType()));
        }

        return ln;
    }


    public TExtRef getExtRef(String iedName, String ldInst, IExtRefDTO filter) throws ScdException {
        TIED ied = receiver.getIED()
                .stream()
                .filter(tied -> iedName.equals(tied.getName()))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown IED :" + iedName));

        TLDevice ld = SclIEDManager.getIEDLDevice(ied,ldInst)
                .orElseThrow(() -> new ScdException("Unknown LDevice " + iedName + "/" + ldInst));

        List<TExtRef> extRefs = extractLN0ExtRefs(ld,filter);

        if(extRefs.size() > 1){
            String msg = String.format("Multiple ExtRef[pDo(%s), intAddr(%s), desc(%s)] for [%s,%s]",
                    filter.getPDO(),filter.getIntAddr(), filter.getDesc(), iedName,ldInst);
            throw new ScdException(msg);
        }

        if(extRefs.isEmpty()){
            String msg = String.format("No ExtRef[pDo(%s), intAddr(%s), desc(%s)] for [%s,%s]",
                    filter.getPDO(),filter.getIntAddr(), filter.getDesc(), iedName,ldInst);
            throw new ScdException(msg);
        }
        return extRefs.get(0);
    }

    public static List<TLDevice> getIEDLDevice(TIED tied){
        return tied.getAccessPoint()
                .stream()
                .filter(tAccessPoint -> tAccessPoint.getServer() != null)
                .map(tAccessPoint -> tAccessPoint.getServer().getLDevice())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static Optional<TLN> getLDeviceLN(TLDevice lDevice, String lnClass, @Nullable String lnInst){
        return lDevice.getLN()
                .stream()
                .filter(tln -> tln.getLnClass().contains(lnClass) && (lnInst == null || lnInst.equals(tln.getInst())))
                .findFirst();
    }

    public static Optional<TLDevice> getIEDLDevice(TIED tied, String ldInst){
        return tied.getAccessPoint()
                .stream()
                .filter(tAccessPoint -> tAccessPoint.getServer() != null)
                .map(tAccessPoint -> tAccessPoint.getServer().getLDevice())
                .flatMap(Collection::stream)
                .filter(tlDevice -> ldInst.equals(tlDevice.getInst()))
                .findFirst();
    }

    public static List<TExtRef> extractLN0ExtRefs(TLDevice tlDevice, IExtRefDTO filter) {
        if(tlDevice.getLN0().getInputs() == null) return new ArrayList<>();

        if(filter == null) {
            return tlDevice.getLN0().getInputs().getExtRef();
        } else {
            return tlDevice.getLN0().getInputs().getExtRef()
                    .stream()
                    .filter(tExtRef -> {

                        if (filter.getDesc() != null && !filter.getDesc().equals(tExtRef.getDesc())) {
                            return false;
                        }
                        if (filter.getPDO() != null && !tExtRef.getPDO().equals(filter.getPDO())) {
                            return false;
                        }
                        if (filter.getIntAddr() != null && !filter.getIntAddr().equals(tExtRef.getIntAddr())) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }

    }

    public static List<TExtRef> extractLNExtRefs(TLDevice tlDevice, String lnClass, String lnInst, IExtRefDTO filter) throws ScdException {
        TLN ln = tlDevice.getLN()
                .stream()
                .filter(tln -> tln.getLnClass().contains(lnClass))
                .collect(Collectors.toList())
                .stream()
                .filter(tln -> lnInst.equals(tln.getInst()))
                .findFirst()
                .orElseThrow(() -> new ScdException(String.format("Unknown LN [lnClass(%s),inst(%s)] ",lnClass,lnInst)));


        if(ln.getInputs() == null) return new ArrayList<>();

        if(filter == null){
            return ln.getInputs().getExtRef();
        } else {
            return ln.getInputs().getExtRef()
                .stream()
                .filter(tExtRef -> filter.isIdentical(tExtRef))
                .collect(Collectors.toList());
        }
    }
}
