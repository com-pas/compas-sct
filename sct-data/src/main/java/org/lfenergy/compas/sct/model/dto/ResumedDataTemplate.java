package org.lfenergy.compas.sct.model.dto;


import lombok.Getter;
import lombok.Setter;
import org.lfenergy.compas.scl.TFCEnum;
import org.lfenergy.compas.scl.TPredefinedCDCEnum;

@Getter
@Setter
public class ResumedDataTemplate {

    private String lnType;
    private String lnClass;
    private String doName; // doName[.sdoName[.sdoName]]
    private TPredefinedCDCEnum cdc;
    private String daName; // doName[.bdaName[.bdaName]]
    private TFCEnum fc;

    @Override
    public String toString() {
        return "ResumedDataTemplate{" +
                "lnType='" + lnType + '\'' +
                ", lnClass='" + lnClass + '\'' +
                ", doName='" + doName + '\'' +
                ", cdc='" + cdc + '\'' +
                ", daName='" + daName + '\'' +
                ", fc=" + fc +
                '}';
    }
}
