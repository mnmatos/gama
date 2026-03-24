package com.digitallib.code;

import com.digitallib.manager.ProjectManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CodeManager {
    static Logger logger = LogManager.getLogger();
    // Lazily initialized – resolved on first use, after a project has been selected.
    private static CodeGenerator codeGenerator = null;

    private static CodeGenerator initializeCodeGenerator() {
        String codeType = ProjectManager.getInstance().getCurrentProject() != null
                ? ProjectManager.getInstance().getCurrentProject().getCodeType()
                : null;

        if (codeType != null && "custom".equals(codeType)) {
            logger.info("Using custom code generation");
            return new ConfigurableCodeGenerator();
        }
        if (codeType == null) {
            logger.warn("No project selected when initializing CodeGenerator; defaulting to CodeGeneratorImpl.");
        }
        return new CodeGeneratorImpl();
    }

    /** Call this after switching projects so the generator is re-resolved for the new project. */
    public static void reload() {
        codeGenerator = null;
    }

    public static CodeGenerator getCodeGenerator() {
        if (codeGenerator == null) {
            codeGenerator = initializeCodeGenerator();
        }
        return codeGenerator;
    }
}
