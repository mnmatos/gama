package com.digitallib.reference.generator;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.ClasseProducao;
import com.digitallib.model.DataDocumento;
import com.digitallib.model.Documento;
import com.digitallib.reference.Reference;
import com.digitallib.reference.generator.NewspaperReferenceGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Collections;

import static com.digitallib.model.SubClasseProducao.ENSAIO;

class NewspaperReferenceGeneratorTest {

    private Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Documento doc = createBaseDocument();

        NewspaperReferenceGenerator newspaperReferenceGenerator = new NewspaperReferenceGenerator();
        Reference reference = newspaperReferenceGenerator.generate(doc);

        logger.info(reference.toString());
        Assertions.assertEquals("DIREITOS femininos. Folha do Norte, Feira de Santana, ano 22, n. 953, 22 out. 1927, p. 3. ", reference.toString());
    }

    private static Documento createBaseDocument() {
        Documento doc = new Documento();
        doc.setClasseProducao(ClasseProducao.PRODUCAO_INTELECTUAL);
        doc.setSubClasseProducao(ENSAIO);
        doc.setTitulo("Direitos femininos");
        doc.setEncontradoEm("Folha do Norte");
        doc.setLugarPublicacao("5");
        doc.setAno("22");

        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setDia(22);
        dataDocumento.setMes(10);
        dataDocumento.setAno("1927");
        dataDocumento.setDataIncerta(false);
        doc.setDataDocumento(dataDocumento);

        doc.setPaginaInicio(3);
        doc.setNumPublicacao(953);

        return doc;
    }
}