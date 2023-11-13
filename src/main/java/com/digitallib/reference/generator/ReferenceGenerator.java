package com.digitallib.reference.generator;

import com.digitallib.model.Documento;
import com.digitallib.reference.Reference;

public interface ReferenceGenerator {
    Reference generate(Documento documento);
}
