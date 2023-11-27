// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

/**
 * A representation of the model object <em><b>FCDA</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link FCDAInfo#getDaName <em>Da Name</em>}</li>
 *   <li>{@link FCDAInfo#getDoName <em>Do Name</em>}</li>
 *   <li>{@link FCDAInfo#getFc <em>Fc</em>}</li>
 *   <li>{@link FCDAInfo#getIx  em>Ix</em>}</li>
 *   <li>{@link FCDAInfo#getLdInst <em>Ld Inst</em>}</li>
 *   <li>{@link FCDAInfo#getLnClass <em>Ln Class</em>}</li>
 *   <li>{@link FCDAInfo#getLnInst <em>Ln Inst</em>}</li>
 *   <li>{@link FCDAInfo#getPrefix <em>Prefix</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TFCDA
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FCDAInfo {

    private String dataSet;

    private TFCEnum fc;
    private String ldInst;
    private String prefix;
    private String lnClass;
    private String lnInst;
    private DoTypeName doName; //doName.[...sdoNames]
    private DaTypeName daName; //daName.[...bdaNames]
    private Long ix;

    /**
     * Checks FCDAInfo validity
     * @return validity state
     */
    public boolean isValid() {
        return doName != null && doName.isDefined();
    }

}
