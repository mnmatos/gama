package com.digitallib.service;

import com.digitallib.exception.ValidationException;
import com.digitallib.manager.ProjectManager;
import com.digitallib.model.Documento;

/**
 * Validates {@link Documento} instances before persistence.
 * <p>
 * Extracted from {@code DocumentCreatorController} to isolate validation
 * rules from UI concerns and make them independently testable.
 */
public class DocumentValidator {

    /**
     * Validates the code entered manually by the user.
     *
     * @param code the code string to validate
     * @throws ValidationException if the code is null, contains spaces, lacks
     *                             the required separator, or does not start
     *                             with the configured acervo prefix.
     */
    public void validateManualCode(String code) throws ValidationException {
        if (code == null || code.isEmpty()) {
            throw new ValidationException("Código inválido: vazio");
        }
        if (code.matches(".*\\s+.*")) {
            throw new ValidationException("Código inválido. Não deve conter espaços.");
        }
        if (!code.contains(".")) {
            throw new ValidationException("Código inválido. Deve conter o separador '.'.");
        }

        String acervoPrefix = ProjectManager.getInstance().getCurrentProject() != null
                ? ProjectManager.getInstance().getCurrentProject().getAcervo()
                : null;

        if (acervoPrefix != null && !acervoPrefix.isEmpty() && !code.startsWith(acervoPrefix)) {
            throw new ValidationException(
                    String.format("Código inválido. Deve começar com o prefixo configurado: %s", acervoPrefix));
        }
    }

    /**
     * Validates the core required fields of a document before saving.
     *
     * @param documento the document to validate
     * @throws ValidationException if mandatory fields are missing
     */
    public void validateDocument(Documento documento) throws ValidationException {
        if (documento.getTitulo() == null || documento.getTitulo().trim().isEmpty()) {
            throw new ValidationException("O título do documento é obrigatório.");
        }
        if (documento.getClasseProducao() == null) {
            throw new ValidationException("A classe de produção é obrigatória.");
        }
        if (documento.getSubClasseProducao() == null) {
            throw new ValidationException("O tipo (subclasse) é obrigatório.");
        }
    }
}

