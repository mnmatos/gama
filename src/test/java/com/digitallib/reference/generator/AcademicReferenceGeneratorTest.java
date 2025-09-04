package com.digitallib.reference.generator;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.*;
import com.digitallib.model.entity.Entity;
import com.digitallib.reference.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static com.digitallib.utils.TestUtils.registerAuthor;

class AcademicReferenceGeneratorTest {

    private Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Documento doc = createBaseDocument();

        AcademicReferenceGenerator academicReferenceGenerator = new AcademicReferenceGenerator();
        Reference reference = academicReferenceGenerator.generate(doc);

        logger.info(reference.toString());
        Assertions.assertEquals("DANTAS, Alcina. Titulo: Subtitulo da dissertação. 2023. Dissertação (Mestrado em Literatura) - Faculdade de letras, Universidade Estadual de Feira de Santana, Feira de Santana, out. 2009. ", reference.toString());

    }

    private static Documento createBaseDocument() {
        Documento doc = new Documento();
        SubClasse testSubClass = new SubClasse("01a", "test_sub_class", "Test sub", new ArrayList<>());
        doc.setClasseProducao(new Classe("01", "test_class", "Test Class", Collections.singletonList(testSubClass)));
        doc.setSubClasseProducao(testSubClass);
        doc.setTitulo("Titulo");
        doc.setSubtitulo("Subtitulo da dissertação");
        doc.setInstituicaoCustodia("Faculdade de letras, Universidade Estadual de Feira de Santana");
        doc.setLugarPublicacao("5");
        doc.setAno("22");

        Entity author = registerAuthor("Alcina Dantas");
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

        doc.setAno("3");
        doc.setVolume("9");
        doc.setEditora("Editora falsa");
        doc.setPaginaInicio(3);
        doc.setNumPagina(5);
        doc.setNumPublicacao(953);
        return doc;
    }
}