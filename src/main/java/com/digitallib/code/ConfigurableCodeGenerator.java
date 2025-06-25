package com.digitallib.code;

import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;
import com.digitallib.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.digitallib.utils.TextUtils.getAcronimo;

public class ConfigurableCodeGenerator extends CodeGeneratorImpl {

    private Logger logger = LogManager.getLogger();
    private static final String CODE_FORMAT = "code_format";

    @Override
    public String generateCodeWithoutAppendix(Documento documento) {

        String classe = documento.getClasseProducao().toValue();
        String format = ConfigReader.getProperty(String.format("%s_%s", CODE_FORMAT, classe));
        String[] CodeFields = format.split("\\.");

        StringBuilder codeBuilder = new StringBuilder();
        for (String field : CodeFields) {
            if (codeBuilder.length() > 0 ) codeBuilder.append(".");
            switch (field) {
                case "titulo":
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
