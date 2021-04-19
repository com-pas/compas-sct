package org.lfenergy.compas.sct.model.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.sct.model.ILDeviceDTO;
import org.lfenergy.compas.sct.model.ILNodeDTO;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class LDeviceDTO implements ILDeviceDTO {
    private String ldInst;
    private String ldName;
    private Set<LNodeDTO> lNodes = new HashSet<>();

    @Override
    public Set<LNodeDTO> getLNodes() {
        return Set.of(lNodes.toArray(new LNodeDTO[0]));
    }

    @Override
    public <T extends ILNodeDTO> void addLNode(T ln) {
        lNodes.add((LNodeDTO) ln);
    }

    public void addAll(Set<LNodeDTO> lns) {
        lNodes.addAll(lns);
    }
}
