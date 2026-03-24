package com.digitallib.reference.generator;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.*;

class BookReferenceGeneratorTest {

    private final Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Entity fakeLocal = new Entity(EntityType.LOCAL, "Feira de Santana", "", "5");
        Entity fakeAuthor = new Entity(EntityType.PESSOA, "Alcina Dantas", "for unit test", "-999");

        try (MockedStatic<EntityManager> mockedEntityManager = mockStatic(EntityManager.class)) {
            mockedEntityManager.when(() -> EntityManager.getEntryById("5")).thenReturn(fakeLocal);
            mockedEntityManager.when(() -> EntityManager.getEntryById("-999")).thenReturn(fakeAuthor);

            Documento doc = createBaseDocument(fakeAuthor);

            BookReferenceGenerator bookReferenceGenerator = new BookReferenceGenerator();
            Reference reference = bookReferenceGenerator.generate(doc);

            logger.info(reference.toString());
            Assertions.assertEquals("DANTAS, Alcina. Livro falso. 4. ed. Feira de Santana: Editora falsa, 1927. p. 3.", reference.toString().trim());
        }
    }

    private static Documento createBaseDocument(Entity author) {
        Documento doc = new Documento();
        SubClasse testSubClass = new SubClasse("01a", "test_sub_class", "Test sub", new ArrayList<>());
        doc.setClasseProducao(new Classe("01", "test_class", "Test Class", Collections.singletonList(testSubClass)));
        doc.setSubClasseProducao(testSubClass);
        doc.setTitulo("Livro falso");
        doc.setEncontradoEm("Folha do Norte");
        doc.setLugarPublicacao("5");
        doc.setAnoRevista("22");

        doc.setAutores(Collections.singletonList(author.getId()));


        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setAno("1927");
        dataDocumento.setDataIncerta(false);
        doc.setDataDocumento(dataDocumento);

        doc.setEdicao(4);
        doc.setEditora("Editora falsa");
        doc.setPaginaInicio(3);
        doc.setNumPublicacao(953);
        return doc;
    }
}