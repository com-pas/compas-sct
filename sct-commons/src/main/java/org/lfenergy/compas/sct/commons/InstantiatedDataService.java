// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.SclConstructorHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service to manage instantiated data (DOI, SDI and DAI)
 * From IEC61850-6 :
 * - DAI (Instantiated Data Attribute),
 * - DOI (Instantiated Data Object),
 * - SDI (Instantiated Sub-DATA; middle name part of a structured DATA name),
 */
public class InstantiatedDataService {

    public Optional<TDOI> findDoi(TAnyLN anyLN, String doiName) {
        return anyLN.getDOI().stream()
                .filter(tdoi -> doiName.equals(tdoi.getName()))
                .findFirst();
    }

    public Optional<TSDI> findSdi(DoiOrSdi doiOrSdi, String sdiName) {
        return doiOrSdi.getSDIOrDAI().stream()
                .filter(TSDI.class::isInstance)
                .map(TSDI.class::cast)
                .filter(tsdi -> sdiName.equals(tsdi.getName()))
                .findFirst();
    }

    public Optional<TDAI> findDai(DoiOrSdi doiOrSdi, String daiName) {
        return doiOrSdi.getSDIOrDAI().stream()
                .filter(TDAI.class::isInstance)
                .map(TDAI.class::cast)
                .filter(tdai -> daiName.equals(tdai.getName()))
                .findFirst();
    }

    public TDOI createDoiIfNotExists(TAnyLN anyLN, String doiName) {
        return findDoi(anyLN, doiName).orElseGet(() -> {
            TDOI tdoi = SclConstructorHelper.newDoi(doiName);
            anyLN.getDOI().add(tdoi);
            return tdoi;
        });
    }

    public TSDI createSdiIfNotExists(DoiOrSdi doiOrSdi, String sdiName) {
        return findSdi(doiOrSdi, sdiName).orElseGet(() -> {
            TSDI tsdi = SclConstructorHelper.newSdi(sdiName);
            doiOrSdi.getSDIOrDAI().add(tsdi);
            return tsdi;
        });
    }

    public TDAI createDaiIfNotExists(DoiOrSdi doiOrSdi, String daiName, Boolean valImportOnCreate) {
        return findDai(doiOrSdi, daiName).orElseGet(() -> {
            TDAI tdai = SclConstructorHelper.newDai(daiName);
            if (valImportOnCreate != null){
                tdai.setValImport(valImportOnCreate);
            }
            doiOrSdi.getSDIOrDAI().add(tdai);
            return tdai;
        });
    }

    /**
     * Return the DAI (in DOI/SDI/DAI chain) matching the given data type reference name
     * If it does not exist, create the missing DOI/SDI/DAI elements in this LN/LN0 if needed, based on the given parameters
     * DOI is the equivalent of the DO
     * SDI is the equivalent of a type with bType="Struct". It can be a SDO, DA or BDA.
     * DAI is the equivalent of the final leaf : DA or BDA with bType != "Struct".
     * Be careful, this method does not check that the given data type is allowed by the lnType of this LN/LN0.
     * It does not even check if the data type exists in DataTypeTemplate section.
     * That means that it will create the missing DOI/SDI/DAI, even if it is not consistent with DataTypeTemplate section.
     * It is the caller responsibility to ensure the consistency between the given data type and the lnType of this LN/LN0 (which refer to DataTypeTemplate section).
     * See 9.3.5 "LN0 and other Logical Nodes" of IEC 61850-6.
     *
     * @param dataTypeRef          Reference name of data : DO/SDO/DA/BDA names, in order from parent to child, separated by a period
     *                             (Ex: "Do1.da1", "Do2.sdoA.sdoB.da2.bdaA.bdaB")
     * @return existing DAI or created DAI.
     */
    public TDAI createDoiSdiDaiChainIfNotExists(TAnyLN anyLN, String dataTypeRef, Boolean valImportOnCreate) {
        // parse dataTypeRef
        String[] names = dataTypeRef.split("\\.");
        if (names.length < 2 || Arrays.stream(names).anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("dataTypeRef must be valid with at least a DO and a DA, but got: " + dataTypeRef);
        }
        String doiName = names[0];
        List<String> sdiNames = Arrays.asList(names).subList(1, names.length - 1);
        String daiName = names[names.length - 1];
        // Create DOI if not exists
        DoiOrSdi latestParent = new DoiOrSdi(createDoiIfNotExists(anyLN, doiName));
        // Create SDIs if not exist
        latestParent = sdiNames.stream()
                .reduce(latestParent,
                        (doiOrSdi, sdiName) -> new DoiOrSdi(createSdiIfNotExists(doiOrSdi, sdiName)),
                        (doiOrSdi1, doiOrSdi2) -> {
                            throw new ScdException("This reduction cannot be parallel");
                        });
        // Create DAI if not exists
        return createDaiIfNotExists(latestParent, daiName, valImportOnCreate);
    }

    record DoiOrSdi(Object doiOrSdi) {
        DoiOrSdi {
            Objects.requireNonNull(doiOrSdi);
            if (!(doiOrSdi instanceof TDOI || doiOrSdi instanceof TSDI)) {
                throw new IllegalArgumentException("Expecting TDOI or TSDI instance, but got: " + doiOrSdi.getClass().getSimpleName());
            }
        }

        public List<TUnNaming> getSDIOrDAI() {
            return switch (doiOrSdi) {
                case TDOI tdoi -> tdoi.getSDIOrDAI();
                case TSDI tsdi -> tsdi.getSDIOrDAI();
                default -> throw new IllegalStateException("Unexpected instance: " + doiOrSdi.getClass().getSimpleName());
            };
        }
    }
}
