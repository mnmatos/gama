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

class InterviewReferenceGeneratorTest {

    private final Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Entity fakeLocal = new Entity(EntityType.LOCAL, "Feira de Santana", "", "5");
        Entity fakeAuthor = new Entity(EntityType.PESSOA, "Leolindo Guimarães", "for unit test", "1");

        try (MockedStatic<EntityManager> mockedEntityManager = mockStatic(EntityManager.class)) {
            mockedEntityManager.when(() -> EntityManager.getEntryById("5")).thenReturn(fakeLocal);
            mockedEntityManager.when(() -> EntityManager.getEntryById("1")).thenReturn(fakeAuthor);

            Documento doc = createBaseDocument(fakeAuthor);

            InterviewReferenceGenerator interviewReferenceGenerator = new InterviewReferenceGenerator();
            Reference reference = interviewReferenceGenerator.generate(doc);

            logger.info(reference.toString());
            Assertions.assertEquals("GUIMARÃES, Leolindo. Entrevista com pessoa X. [Entrevista cedida a] Pollianna dos Santos Ferreira Silva. Feira de Santana, out. 2021. Disponível em: https://sitefalso.local. Acesso em: 12 maio. 2023.", reference.toString().trim());
        }
    }

    private static Documento createBaseDocument(Entity author) {
        Documento doc = new Documento();
        SubClasse testSubClass = new SubClasse("01a", "test_sub_class", "Test sub", new ArrayList<>());
        doc.setClasseProducao(new Classe("01", "test_class", "Test Class", Collections.singletonList(testSubClass)));
        doc.setSubClasseProducao(testSubClass);
        doc.setAutores(Collections.singletonList(author.getId()));
        doc.setTitulo("Entrevista com pessoa X");
        doc.setLugarPublicacao("5");

        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setMes(10);
        dataDocumento.setAno("2021");
        dataDocumento.setDataIncerta(false);
        doc.setDataDocumento(dataDocumento);

        InfoAdicionais infoAdicionais = new InfoAdicionais();
        infoAdicionais.setEntrevistador("Pollianna dos Santos Ferreira Silva");
        doc.setInfoAdicionais(infoAdicionais);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
        LocalDate date = LocalDate.parse("2023 05 12", formatter);

        doc.setDisponivelEm("https://sitefalso.local");
        doc.setAcessoEm(date);

        return doc;
    }
}