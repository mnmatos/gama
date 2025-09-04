package com.digitallib.reference.generator;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.Classe;
import com.digitallib.model.DataDocumento;
import com.digitallib.model.Documento;
import com.digitallib.model.SubClasse;
import com.digitallib.reference.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;


class CivilianDocumentReferenceGeneratorTest {
    private Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Documento doc = createBaseDocument();

        CivilianDocumentReferenceGenerator civilianDocumentReferenceGenerator = new CivilianDocumentReferenceGenerator();
        Reference reference = civilianDocumentReferenceGenerator.generate(doc);

        logger.info(reference.toString());
        Assertions.assertEquals("Feira de Santana. Cartório de Registro Civil tal de Feira de Santana. Certidão de Nascimento [de] tal pessoa. Registro em: 21 ago. 1979. ", reference.toString());

    }

    private static Documento createBaseDocument() {
        Documento doc = new Documento();
        SubClasse testSubClass = new SubClasse("01a", "test_sub_class", "Test sub", new ArrayList<>());
        doc.setClasseProducao(new Classe("01", "test_class", "Test Class", Collections.singletonList(testSubClass)));
        doc.setSubClasseProducao(testSubClass);
        doc.setTitulo("Certidão de Nascimento [de] tal pessoa");
        doc.setInstituicaoCustodia("Cartório de Registro Civil tal de Feira de Santana");
        doc.setLugarPublicacao("5");
        doc.setAno("22");

        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setDia(21);
        dataDocumento.setMes(8);
        dataDocumento.setAno("1979");
        dataDocumento.setDataIncerta(false);
        doc.setDataDocumento(dataDocumento);

        doc.setEdicao(4);
        doc.setEditora("Editora falsa");
        doc.setPaginaInicio(3);
        doc.setNumPublicacao(953);
        return doc;
    }
}