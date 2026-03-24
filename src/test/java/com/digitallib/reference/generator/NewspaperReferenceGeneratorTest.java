package com.digitallib.reference.generator;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.manager.EntityManager;
import com.digitallib.model.Classe;
import com.digitallib.model.DataDocumento;
import com.digitallib.model.Documento;
import com.digitallib.model.SubClasse;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.entity.EntityType;
import com.digitallib.reference.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.*;

class NewspaperReferenceGeneratorTest {

    private final Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException, EntityNotFoundException {
        Entity fakeEntity = new Entity(EntityType.LOCAL, "Feira de Santana", "", "5");

        try (MockedStatic<EntityManager> mockedEntityManager = mockStatic(EntityManager.class)) {
            mockedEntityManager.when(() -> EntityManager.getEntryById("5")).thenReturn(fakeEntity);

            Documento doc = createBaseDocument();
            NewspaperReferenceGenerator newspaperReferenceGenerator = new NewspaperReferenceGenerator();
            Reference reference = newspaperReferenceGenerator.generate(doc);

            logger.info(reference.toString());
            Assertions.assertEquals("DIREITOS femininos. Folha do Norte, Feira de Santana, ano 22, n. 953, 22 out. 1927, p. 3.", reference.toString().trim());
        }
    }

    private static Documento createBaseDocument() {
        Documento doc = new Documento();
        SubClasse testSubClass = new SubClasse("01a", "test_sub_class", "Test sub", new ArrayList<>());
        doc.setClasseProducao(new Classe("01", "test_class", "Test Class", Collections.singletonList(testSubClass)));
        doc.setSubClasseProducao(testSubClass);
        doc.setTitulo("Direitos femininos");
        doc.setEncontradoEm("Folha do Norte");
        doc.setLugarPublicacao("5");
        doc.setAnoRevista("22");

        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setDia(22);
        dataDocumento.setMes(10);
        dataDocumento.setAno("1927");
        dataDocumento.setDataIncerta(false);
        doc.setDataDocumento(dataDocumento);

        doc.setPaginaInicio(3);
        doc.setNumPublicacao(953);

        return doc;
    }
}