package com.digitallib.code;

import com.digitallib.exception.ValidationException;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;
import com.digitallib.utils.ConfigReader;

import java.util.HashSet;

import static com.digitallib.main.ACERVO;
import static com.digitallib.utils.TextUtils.getAcronimo;

public class CodeGeneratorImpl implements CodeGenerator{
    @Override
    public String generateCode(Documento documento) throws ValidationException {
        String codigo = generateCodeWithoutAppendix(documento);
        codigo = getCodeAfterDuplicationCheck(codigo, RepositoryManager.getDocCodeSet());
        documento.setCodigo(codigo);
        return codigo;
    }

    static String getCodeAfterDuplicationCheck(String codigo, HashSet<String> docCodeSet) {
        if (docCodeSet.contains(codigo)){
            for (int i = 1; i < 1000; i++){
                if(!docCodeSet.contains(codigo +String.format(".%03d", i))){
                    codigo += String.format(".%03d", i);
                    break;
                }
            }
        }
        return codigo;
    }

    @Override
    public String generateCodeWithoutAppendix(Documento documento) throws ValidationException {
        StringBuilder codigoBuilder = new StringBuilder();
        String acervo = ConfigReader.getProperty(ACERVO);
        codigoBuilder.append(acervo+"."+documento.getSubClasseProducao().getCode()).append(".");
        switch (documento.getClasseProducao().getName()) {
            case "producao_intelectual":
            case "memorabilia":
            case "recepcao":
            case "varia":
            case "publicacoes_imprensa":
                codigoBuilder.append(getAcronimo(documento.getTitulo(), "ST")).append(".");
                codigoBuilder.append(getDateForCode(documento)).append(".");
                codigoBuilder.append(getAcronimo(documento.getEncontradoEm(), "SL")).append(".");
                codigoBuilder.append(getAcronimo(documento.getInstituicaoCustodia(), "SL"));
                break;
            case "documentos_audiovisuais":
                codigoBuilder.append(getAcronimo(documento.getTitulo(), "ST")).append(".");
                codigoBuilder.append(getDateForCode(documento)).append(".");
                codigoBuilder.append(getAcronimo(documento.getInstituicaoCustodia(), "SL"));
                break;
            case "esbocos_e_notas":
                codigoBuilder.append(getAcronimo(documento.getTitulo(), "ST"));
                break;
            case "correspondencia":
                codigoBuilder.append(getAcronimo(documento.getTitulo(), "ST")).append(".");
                codigoBuilder.append(getDateForCode(documento)).append(".");
                codigoBuilder.append(getAcronimo(documento.getEncontradoEm(), "SL"));
                break;
        }
        return codigoBuilder.toString();
    }

    protected String getDateForCode(Documento documento) throws ValidationException {
        if(documento.getDataDocumento().isDataIncerta()){
            return "00";
        }
        else if(documento.getDataDocumento() != null && !documento.getDataDocumento().getAno().isEmpty()) {
            return String.valueOf(documento.getDataDocumento().getAno()).substring(2);
        } else {
            throw new ValidationException("Data é obrigatório!");
        }
    }
}
