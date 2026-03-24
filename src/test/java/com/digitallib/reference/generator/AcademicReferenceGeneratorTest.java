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

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.*;

class AcademicReferenceGeneratorTest {

    private final Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Entity fakeLocal = new Entity(EntityType.LOCAL, "Feira de Santana", "", "5");
        Entity fakeAuthor = new Entity(EntityType.PESSOA, "Alcina Dantas", "for unit test", "-999");

        try (MockedStatic<EntityManager> mockedEntityManager = mockStatic(EntityManager.class)) {
            mockedEntityManager.when(() -> EntityManager.getEntryById("5")).thenReturn(fakeLocal);
            mockedEntityManager.when(() -> EntityManager.getEntryById("-999")).thenReturn(fakeAuthor);

            Documento doc = createBaseDocument(fakeAuthor);

            AcademicReferenceGenerator academicReferenceGenerator = new AcademicReferenceGenerator();
            Reference reference = academicReferenceGenerator.generate(doc);

            logger.info(reference.toString());
            Assertions.assertEquals("DANTAS, Alcina. Titulo: Subtitulo da dissertação. 2023. Dissertação (Mestrado em Literatura) - Faculdade de letras, Universidade Estadual de Feira de Santana, Feira de Santana, out. 2009. p. 3-7.", reference.toString().trim());
        }
    }

    private static Documento createBaseDocument(Entity author) {
        Documento doc = new Documento();
        SubClasse testSubClass = new SubClasse("01a", "test_sub_class", "Test sub", new ArrayList<>());
        doc.setClasseProducao(new Classe("01", "test_class", "Test Class", Collections.singletonList(testSubClass)));
        doc.setSubClasseProducao(testSubClass);
        doc.setTitulo("Titulo");
        doc.setSubtitulo("Subtitulo da dissertação");
        doc.setInstituicaoCustodia("Faculdade de letras, Universidade Estadual de Feira de Santana");
        doc.setLugarPublicacao("5");
        doc.setAnoRevista("22");

        doc.setAutores(Collections.singletonList(author.getId()));

        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setMes(10);
        dataDocumento.setAno("2009");
        dataDocumento.setDataIncerta(false);
        doc.setDataDocumento(dataDocumento);

        InfoAdicionais infoAdicionais = new InfoAdicionais();
        infoAdicionais.setAnoSubmissao("2023");
        infoAdicionais.setTipoTrabalho("Dissertação");
        infoAdicionais.setGrau("Mestrado");
        infoAdicionais.setCurso("Literatura");
        doc.setInfoAdicionais(infoAdicionais);

        doc.setAnoRevista("3");
        doc.setVolume("9");
        doc.setEditora("Editora falsa");
        doc.setPaginaInicio(3);
        doc.setNumPagina(5);
        doc.setNumPublicacao(953);
        return doc;
    }
}