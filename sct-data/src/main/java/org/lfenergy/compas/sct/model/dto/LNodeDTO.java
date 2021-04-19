// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl.TLN;
import org.lfenergy.compas.sct.model.IControlBlockDTO;
import org.lfenergy.compas.sct.model.IExtRefDTO;
import org.lfenergy.compas.sct.model.ILNodeDTO;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class LNodeDTO implements ILNodeDTO {
    private String inst;
    private String lNodeClass;
    private String lNodeType;
    private Set<ExtRefDTO> extRefs = new HashSet<>();
    private Set<ResumedDataTemplate> resumedDataTemplates = new HashSet<>();

    public LNodeDTO(String inst, String lNClass, String lNType) {
        this.inst = inst;
        this.lNodeClass = lNClass;
        this.lNodeType = lNType;

    }

    public static LNodeDTO extractData(TLN ln) {
        String lnClass = ln.getLnClass().isEmpty() ? "" : ln.getLnClass().get(0);
        LNodeDTO lNodeDTO = new LNodeDTO(ln.getInst(),lnClass,ln.getLnType());
        if(ln.getInputs() != null){
            ln.getInputs().getExtRef().forEach(tExtRef -> {
                ExtRefDTO extRefDTO = new ExtRefDTO(tExtRef);
                lNodeDTO.addExtRef(extRefDTO);
            });
        }
        return lNodeDTO;
    }

    @Override
    public Set<ExtRefDTO> getExtRefs() {
        return Set.of(extRefs.toArray(new ExtRefDTO[0]));
    }

    @Override
    public <T extends IExtRefDTO> void addExtRef(T extRef) {
        extRefs.add((ExtRefDTO) extRef);
    }

    @Override
    public void addResumedDataTemplate(ResumedDataTemplate dtt) {
        resumedDataTemplates.add(dtt);
    }

    @Override
    public Set<ResumedDataTemplate> getResumedDataTemplates(){
        return Set.of(resumedDataTemplates.toArray(new ResumedDataTemplate[0]));
    }
}
