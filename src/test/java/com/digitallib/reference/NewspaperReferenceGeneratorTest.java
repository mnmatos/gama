package com.digitallib.reference;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.ClasseProducao;
import com.digitallib.model.DataDocumento;
import com.digitallib.model.Documento;
import com.digitallib.reference.generator.NewspaperReferenceGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Collections;

import static com.digitallib.model.SubClasseProducao.ENSAIO;

class NewspaperReferenceGeneratorTest {

    private Logger logger = LogManager.getLogger();

    //{"dia":22,"mes":10,"ano":"1927","dataIncerta":false},"num_publicacao":953,"ano_volume":"22","pagina_inicio":3
    @Test
    void generate() throws ReferenceBlockBuilderException {
        Documento doc = new Documento();
        doc.setClasseProducao(ClasseProducao.PRODUCAO_INTELECTUAL);
        doc.setSubClasseProducao(ENSAIO);
        doc.setTitulo("Direitos femininos");
        doc.setAutores(Collections.singletonList("Alcina Gomes Dantas"));
        doc.setEncontradoEm("Folha do Norte");
        doc.setLugarPublicacao("5");
        doc.setAnoVolume("22");

        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setDia(22);
        dataDocumento.setMes(10);
        dataDocumento.setAno("1927");
        dataDocumento.setDataIncerta(false);
        doc.setDataDocumento(dataDocumento);

        doc.setPaginaInicio(3);
        doc.setNumPublicacao(953);

        NewspaperReferenceGenerator newspaperReferenceGenerator = new NewspaperReferenceGenerator();
        Reference reference = newspaperReferenceGenerator.generate(doc);

        logger.info(reference.toString());
        Assertions.assertEquals("DANTAS, Alcina. Direitos femininos. Folha do Norte, Feira de Santana, ano 22, n. 953, 22 out. 1927, p. 3. ", reference.toString());
    }
}