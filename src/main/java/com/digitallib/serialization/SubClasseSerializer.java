package com.digitallib.serialization;

import com.digitallib.model.Classe;
import com.digitallib.model.SubClasse;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class SubClasseSerializer extends JsonSerializer<SubClasse> {

    @Override
    public void serialize(SubClasse subClasse,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeObject(subClasse.getName());
    }
}
