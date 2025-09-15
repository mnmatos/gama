package com.digitallib.code;

import com.digitallib.exception.ValidationException;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;
import com.digitallib.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.digitallib.main.ACERVO;
import static com.digitallib.utils.TextUtils.getAcronimo;

public class ConfigurableCodeGenerator extends CodeGeneratorImpl {

    private Logger logger = LogManager.getLogger();

    @Override
    public String generateCodeWithoutAppendix(Documento documento) throws ValidationException {

        String format = documento.getClasseProducao().getFormat();
        String[] CodeFields = format.split("\\.");

        StringBuilder codeBuilder = new StringBuilder();
        String acervo = ConfigReader.getProperty(ACERVO);
        codeBuilder.append(acervo+"."+documento.getSubClasseProducao().getCode());
        for (String field : CodeFields) {
            if (codeBuilder.length() > 0 ) codeBuilder.append(".");
            switch (field) {
                case "titulo":
                    if(documento.getTitulo().isEmpty()) throw new ValidationException("Titulo é obrigatório!");
                    codeBuilder.append(getAcronimo(documento.getTitulo(), "ST"));
                    break;
                case "data":
                    codeBuilder.append(getDateForCode(documento));
                    break;
                case "encontrado_em":
                    codeBuilder.append(getAcronimo(documento.getEncontradoEm(), "SL"));
                    break;
                case "instituicao":
                    codeBuilder.append(getAcronimo(documento.getInstituicaoCustodia(), "SL"));
                    break;
                default:
                    logger.error("Error generating code. Unrecognized field on code generation config: "+field+". switching to default");
                    return super.generateCodeWithoutAppendix(documento);
            }
        }

        return codeBuilder.toString();
    }
}
