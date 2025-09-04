package com.digitallib.reference.generator;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.*;
import com.digitallib.reference.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

class InterviewReferenceGeneratorTest {

    private Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Documento doc = createBaseDocument();

        InterviewReferenceGenerator interviewReferenceGenerator = new InterviewReferenceGenerator();
        Reference reference = interviewReferenceGenerator.generate(doc);

        logger.info(reference.toString());
        Assertions.assertEquals("GUIMARÃES, Leolindo. Entrevista com pessoa X. [Entrevista cedida a] Pollianna dos Santos Ferreira Silva. Feira de Santana, out. 2021. Disponível em: https://sitefalso.local. Acesso em: 12 maio. 2023. ", reference.toString());
    }

    private static Documento createBaseDocument() {
        Documento doc = new Documento();
        SubClasse testSubClass = new SubClasse("01a", "test_sub_class", "Test sub", new ArrayList<>());
        doc.setClasseProducao(new Classe("01", "test_class", "Test Class", Collections.singletonList(testSubClass)));
        doc.setSubClasseProducao(testSubClass);
        doc.setAutores(Collections.singletonList("1"));
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