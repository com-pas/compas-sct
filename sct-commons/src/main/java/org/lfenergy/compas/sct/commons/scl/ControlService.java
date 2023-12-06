// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.lfenergy.compas.scl2007b4.model.*;

import java.util.stream.Stream;

public class ControlService {

    public <T extends TControl> Stream<T> getControls(TAnyLN tAnyLN, Class<T> tControlClass){
        if (tControlClass == TGSEControl.class && tAnyLN instanceof TLN0 tln0 && tln0.isSetGSEControl()){
            return tln0.getGSEControl().stream().map(tControlClass::cast);
        } else if (tControlClass == TSampledValueControl.class && tAnyLN instanceof TLN0 tln0 && tln0.isSetSampledValueControl()){
            return tln0.getSampledValueControl().stream().map(tControlClass::cast);
        } else if (tControlClass == TReportControl.class && tAnyLN.isSetReportControl()){
            return tAnyLN.getReportControl().stream().map(tControlClass::cast);
        } else if (tControlClass == TLogControl.class){
            return tAnyLN.getLogControl().stream().map(tControlClass::cast);
        }
        return Stream.empty();
    }

}
