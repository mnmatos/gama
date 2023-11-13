package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public interface ReferenceBlockBuilder {
    ReferenceBlock build(Documento doc);
}
