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

class MailReferenceGeneratorTest {

    private Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Documento doc = createBaseDocument();

        MailReferenceGenerator mailReferenceGenerator = new MailReferenceGenerator();
        Reference reference = mailReferenceGenerator.generate(doc);

        logger.info(reference.toString());
        Assertions.assertEquals("DANTAS, Alcina. [correspondência]. Destinatário: pessoa falsa. Feira de Santana, 22 out. 1927. 1 cartão pessoal. Autografado. ", reference.toString());
    }

    private static Documento createBaseDocument() {
        Documento doc = new Documento();
        SubClasse testSubClass = new SubClasse("01a", "test_sub_class", "Test sub", new ArrayList<>());
        doc.setClasseProducao(new Classe("01", "test_class", "Test Class", Collections.singletonList(testSubClass)));
        doc.setSubClasseProducao(testSubClass);
        doc.setTitulo("[correspondência]");
        doc.setLugarPublicacao("5");
        doc.setAno("22");

        Entity author = registerAuthor("Alcina Dantas");
        doc.setAutores(Collections.singletonList(author.getId()));

        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setDia(22);
        dataDocumento.setMes(10);
        dataDocumento.setAno("1927");
        dataDocumento.setDataIncerta(false);
        doc.setDataDocumento(dataDocumento);

        InfoAdicionais infoAdicionais = new InfoAdicionais();
        infoAdicionais.setDestinatario("pessoa falsa");
        infoAdicionais.setDescricaoFisica("1 cartão pessoal. Autografado.");
        doc.setInfoAdicionais(infoAdicionais);

        doc.setPaginaInicio(3);
        doc.setNumPublicacao(953);
        return doc;
    }
}