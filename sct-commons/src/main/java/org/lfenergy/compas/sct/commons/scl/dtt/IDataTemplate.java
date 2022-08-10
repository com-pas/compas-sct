// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.dtt.IDataTemplate DataTemplate}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link IDataTemplate#getDataTypeTemplateAdapter <em>get DataTypeTemplateAdapter</em>}</li>
 * </ul>
 *
 */
public interface IDataTemplate {

    /**
     * Gets linked DataTypeTemplateAdapter as parent
     * @return <em>DataTypeTemplateAdapter</em> object
     */
    DataTypeTemplateAdapter getDataTypeTemplateAdapter();
}
