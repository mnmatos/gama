package com.digitallib.serialization;

import com.digitallib.exception.RepositoryException;
import com.digitallib.manager.CategoryManager;
import com.digitallib.model.Classe;
import com.digitallib.model.SubClasse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class SubClasseDeserializer extends JsonDeserializer<SubClasse> {

    @Override
    public SubClasse deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        CategoryManager categoryManager = new CategoryManager();

        JsonNode node = parser.getCodec().readTree(parser);

        String subClasse = node.asText();

        try {
            return categoryManager.getSubClasseForName(subClasse.toLowerCase());
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}
