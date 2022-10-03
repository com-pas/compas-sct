// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;
import org.lfenergy.compas.sct.commons.scl.com.SubNetworkAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Set<ConnectedApDTO> connectedAPs = new HashSet<>();

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
     * Create default SubnetworkType in Communication node of SCL file
     * @param iedName name of existing IED in SCL
     * @param comAdapter Communication node Adapter object value
     * @param comMap possible name of SubnetworkTypes and corresponding ConnectedAPs
     * @return
     */
    public static Set<SubNetworkDTO> createDefaultSubnetwork(String iedName, CommunicationAdapter comAdapter, Map<Pair<String, String>, List<String>> comMap){
        Set<SubNetworkDTO> subNetworkDTOS = new HashSet<>();
        comMap.forEach((subnetworkNameType, apNames) -> {
            SubNetworkDTO subNetworkDTO = new SubNetworkDTO(subnetworkNameType.getLeft(), subnetworkNameType.getRight());
            apNames.forEach(s -> {
                if(getStdConnectedApNames(comAdapter).contains(s)){
                    ConnectedApDTO connectedApDTO = new ConnectedApDTO(iedName, s);
                    subNetworkDTO.addConnectedAP(connectedApDTO);}
            });
            subNetworkDTOS.add(subNetworkDTO);
        });
        return subNetworkDTOS;
    }

    /**
     * Gets ConnectedAP name's from Communication node
     * @param comAdapter Communication node object value
     * @return
     */
    private static List<String> getStdConnectedApNames(CommunicationAdapter comAdapter){
        return comAdapter.getSubNetworkAdapters().stream()
                .map(SubNetworkAdapter::getConnectedAPAdapters)
                .flatMap(connectedAPAdapters -> connectedAPAdapters.stream().map(ConnectedAPAdapter::getApName))
                .collect(Collectors.toList());
    }
}
