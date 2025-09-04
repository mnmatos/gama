package com.digitallib.serialization;

import com.digitallib.exception.RepositoryException;
import com.digitallib.manager.CategoryManager;
import com.digitallib.model.Classe;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;

public class ClasseDeserializer  extends JsonDeserializer<Classe> {

    @Override
    public Classe deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        CategoryManager categoryManager = new CategoryManager();

        JsonNode node = parser.getCodec().readTree(parser);

        String classe = node.asText();

        try {
            return categoryManager.getClasseForName(classe);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}



