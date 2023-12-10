package com.digitallib.reference.generator;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.ClasseProducao;
import com.digitallib.model.DataDocumento;
import com.digitallib.model.Documento;
import com.digitallib.model.InfoAdicionais;
import com.digitallib.reference.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static com.digitallib.model.SubClasseProducao.ENSAIO;

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
        doc.setClasseProducao(ClasseProducao.PRODUCAO_INTELECTUAL);
        doc.setSubClasseProducao(ENSAIO);
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