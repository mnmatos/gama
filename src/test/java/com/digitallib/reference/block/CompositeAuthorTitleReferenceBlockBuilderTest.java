package com.digitallib.reference.block;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.digitallib.model.SubClasseProducao.ENSAIO;
import static com.digitallib.utils.TestUtils.registerAuthor;
import static org.junit.jupiter.api.Assertions.*;

class CompositeAuthorTitleReferenceBlockBuilderTest {
    private Logger logger = LogManager.getLogger();

    @Test
    void build() throws ReferenceBlockBuilderException {
        Documento doc = createBaseDocument();

        CompositeAuthorTitleReferenceBlockBuilder compositeAuthorTitleReferenceBlockBuilder = new CompositeAuthorTitleReferenceBlockBuilder(".");
        Reference reference = new Reference(compositeAuthorTitleReferenceBlockBuilder.build(doc));
        logger.info(reference.toString());
        Assertions.assertEquals("DANTAS, Alcina. Este é um titulo: Este é um subtitulo. Este é um titulo de publicação: Este é um subtitulo de publicação. ", reference.toString());

    }

    private static Documento createBaseDocument() {
        Documento doc = new Documento();
        doc.setClasseProducao(ClasseProducao.PRODUCAO_INTELECTUAL);
        doc.setSubClasseProducao(ENSAIO);
        doc.setTitulo("Este é um titulo");
        doc.setSubtitulo("Este é um subtitulo");
        doc.setEncontradoEm("Folha do Norte");
        doc.setLugarPublicacao("5");
        doc.setAno("22");

        Entity author = registerAuthor("Alcina Dantas");
        doc.setAutores(Collections.singletonList(author.getId()));


        doc.setTituloPublicacao("Este é um titulo de publicação");
        doc.setSubtituloPublicacao("Este é um subtitulo de publicação");
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