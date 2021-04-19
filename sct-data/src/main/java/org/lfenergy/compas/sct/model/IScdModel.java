package org.lfenergy.compas.sct.model;

public interface IScdModel<ID> {
    ID getId();
    byte[] getRawXml();
}
