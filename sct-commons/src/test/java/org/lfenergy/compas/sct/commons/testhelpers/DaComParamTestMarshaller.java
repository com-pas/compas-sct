// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.lfenergy.compas.core.commons.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.model.da_comm.DACOMM;

public class DaComParamTestMarshaller extends MarshallerWrapper<DACOMM> {

    public DaComParamTestMarshaller(Unmarshaller jaxbUnmarshaller, Marshaller jaxbMarshaller) {
        super(jaxbUnmarshaller, jaxbMarshaller);
    }

    @Override
    protected Class<DACOMM> getResultClass() {
        return DACOMM.class;
    }

    public static class Builder extends MarshallerWrapper.Builder<DaComParamTestMarshaller, DACOMM> {
        public Builder() {
            withProperties("da_comm-marshaller-config.yml");
        }

        @Override
        protected DaComParamTestMarshaller createMarshallerWrapper(Unmarshaller jaxbUnmarshaller,
                                                                   Marshaller jaxbMarshaller) {
            return new DaComParamTestMarshaller(jaxbUnmarshaller, jaxbMarshaller);
        }
    }
}
