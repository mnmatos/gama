package com.digitallib.reference.generator;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.manager.EntityManager;
import com.digitallib.model.*;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.entity.EntityType;
import com.digitallib.reference.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.*;

class PaperReferenceGeneratorTest {

    private final Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Entity fakeLocal = new Entity(EntityType.LOCAL, "Feira de Santana", "", "5");
        Entity fakeAuthor = new Entity(EntityType.PESSOA, "Alcina Dantas", "for unit test", "-999");

        try (MockedStatic<EntityManager> mockedEntityManager = mockStatic(EntityManager.class)) {
            mockedEntityManager.when(() -> EntityManager.getEntryById("5")).thenReturn(fakeLocal);
            mockedEntityManager.when(() -> EntityManager.getEntryById("-999")).thenReturn(fakeAuthor);

            Documento doc = createBaseDocument(fakeAuthor);

            PaperReferenceGenerator paperReferenceGenerator = new PaperReferenceGenerator();
            Reference reference = paperReferenceGenerator.generate(doc);

            logger.info(reference.toString());
            Assertions.assertEquals("DANTAS, Alcina. Titulo do artigo: Subtitulo do artigo. Titulo da revista, Feira de Santana, ano 3, v. 9, n. 953, p. 3-7, out. 2009. DOI: https://dx.doi.org/123123.12312. Disponível em: https://sitefalso.local. Acesso em: 12 maio. 2005.", reference.toString().trim());
        }
    }

    private static Documento createBaseDocument(Entity author) {
        Documento doc = new Documento();
        SubClasse testSubClass = new SubClasse("01a", "test_sub_class", "Test sub", new ArrayList<>());
        doc.setClasseProducao(new Classe("01", "test_class", "Test Class", Collections.singletonList(testSubClass)));
        doc.setSubClasseProducao(testSubClass);
        doc.setTitulo("Titulo do artigo");
        doc.setSubtitulo("Subtitulo do artigo");
        doc.setTituloPublicacao("Titulo da revista");
        doc.setLugarPublicacao("5");
        doc.setAnoRevista("22");

        doc.setAutores(Collections.singletonList(author.getId()));

        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setMes(10);
        dataDocumento.setAno("2009");
        dataDocumento.setDataIncerta(false);
        doc.setDataDocumento(dataDocumento);

        doc.setAnoRevista("3");
        doc.setVolume("9");
        doc.setEditora("Editora falsa");
        doc.setPaginaInicio(3);
        doc.setNumPagina(5);
        doc.setNumPublicacao(953);

        InfoAdicionais infoAdicionais = new InfoAdicionais();
        infoAdicionais.setDoi("https://dx.doi.org/123123.12312");
        doc.setInfoAdicionais(infoAdicionais);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
        LocalDate date = LocalDate.parse("2005 05 12", formatter);

        doc.setDisponivelEm("https://sitefalso.local");
        doc.setAcessoEm(date);
        return doc;
    }
}