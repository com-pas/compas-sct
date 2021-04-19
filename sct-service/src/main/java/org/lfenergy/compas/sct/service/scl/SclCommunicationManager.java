package org.lfenergy.compas.sct.service.scl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.exception.ScdException;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.TCommunication;
import org.lfenergy.compas.scl.TConnectedAP;
import org.lfenergy.compas.scl.TIED;
import org.lfenergy.compas.scl.TSubNetwork;

@Setter
@Getter
@NoArgsConstructor
public class SclCommunicationManager {
    protected SCL receiver;

    public SclCommunicationManager(SCL receiver) {
        this.receiver = receiver;
    }

    public SCL addSubnetwork(String snName, String snType, String iedName, String apName) throws ScdException {

        TIED ied = receiver.getIED().stream()
                .filter(tied -> tied.getName().equals(iedName))
                .findAny().orElseThrow(() -> new ScdException("Unknown IED:" + iedName));

        boolean isFound = ied.getAccessPoint()
                .stream()
                .anyMatch(tAccessPoint -> tAccessPoint.getName().equals(apName));
        if(!isFound){
            throw new ScdException("Unknown AccessPoint :" + apName + " in IED :" + iedName);
        }

        TCommunication communication = receiver.getCommunication();
        if(communication == null){
            communication = new TCommunication();
            receiver.setCommunication(communication);
        }
        addSubnetwork(communication,snName,snType,iedName,apName);

        return receiver;
    }

    public TCommunication addSubnetwork(TCommunication communication, String snName, String snType,String iedName, String apName){

         TSubNetwork subNetwork = communication.getSubNetwork()
                    .stream()
                    .filter(tSubNetwork -> tSubNetwork.getName().equals(snName))
                    .findFirst().orElse(null);
        if(subNetwork == null){ // create new subnetwork
            subNetwork = new TSubNetwork();
            subNetwork.setName(snName);
            subNetwork.setType(snType);
            communication.getSubNetwork().add(subNetwork);
        }
        addConnectedAP(subNetwork,iedName,apName);
        return communication;
    }

    public TSubNetwork addConnectedAP(TSubNetwork subNetwork, String iedName, String apName){
        boolean isFound = subNetwork.getConnectedAP().stream()
                .anyMatch(tConnectedAP ->
                        tConnectedAP.getApName().equals(apName) && tConnectedAP.getIedName().equals(iedName));

        if(!isFound){
            TConnectedAP tConnectedAP = new TConnectedAP();
            tConnectedAP.setApName(apName);
            tConnectedAP.setIedName(iedName);
            subNetwork.getConnectedAP().add(tConnectedAP);
        }
        return subNetwork;
    }
}
