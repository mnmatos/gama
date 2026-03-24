package com.digitallib.code;

import com.digitallib.manager.ProjectManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CodeManager {
    static Logger logger = LogManager.getLogger();
    static CodeGenerator codeGenerator = InitializeCodeGenerator();

    private static CodeGenerator InitializeCodeGenerator() {
        String codeType = ProjectManager.getInstance().getCurrentProject() != null ? ProjectManager.getInstance().getCurrentProject().getCode_type() : null;

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
