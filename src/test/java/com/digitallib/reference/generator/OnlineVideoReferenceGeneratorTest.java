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

class OnlineVideoReferenceGeneratorTest {
    private Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Documento doc = createBaseDocument();

        OnlineVideoReferenceGenerator videoReferenceGenerator = new OnlineVideoReferenceGenerator();
        Reference reference = videoReferenceGenerator.generate(doc);

        logger.info(reference.toString());
        Assertions.assertEquals("TITULO do vídeo. [S.L.: s.n], 2009. 1 Vídeo (4 min). Publicado pelo canal Canal x. Disponível em: https://sitefalso.local. Acesso em: 12 maio. 2023. ", reference.toString());
    }

    private static Documento createBaseDocument() {
        Documento doc = new Documento();
        SubClasse testSubClass = new SubClasse("01a", "test_sub_class", "Test sub", new ArrayList<>());
        doc.setClasseProducao(new Classe("01", "test_class", "Test Class", Collections.singletonList(testSubClass)));
        doc.setSubClasseProducao(testSubClass);
        doc.setTitulo("Titulo do vídeo");
        doc.setLugarPublicacao("5");
        doc.setAno("22");

        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setAno("2009");
        dataDocumento.setDataIncerta(false);
        doc.setDataDocumento(dataDocumento);

        doc.setAno("3");
        doc.setVolume("9");
        doc.setEditora("Editora falsa");
        doc.setPaginaInicio(3);
        doc.setNumPagina(5);
        doc.setNumPublicacao(953);

        InfoAdicionais infoAdicionais = new InfoAdicionais();
        infoAdicionais.setPublicadoPor("Canal x");
        infoAdicionais.setDescricaoFisica("1 Vídeo (4 min)");
        doc.setInfoAdicionais(infoAdicionais);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
        LocalDate date = LocalDate.parse("2023 05 12", formatter);

        doc.setDisponivelEm("https://sitefalso.local");
        doc.setAcessoEm(date);
        return doc;
    }
}