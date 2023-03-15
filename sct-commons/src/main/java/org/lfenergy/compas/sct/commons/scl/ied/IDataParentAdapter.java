// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TSDI;
import org.lfenergy.compas.scl2007b4.model.TUnNaming;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.List;
import java.util.Optional;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.IDataParentAdapter IDataParentAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link IDataParentAdapter#getStructuredDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference</em>}</li>
 *      <li>{@link IDataParentAdapter#getDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference By Name</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link IDataParentAdapter#addDAI <em>Add <b>TDAI </b>under this object</em>}</li>
 *      <li>{@link IDataParentAdapter#addSDOI <em>Add <b>TSDI </b>under this object</em>}</li>
 *    </ul>
 *    </ul>
 * </ol>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TDAI
 * @see org.lfenergy.compas.scl2007b4.model.TSDI
 */
public interface IDataParentAdapter extends IDataAdapter {


    /**
     * Get name of the DOI or SDI
     *
     * @return name of the DOI or SDI
     */
    String getName();

    /**
     * Get children (SDI or DAI) of this element
     *
     * @return currentElem.getSDIOrDAI()
     */
    List<TUnNaming> getSDIOrDAI();

    /**
     * Convert the given TSDI to a RootSDIAdapter or SDIAdapter
     * The current instance must be the parent of the given childTSDI
     *
     * @param childTSDI child SDI to convert to an adapter
     * @return RootSDIAdapter when current instance is a DOI or SDIAdapter otherwise
     * @throws IllegalArgumentException when the current instance is not the parent of the given childTSDI
     */
    IDataParentAdapter toAdapter(TSDI childTSDI);

    /**
     * Convert the given TDAI to a AbstractDAIAdapter
     * The current instance must be the parent of the given childTSDI
     *
     * @param childTDAI child SDI to convert to an adapter
     * @return AbstractDAIAdapter
     * @throws IllegalArgumentException when the current instance is not the parent of the given childTSDI
     */
    AbstractDAIAdapter<?> toAdapter(TDAI childTDAI);

    /**
     * Search SDI by name, directly under current this DOI or SDI
     *
     * @param sdiName name of SDI to find
     * @return <em>RootSDIAdapter</em> object
     * @throws ScdException throws when specified name of SDI not present in current DOI or SDI
     */
    default Optional<IDataParentAdapter> findStructuredDataAdapterByName(String sdiName) throws ScdException {
        return getSDIOrDAI()
                .stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(tsdi -> tsdi.getName().equals(sdiName))
                .map(this::toAdapter)
                .findFirst();
    }

    /**
     * Gets SDI by name, directly under current this DOI or SDI
     *
     * @param sdiName name of SDI to find
     * @return found SDI
     * @throws ScdException throws when specified name of SDI not present in current DOI or SDI
     */
    default IDataParentAdapter getStructuredDataAdapterByName(String sdiName) throws ScdException {
        return findStructuredDataAdapterByName(sdiName)
                .orElseThrow(() -> new ScdException(
                        String.format("Unknown SDI (%s) in this DOI or SDI (%s)", sdiName, getName())
                ));
    }

    /**
     * Gets DAI from current DOI
     *
     * @param daiName name of DAI to get
     * @return found DAI
     * @throws ScdException throws when specified name of DAI not present in current DOI
     */
    default AbstractDAIAdapter<?> getDataAdapterByName(String daiName) throws ScdException {
        return findDataAdapterByName(daiName)
                .orElseThrow(() -> new ScdException(
                        String.format("Unknown DAI (%s) in this SDI (%s)", daiName, getName())
                ));
    }

    /**
     * Finds DAI from current DOI
     *
     * @param daiName name of DAI to get
     * @return found DAI
     * @throws ScdException throws when specified name of DAI not present in current DOI
     */
    default Optional<AbstractDAIAdapter<?>> findDataAdapterByName(String daiName) {
        return getSDIOrDAI()
                .stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
                .map(TDAI.class::cast)
                .filter(tdai -> tdai.getName().equals(daiName))
                .findFirst()
                .map(this::toAdapter);
    }

    /**
     * Adds DAI to current DOI
     *
     * @param name        name of DAI to add
     * @param isUpdatable updatability state of DAI
     * @return <em>DAIAdapter</em> object as added DAI
     */
    default AbstractDAIAdapter<?> addDAI(String name, boolean isUpdatable) {
        TDAI tdai = new TDAI();
        tdai.setName(name);
        tdai.setValImport(isUpdatable);
        getSDIOrDAI().add(tdai);
        return toAdapter(tdai);
    }

    /**
     * Adds SDOI to SDI in current DOI
     *
     * @param sdiName name of SDOI to add
     * @return <em>RootSDIAdapter</em> object as added SDOI
     */
    default IDataParentAdapter addSDOI(String sdiName) {
        TSDI tsdi = new TSDI();
        tsdi.setName(sdiName);
        getSDIOrDAI().add(tsdi);
        return toAdapter(tsdi);
    }

    /**
     * Search for the closest match for a list of structured data name (SDIs names = list of SDOs or BDAs names)
     *
     * @param sNames     list of either SDO or BDA names
     * @param fromIndex  position in the list (0 to list size minus one)
     * @param isBdaNames true if list of BDA names, false otherwise
     * @return a pair of SCL data object Adapter and depth of the list (an index of the list)
     */
    default Pair<IDataAdapter, Integer> findDeepestMatch(List<String> sNames, int fromIndex, boolean isBdaNames) {
        int index = -1;
        int sz = sNames.size();
        IDataAdapter diAdapter = null;
        IDataAdapter currSdiAdapter = this;
        for (int currIndex = fromIndex; currIndex < sz; currIndex++) {
            try {
                if (currIndex == sz - 1 && isBdaNames) {
                    currSdiAdapter = ((IDataParentAdapter) currSdiAdapter).getDataAdapterByName(
                            sNames.get(currIndex)
                    );
                } else {
                    currSdiAdapter = ((IDataParentAdapter) currSdiAdapter).getStructuredDataAdapterByName(
                            sNames.get(currIndex)
                    );
                }
                diAdapter = currSdiAdapter;
                index = currIndex;
            } catch (ScdException e) {
                // partial match was found if index >= 0 or unknown data object names
                break;
            }
        }
        return Pair.of(diAdapter, index);
    }
}
