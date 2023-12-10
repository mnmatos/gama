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

import static com.digitallib.model.SubClasseProducao.ENSAIO;

class ImageReferenceGeneratorTest {
    private Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Documento doc = createBaseDocument();

        ImageReferenceGenerator imageReferenceGenerator = new ImageReferenceGenerator();
        Reference reference = imageReferenceGenerator.generate(doc);

        logger.info(reference.toString());
        Assertions.assertEquals("FOTO da Autora com alunas. 2009. 1 Fotografia. Disponível em: https://sitefalso.local. Acesso em: 12 maio. 2023. ", reference.toString());
    }

    private static Documento createBaseDocument() {
        Documento doc = new Documento();
        doc.setClasseProducao(ClasseProducao.PRODUCAO_INTELECTUAL);
        doc.setSubClasseProducao(ENSAIO);
        doc.setTitulo("Foto da Autora com alunas");
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
        infoAdicionais.setDescricaoFisica("1 Fotografia");
        doc.setInfoAdicionais(infoAdicionais);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
        LocalDate date = LocalDate.parse("2023 05 12", formatter);

        doc.setDisponivelEm("https://sitefalso.local");
        doc.setAcessoEm(date);
        return doc;
    }
}