package com.digitallib.code;

import com.digitallib.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CodeManager {
    static Logger logger = LogManager.getLogger();
    static CodeGenerator codeGenerator = InitializeCodeGenerator();

    private static CodeGenerator InitializeCodeGenerator() {
        String code_type = "code_type";
        String codeType = ConfigReader.getProperty(code_type);

        if(codeType != null && "custom".equals(codeType)){
            logger.info("Using custom code generation");
            return new ConfigurableCodeGenerator();
        }
        return new CodeGeneratorImpl();
    }

    public static CodeGenerator getCodeGenerator () {
        return codeGenerator;
    }
}
