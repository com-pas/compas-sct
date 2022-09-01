// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.List;

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
     * Gets SDI by name from current DOI
     * @param sName name of SDI to get
     * @return <em>RootSDIAdapter</em> object
     * @throws ScdException throws when specified name of SDI not present in current DOI
     */
    <S extends IDataParentAdapter> S getStructuredDataAdapterByName(String sName) throws ScdException;

    /**
     * Gets DAI from current DOI
     * @param sName name of DAI to get
     * @return <em>DAIAdapter</em> object
     * @throws ScdException throws when specified name of DAI not present in current DOI
     */
    <S extends AbstractDAIAdapter> S getDataAdapterByName(String sName) throws ScdException;

    /**
     * Adds DAI to current DOI
     * @param name name of DAI to add
     * @param isUpdatable updatability state of DAI
     * @return <em>DAIAdapter</em> object as added DAI
     */
    <S extends AbstractDAIAdapter> S addDAI(String name,boolean isUpdatable);


    /**
     * Adds SDOI to SDI in current DOI
     * @param sdoNme name of SDOI to add
     * @return <em>RootSDIAdapter</em> object as added SDOI
     */
    <S extends IDataParentAdapter> S addSDOI(String sdoNme) ;

    /**
     * Search for the closest match for a list of structured data name (SDIs names = list of SDOs or BDAs names)
     * @param sNames list of either SDO or BDA names
     * @param fromIndex position in the list (0 to list size minus one)
     * @param isBdaNames true if list of BDA names, false otherwise
     * @return a pair of SCL data object Adapter and depth of the list (an index of the list)
     */
    default Pair<? extends IDataAdapter,Integer> findDeepestMatch(List<String> sNames, int fromIndex, boolean isBdaNames){
        int index = -1;
        int sz = sNames.size();
        IDataAdapter diAdapter = null;
        IDataAdapter currSdiAdapter = this;
        for(int currIndex = fromIndex; currIndex < sz; currIndex++){
            try {
                if(currIndex == sz-1 && isBdaNames){
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
        return Pair.of(diAdapter,index);
    }
}
