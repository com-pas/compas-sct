package org.lfenergy.compas.sct.commons.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TExtRef;


@Getter
@Setter
@NoArgsConstructor
public class ExtRefInfo {
    private String iedName;
    private String ldInst;
    private String prefix;
    private String lnClass;
    private String lnInst;

    private ExtRefSignalInfo signalInfo;
    private ExtRefBindingInfo bindingInfo;
    private ExtRefSourceInfo sourceInfo;

    public ExtRefInfo(TExtRef tExtRef) {
        bindingInfo = new ExtRefBindingInfo(tExtRef);
        sourceInfo = new ExtRefSourceInfo(tExtRef);
        signalInfo = new ExtRefSignalInfo(tExtRef);
    }
}
