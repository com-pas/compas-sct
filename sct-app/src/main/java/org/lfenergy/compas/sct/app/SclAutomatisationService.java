// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.SclService;

import java.util.Optional;

@Slf4j
public class SclAutomatisationService {

    private SclAutomatisationService(){ throw new IllegalStateException("SclService class"); }

    public static SclRootAdapter createSCD(@NonNull SCL ssd, String hVersion, String hRevision) throws ScdException {
        return SclService.initScl(Optional.empty(),hVersion,hRevision);
    }
}
