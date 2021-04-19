package org.lfenergy.compas.sct.model;

public interface IScd <ID> {
    ID getId();
    byte[] getRawXml();
    ID getHeaderId();
}