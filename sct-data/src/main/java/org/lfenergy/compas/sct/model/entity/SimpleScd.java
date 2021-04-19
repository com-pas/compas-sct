// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.lfenergy.compas.sct.model.IScd;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@Getter
@Setter
@Entity
@Table(name = "SCD")
public class SimpleScd implements IScd<UUID>, Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "ID")
    private UUID id;

    @Column(name="RAW_XML")
    @Lob
    @Type(type = "xmltype")
    private byte[] rawXml;

    @Column(name = "FILE_NAME",unique = true)
    private String fileName;

    @Column(name = "HEADER_ID")
    private UUID headerId;

    @Column(name = "HEADER_VERSION")
    private String headerVersion;

    @Column(name = "HEADER_REVISION")
    private String headerRevision;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o == null) return false;
        if (o.getClass() != getClass()) return false;
        SimpleScd simpleScd = (SimpleScd) o;
        return Objects.equals(id, simpleScd.id) &&
                Objects.equals(fileName, simpleScd.fileName) &&
                Objects.equals(headerId, simpleScd.headerId) &&
                Objects.equals(headerVersion, simpleScd.headerVersion) &&
                Objects.equals(headerRevision, simpleScd.headerRevision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileName, headerId, headerVersion, headerRevision);
    }
}
