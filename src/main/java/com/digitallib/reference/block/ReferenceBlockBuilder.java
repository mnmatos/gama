package com.digitallib.reference.block;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.Documento;

import java.util.List;

public interface ReferenceBlockBuilder {
    List<ReferenceBlock> build(Documento doc) throws ReferenceBlockBuilderException;
}
