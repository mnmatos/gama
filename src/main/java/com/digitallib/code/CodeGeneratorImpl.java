package com.digitallib.code;

import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;
import com.digitallib.utils.ConfigReader;

import static com.digitallib.main.ACERVO;
import static com.digitallib.utils.TextUtils.getAcronimo;

public class CodeGeneratorImpl implements CodeGenerator{
    @Override
    public String generateCode(Documento documento) {
        String codigo = generateCodeWithoutAppendix(documento);
        if (RepositoryManager.getDocCodeSet().contains(codigo)){
            for (int i = 1; i < 1000; i++){
                if(!RepositoryManager.getDocCodeSet().contains(codigo+String.format(".%03d", i))){
                    codigo += String.format(".%03d", i);
                    break;
                }
            }
        }
        documento.setCodigo(codigo);
        return codigo;
    }

    @Override
    public String generateCodeWithoutAppendix(Documento documento) {
        StringBuilder codigoBuilder = new StringBuilder();
        String acervo = ConfigReader.getProperty(ACERVO);
        codigoBuilder.append(acervo+"."+documento.getSubClasseProducao().getCode()).append(".");
        switch (documento.getClasseProducao()) {
            case PRODUCAO_INTELECTUAL:
            case MEMORABILIA:
            case RECEPCAO:
            case VIDA:
            case PUBLICACOES_IMPRENSA:
                codigoBuilder.append(getAcronimo(documento.getTitulo(), "ST")).append(".");
                codigoBuilder.append(getDateForCode(documento)).append(".");
                codigoBuilder.append(getAcronimo(documento.getEncontradoEm(), "SL")).append(".");
                codigoBuilder.append(getAcronimo(documento.getInstituicaoCustodia(), "SL"));
                break;
            case DOCUMENTOS_AUDIOVISUAIS:
                codigoBuilder.append(getAcronimo(documento.getTitulo(), "ST")).append(".");
                codigoBuilder.append(getDateForCode(documento)).append(".");
                codigoBuilder.append(getAcronimo(documento.getInstituicaoCustodia(), "SL"));
                break;
            case ESBOCOS_NOTAS:
                codigoBuilder.append(getAcronimo(documento.getTitulo(), "ST"));
                break;
            case CORRESPONDENCIA:
                codigoBuilder.append(getAcronimo(documento.getTitulo(), "ST")).append(".");
                codigoBuilder.append(getDateForCode(documento)).append(".");
                codigoBuilder.append(getAcronimo(documento.getEncontradoEm(), "SL"));
                break;
        }
        return codigoBuilder.toString();
    }

    protected String getDateForCode(Documento documento) {
        if(documento.getDataDocumento() != null && documento.getDataDocumento().getAno()!=null && !documento.getDataDocumento().isDataIncerta()) {
            return String.valueOf(documento.getDataDocumento().getAno()).substring(2);
        } else {
            return "00";
        }
    }
}
