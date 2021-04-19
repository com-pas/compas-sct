package org.lfenergy.compas.sct.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl.TSubNetwork;
import org.lfenergy.compas.sct.model.IConnectedApDTO;
import org.lfenergy.compas.sct.model.ISubNetworkDTO;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class SubNetworkDTO implements ISubNetworkDTO {

    private String name;
    private SubnetworkType type;
    private Set<ConnectedApDTO> connectedAPs = new HashSet<>();

    public SubNetworkDTO(String name, String type) {
        this.name = name;
        this.type = SubnetworkType.fromValue(type);
    }

    public SubNetworkDTO(@NonNull TSubNetwork subNetwork) {
        this.name = subNetwork.getName();
        this.type = SubnetworkType.fromValue(subNetwork.getType());
        subNetwork.getConnectedAP().forEach(tConnectedAP -> {
            connectedAPs.add(new ConnectedApDTO(tConnectedAP));
        });
    }

    @Override
    public Set<ConnectedApDTO> getConnectedAPs() {
        return Set.of(connectedAPs.toArray(new ConnectedApDTO[0]));
    }

    public String getType(){
        return this.type.value;
    }

    @Override
    public <T extends IConnectedApDTO> void addConnectedAPs(T cap) {
        connectedAPs.add((ConnectedApDTO) cap);
    }

    @Override
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
