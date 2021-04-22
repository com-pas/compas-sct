// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.service;

import org.lfenergy.compas.exception.ScdException;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.sct.exception.CompasDataAccessException;
import org.lfenergy.compas.sct.model.IIedDTO;
import org.lfenergy.compas.sct.model.IScd;
import org.lfenergy.compas.sct.model.ISubNetworkDTO;
import org.lfenergy.compas.sct.model.dto.ExtRefSignalInfo;
import org.springframework.lang.NonNull;

import java.util.Set;
import java.util.UUID;

public interface ISCDService<T extends IScd<UUID>> {
    /**
     * Find SCD object for given ID
     * @param id object identifier
     * @return SCD object identified
     * @throws CompasDataAccessException Data access exception on error
     */
    T findScd(UUID id) throws CompasDataAccessException;

    /**
     * Initiate SCD object with a generated header ID (UUID)
     * @param filename scd file name
     * @param hVersion scd header version
     * @param hRevision scd header revision
     * @return the initiated scd
     * @throws ScdException exception thrown on error
     */
    T initiateSCD(@NonNull String filename, @NonNull String hVersion, @NonNull String hRevision) throws ScdException;

    /**
     * Add history item to the scd
     * @param scdObj scd representation in database
     * @param who scd modifier
     * @param when scd modification's date
     * @param why scd modification's reason
     * @return the modified scd
     * @throws ScdException exception thrown on error
     */
    T addHistoryItem(T scdObj, String who, String when, String why) throws ScdException;

    /**
     * Add/rename IED from ICD file to SCD
     * This method import the ICD's DataTemplate as well
     * @param scdObj SCD object receiving the ICD
     * @param iedName iedName ofr the ICD to import
     * @param icd Objectified ICD
     * @return Scd object with the IED imported
     * @throws ScdException exception thrown on error
     */
    T addIED(T scdObj, String iedName, SCL icd) throws ScdException;

    /**
     * Add/rename IED from ICD file to SCD
     * This method import the ICD's DataTemplate as well
     * @param scdObj SCD object receiving the ICD
     * @param iedName iedName ofr the ICD to import
     * @param rawIcd raw ICD content
     * @return Scd object with the IED imported
     * @throws ScdException exception thrown on error
     */
    T addIED(T scdObj, String iedName,  byte[] rawIcd) throws ScdException;

    /**
     * Extract External Reference signals for given IED, AccessPoint and LDevice.
     * @param scdObj SCD object encapsulating the receiver SCD of the ICD
     * @param iedName IED name
     * @param ldInst the logical device
     * @param <D> DTO type parameter
     * @return IED generic DTO
     * @throws ScdException exception thrown on error
     */
    <D extends IIedDTO> D extractExtRefs(T scdObj, String iedName, String ldInst)  throws ScdException;

    /**
     *
     * @param scdObj
     * @param iedName
     * @param ldInst
     * @param filter
     * @param <D>
     * @param <U>
     * @return
     * @throws ScdException
     */
    <D extends IIedDTO> Set<D> extractExtRefBindingInfo(T scdObj, String iedName,
                                                        String ldInst, ExtRefSignalInfo filter) throws ScdException;

    /**
     *
     * @param scdObj
     * @param <D>
     * @return
     * @throws ScdException
     */
    <D extends ISubNetworkDTO> Set<D> getSubnetwork(T scdObj) throws ScdException;

    /**
     *
     * @param scdObject
     * @param subNetworkDTO
     * @return
     * @throws ScdException
     */
    T addSubnetworks(T scdObject,Set<? extends ISubNetworkDTO> subNetworkDTO) throws ScdException;
}
