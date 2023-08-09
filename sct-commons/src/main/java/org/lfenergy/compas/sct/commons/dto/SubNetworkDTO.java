// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lfenergy.compas.scl2007b4.model.TCommunication;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.scl.com.SubNetworkAdapter;

import java.util.*;

/**
 * A representation of the model object <em><b>Sub Network</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link SubNetworkDTO#getType <em>Type</em>}</li>
 *   <li>{@link SubNetworkDTO#getName <em>Name</em>}</li>
 *   <li>{@link SubNetworkDTO#getConnectedAPs <em>Refers To Connected AP</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TSubNetwork
 */
@Getter
@NoArgsConstructor
public class SubNetworkDTO {

    private String name;
    private SubnetworkType type;
    private final Set<ConnectedApDTO> connectedAPs = new HashSet<>();

    /**
     * Constructor
     * @param name input
     * @param type input
     */
    public SubNetworkDTO(String name, String type) {
        this.name = name;
        this.type = SubnetworkType.fromValue(type);
    }

    /**
     * Initializes SubNetworkDTO
     * @param subNetworkAdapter input
     * @return SubNetworkDTO object value
     */
    public static SubNetworkDTO  from(SubNetworkAdapter subNetworkAdapter) {
        SubNetworkDTO subNetworkDTO = new SubNetworkDTO();
        subNetworkDTO.name = subNetworkAdapter.getName();
        subNetworkDTO.type = SubnetworkType.fromValue(subNetworkAdapter.getType());
        subNetworkAdapter.getConnectedAPAdapters()
                .forEach(
                    connectedAPAdapter -> subNetworkDTO.connectedAPs.add(ConnectedApDTO.from(connectedAPAdapter))
                );

        return subNetworkDTO;
    }

    /**
     * Gets list of ConnectedApDTO of SubNetwork
     * @return Set of ConnectedApDTO object
     */
    public Set<ConnectedApDTO> getConnectedAPs() {
        return Set.of(connectedAPs.toArray(new ConnectedApDTO[0]));
    }

    /**
     * Gets SubNetwork type
     * @return string SubNetwork type
     */
    public String getType(){
        return this.type.value;
    }

    /**
     * Add ConnectedApDTO to SubNetwork list of ConnectedAPs'
     * @param cap input
     */
    public void addConnectedAP(ConnectedApDTO cap) {
        connectedAPs.add(cap);
    }

    /**
     * Sets SubNetwork name
     * @param sName input
     */
    public void setName(String sName){
        name = sName;
    }

    /**
     * Sets SubNetwork Type
     * @param type input
     */
    public void setType(String type) {
        this.type = SubnetworkType.fromValue(type);
    }


    /**
     * Subnetwork Type enum
     */
    public enum SubnetworkType {
        IP("IP"), // 0
        MMS("8-MMS"), // 1
        PHYSICAL("PHYSICAL"); // 2

        private final String value;

        SubnetworkType(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static SubnetworkType fromValue(String text) {
            if(text.equalsIgnoreCase("8-MMS") ) {
                return MMS;
            }

            for (SubnetworkType b : SubnetworkType.values()) {
                if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    /**
     * Create default Subnetwork in Communication node of SCL file
     * @param iedName           Name of existing IED in SCL
     * @param communication     Communication node in SCL
     * @param subNetworkTypes   Possible name of Subnetwork and corresponding ConnectedAPs
     * @return List of Subnetwork
     */
    public static List<SubNetworkDTO> createDefaultSubnetwork(String iedName, TCommunication communication, List<SubNetworkTypeDTO> subNetworkTypes){
        List<SubNetworkDTO> subNetworkDTOS = new ArrayList<>();
        subNetworkTypes.forEach(subnetwork -> {
            SubNetworkDTO subNetworkDTO = new SubNetworkDTO(subnetwork.subnetworkName(), subnetwork.subnetworkType());
            subnetwork.accessPointNames().forEach(accessPointName -> {
                if(getStdConnectedApNames(communication).contains(accessPointName)){
                    ConnectedApDTO connectedApDTO = new ConnectedApDTO(iedName, accessPointName);
                    subNetworkDTO.addConnectedAP(connectedApDTO);
                }
            });
            subNetworkDTOS.add(subNetworkDTO);
        });
        return subNetworkDTOS;
    }

    /**
     * Gets ConnectedAP name's from Communication node
     * @param communication Communication node object value
     * @return List of ConnectedAP names
     */
    private static List<String> getStdConnectedApNames(TCommunication communication){
        return communication.getSubNetwork().stream()
                .map(TSubNetwork::getConnectedAP)
                .flatMap(List::stream)
                .map(TConnectedAP::getApName)
                .toList();
    }

}
