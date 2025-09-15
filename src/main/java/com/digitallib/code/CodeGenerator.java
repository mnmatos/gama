package com.digitallib.code;

import com.digitallib.exception.ValidationException;
import com.digitallib.model.Documento;

public interface CodeGenerator {

    String generateCode (Documento documento) throws ValidationException;

    String generateCodeWithoutAppendix (Documento documento) throws ValidationException;
}
