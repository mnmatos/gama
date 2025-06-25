package com.digitallib.code;

import com.digitallib.model.Documento;

public interface CodeGenerator {

    String generateCode (Documento documento);

    String generateCodeWithoutAppendix (Documento documento);
}
