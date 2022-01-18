// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.scl.com.SubNetworkAdapter;

import java.util.HashSet;
import java.util.Set;


@Getter
@NoArgsConstructor
public class SubNetworkDTO {

    private String name;
    private SubnetworkType type;
    private Set<ConnectedApDTO> connectedAPs = new HashSet<>();

    public SubNetworkDTO(String name, String type) {
        this.name = name;
        this.type = SubnetworkType.fromValue(type);
    }

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

    public Set<ConnectedApDTO> getConnectedAPs() {
        return Set.of(connectedAPs.toArray(new ConnectedApDTO[0]));
    }

    public String getType(){
        return this.type.value;
    }

    public void addConnectedAP(ConnectedApDTO cap) {
        connectedAPs.add(cap);
    }

    public void setName(String sName){
        name = sName;
    }
    public void setType(String type) {
        this.type = SubnetworkType.fromValue(type);
    }



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
}
