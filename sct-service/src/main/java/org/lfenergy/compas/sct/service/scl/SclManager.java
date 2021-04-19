// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.service.scl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lfenergy.compas.exception.ScdException;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.THeader;
import org.lfenergy.compas.scl.TIED;

import java.util.Optional;

@Getter
@NoArgsConstructor
public class SclManager {
    private SCL receiver;

    public SclManager(SCL receiver) {
        this.receiver = receiver;
    }

    public SclManager(String hId, String hVersion, String hRevision){
        this.receiver = SclManager.initialize(hId,hVersion,hRevision);
    }



    public Optional<TIED> getIED(String iedName){
        return receiver.getIED().stream().filter(tied -> iedName.equals(tied.getName())).findFirst();
    }

    /**
     * Add IED named <a>iedName</a> from a given SCL to recipient SCL
     * @param provider SCL containing IED to add
     * @param iedName IED name to be added
     * @throws ScdException
     */
    public void addIED(SCL provider, String iedName) throws ScdException {
        SclIEDManager sclIEDManager = new SclIEDManager(receiver);
        sclIEDManager.addIed(provider,iedName);
    }

    /*--------------------------------------------------------*/
    /*   Static methods                                       */
    /*--------------------------------------------------------*/

    public static SCL initialize(String hId, String hVersion, String hRevision) {

        SCL scd = new SCL();
        scd.setRelease((short) 4);
        scd.setVersion("2007");
        scd.setRevision("B");
        THeader tHeader = new THeader();
        tHeader.setRevision(hRevision);
        tHeader.setVersion(hVersion);
        tHeader.setId(hId);
        scd.setHeader(tHeader);

        return scd;
    }
}
