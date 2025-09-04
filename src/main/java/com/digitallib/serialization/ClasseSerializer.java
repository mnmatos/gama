package com.digitallib.serialization;

import com.digitallib.exception.RepositoryException;
import com.digitallib.manager.CategoryManager;
import com.digitallib.model.Classe;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ClasseSerializer extends JsonSerializer<Classe> {

    @Override
    public void serialize(Classe classe,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeObject(classe.getName());
    }
}
