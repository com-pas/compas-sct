// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model;

import org.lfenergy.compas.sct.model.dto.ResumedDataTemplate;

import java.util.Set;

public interface ILNodeDTO {
    String getInst();
    String getLNodeClass();
    String getLNodeType();
    <T extends IExtRefDTO> Set<T> getExtRefs();
    Set<ResumedDataTemplate> getResumedDataTemplates();

    <T extends IExtRefDTO> void addExtRef(T extRef);
    void addResumedDataTemplate(ResumedDataTemplate dtt);

    void setInst(String inst);
    void setLNodeClass(String lnClass);
    void setLNodeType(String lnType);
}
