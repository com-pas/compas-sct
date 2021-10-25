// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
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
    private byte[] rawXml; // NEXT : may be SCL type directly => delegate conversion to hibernate completely

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
        if (o == null || o.getClass() != getClass()) return false;

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
