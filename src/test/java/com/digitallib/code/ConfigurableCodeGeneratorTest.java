package com.digitallib.code;

import com.digitallib.model.Classe;
import com.digitallib.model.DataDocumento;
import com.digitallib.model.Documento;
import com.digitallib.model.SubClasse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurableCodeGeneratorTest {

    private static Documento getBasicDocumento() {
        Documento documento = new Documento();
        SubClasse testSubClass = new SubClasse("01a", "poesia", "Test sub", new ArrayList<>());
        documento.setClasseProducao(new Classe("01", "producao_intelectual", "Test Class", Collections.singletonList(testSubClass)));
        documento.setSubClasseProducao(testSubClass);
        documento.setTitulo("Test Tittle");
        documento.setEncontradoEm("Test Encontrado Em");
        documento.setEncontradoEm("Test Instituicao");
        documento.setDataDocumento(new DataDocumento(5, 5, "2015"));
        return documento;
    }

    @Test
    void generateCodeWithoutAppendix() {
        Documento documento = getBasicDocumento();
        documento.getClasseProducao().setFormat("titulo.data.encontrado_em.instituicao");

        ConfigurableCodeGenerator codeGenerator = new ConfigurableCodeGenerator();
        String codigo = codeGenerator.generateCodeWithoutAppendix(documento);
        assertEquals("AAD.01a.TT.15.TI.SL", codigo);

        documento.getClasseProducao().setFormat("titulo.data.instituicao");
        codigo = codeGenerator.generateCodeWithoutAppendix(documento);
        assertEquals("AAD.01a.TT.15.SL", codigo);

        documento.getClasseProducao().setFormat("titulo");
        codigo = codeGenerator.generateCodeWithoutAppendix(documento);
        assertEquals("AAD.01a.TT", codigo);

        documento.getClasseProducao().setFormat("titulo.data.encontrado_em");
        codigo = codeGenerator.generateCodeWithoutAppendix(documento);
        assertEquals("AAD.01a.TT.15.TI", codigo);
    }
}