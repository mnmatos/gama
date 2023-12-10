package com.digitallib.reference.generator;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.ClasseProducao;
import com.digitallib.model.DataDocumento;
import com.digitallib.model.Documento;
import com.digitallib.model.entity.Entity;
import com.digitallib.reference.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.digitallib.model.SubClasseProducao.ENSAIO;
import static com.digitallib.utils.TestUtils.registerAuthor;

class BookReferenceGeneratorTest {

    private Logger logger = LogManager.getLogger();

    @Test
    void generate() throws ReferenceBlockBuilderException {
        Documento doc = createBaseDocument();

        BookReferenceGenerator bookReferenceGenerator = new BookReferenceGenerator();
        Reference reference = bookReferenceGenerator.generate(doc);

        logger.info(reference.toString());
        Assertions.assertEquals("DANTAS, Alcina. Livro falso. 4. ed. Feira de Santana: Editora falsa, 1927. ", reference.toString());

    }

    private static Documento createBaseDocument() {
        Documento doc = new Documento();
        doc.setClasseProducao(ClasseProducao.PRODUCAO_INTELECTUAL);
        doc.setSubClasseProducao(ENSAIO);
        doc.setTitulo("Livro falso");
        doc.setEncontradoEm("Folha do Norte");
        doc.setLugarPublicacao("5");
        doc.setAno("22");

        Entity author = registerAuthor("Alcina Dantas");
        doc.setAutores(Collections.singletonList(author.getId()));


        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setAno("1927");
        dataDocumento.setDataIncerta(false);
        doc.setDataDocumento(dataDocumento);

        doc.setEdicao(4);
        doc.setEditora("Editora falsa");
        doc.setPaginaInicio(3);
        doc.setNumPublicacao(953);
        return doc;
    }
}