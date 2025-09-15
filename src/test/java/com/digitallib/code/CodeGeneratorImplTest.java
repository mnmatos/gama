package com.digitallib.code;

import com.digitallib.exception.ValidationException;
import com.digitallib.model.Classe;
import com.digitallib.model.DataDocumento;
import com.digitallib.model.Documento;
import com.digitallib.model.SubClasse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorImplTest {

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
    void generateCodeWithoutAppendix() throws ValidationException {
        Documento documento = getBasicDocumento();

        CodeGeneratorImpl codeGenerator = new CodeGeneratorImpl();
        String codigo = codeGenerator.generateCodeWithoutAppendix(documento);
        assertEquals("AAD.01a.TT.15.TI.SL", codigo);
    }

    @Test
    void generateCodeWithoutAppendixAudioVisual() throws ValidationException {
        Documento documento = getBasicDocumento();
        SubClasse testSubClass = new SubClasse("01a", "sub", "Test sub", new ArrayList<>());
        documento.setClasseProducao(new Classe("01", "documentos_audiovisuais", "Test Class", Collections.singletonList(testSubClass)));

        CodeGeneratorImpl codeGenerator = new CodeGeneratorImpl();
        String codigo = codeGenerator.generateCodeWithoutAppendix(documento);
        assertEquals("AAD.01a.TT.15.SL", codigo);
    }

    @Test
    void generateCodeWithoutAppendixEsboco() throws ValidationException {
        Documento documento = getBasicDocumento();
        SubClasse testSubClass = new SubClasse("01a", "sub", "Test sub", new ArrayList<>());
        documento.setClasseProducao(new Classe("01", "esbocos_e_notas", "Test Class", Collections.singletonList(testSubClass)));

        CodeGeneratorImpl codeGenerator = new CodeGeneratorImpl();
        String codigo = codeGenerator.generateCodeWithoutAppendix(documento);
        assertEquals("AAD.01a.TT", codigo);
    }

    @Test
    void generateCodeWithoutAppendixCorrespondencia() throws ValidationException {
        Documento documento = getBasicDocumento();
        SubClasse testSubClass = new SubClasse("01a", "sub", "Test sub", new ArrayList<>());
        documento.setClasseProducao(new Classe("01", "correspondencia", "Test Class", Collections.singletonList(testSubClass)));

        CodeGeneratorImpl codeGenerator = new CodeGeneratorImpl();
        String codigo = codeGenerator.generateCodeWithoutAppendix(documento);
        assertEquals("AAD.01a.TT.15.TI", codigo);
    }

    @Test
    void generateCodeWhenDuplicated() {
        HashSet<String> testSet = new HashSet<>();
        String code = CodeGeneratorImpl.getCodeAfterDuplicationCheck("AAD.01a.TT.15.TI.SL", testSet);
        assertEquals("AAD.01a.TT.15.TI.SL", code);

        testSet.add("AAD.01a.TT.15.TI.SL");
        code = CodeGeneratorImpl.getCodeAfterDuplicationCheck("AAD.01a.TT.15.TI.SL", testSet);
        assertEquals("AAD.01a.TT.15.TI.SL.001", code);


        testSet.add("AAD.01a.TT.15.TI.SL.001");
        code = CodeGeneratorImpl.getCodeAfterDuplicationCheck("AAD.01a.TT.15.TI.SL", testSet);
        assertEquals("AAD.01a.TT.15.TI.SL.002", code);
    }


    @Test
    void getDateForCode() throws ValidationException {
        Documento documento = getBasicDocumento();
        CodeGeneratorImpl codeGenerator = new CodeGeneratorImpl();
        String date = codeGenerator.getDateForCode(documento);
        assertEquals("15", date);
    }

    @Test
    void getDateForCodeUncertainDate() throws ValidationException {
        Documento documento = getBasicDocumento();
        documento.getDataDocumento().setDataIncerta(true);
        CodeGeneratorImpl codeGenerator = new CodeGeneratorImpl();
        String date = codeGenerator.getDateForCode(documento);
        assertEquals("00", date);
    }
}