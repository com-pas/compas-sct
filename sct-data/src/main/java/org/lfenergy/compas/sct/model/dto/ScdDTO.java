package org.lfenergy.compas.sct.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ScdDTO {
    private UUID id;
    private String fileName;
    private UUID headerId;
    private String headerRevision;
    private String headerVersion;
    private String who;
    private String what;
    private String why;

    public ScdDTO(UUID id, String fileName, UUID headerId, String headerRevision, String headerVersion) {
        this.id = id;
        this.fileName = fileName;
        this.headerId = headerId;
        this.headerRevision = headerRevision;
        this.headerVersion = headerVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        ScdDTO scdDTO = (ScdDTO) o;
        return Objects.equals(id, scdDTO.id) &&
                Objects.equals(fileName, scdDTO.fileName) &&
                Objects.equals(headerId, scdDTO.headerId) &&
                Objects.equals(headerRevision, scdDTO.headerRevision) &&
                Objects.equals(headerVersion, scdDTO.headerVersion) &&
                Objects.equals(who, scdDTO.who) &&
                Objects.equals(what, scdDTO.what) &&
                Objects.equals(why, scdDTO.why);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileName, headerId, headerRevision, headerVersion, who, what, why);
    }
}
