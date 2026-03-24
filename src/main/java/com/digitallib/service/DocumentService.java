package com.digitallib.service;

import com.digitallib.code.CodeManager;
import com.digitallib.exception.RepositoryException;
import com.digitallib.exception.ValidationException;
import com.digitallib.manager.MultiSourcedDocumentManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;
import com.digitallib.model.MultiSourcedDocument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * Business logic for document persistence operations.
 * <p>
 * Extracted from {@code DocumentCreatorController} so the controller
 * only handles UI wiring while all save/update/delete logic lives here.
 */
public class DocumentService {

    private static final Logger logger = LogManager.getLogger(DocumentService.class);

    private final DocumentValidator validator;

    public DocumentService() {
        this.validator = new DocumentValidator();
    }

    public DocumentService(DocumentValidator validator) {
        this.validator = validator;
    }

    /**
     * Persists a new document together with any attached files.
     * Generates its code automatically unless {@code manualCode} is provided.
     *
     * @param documento   the document to save
     * @param files       files to attach (may be empty)
     * @param manualCode  explicit code override, or {@code null} to auto-generate
     * @return the final code assigned to the document
     * @throws ValidationException if validation fails
     * @throws RepositoryException if persistence fails
     */
    public String createDocument(Documento documento, List<File> files, String manualCode)
            throws ValidationException, RepositoryException {

        validator.validateDocument(documento);

        String code;
        if (manualCode != null && !manualCode.isEmpty()) {
            validator.validateManualCode(manualCode);
            code = manualCode;
        } else {
            code = CodeManager.getCodeGenerator().generateCode(documento);
        }

        documento.setCodigo(code);
        RepositoryManager.addEntry(documento, files);
        return code;
    }

    /**
     * Updates an existing document.
     * If the code has changed the old entry is removed and a new one created,
     * also updating any multi-source group the document belongs to.
     *
     * @param documento      the updated document state
     * @param oldCode        the code the document had before editing
     * @param manualCode     explicit new code override, or {@code null} to auto-generate
     * @throws ValidationException if validation fails
     * @throws RepositoryException if persistence fails
     */
    public void updateDocument(Documento documento, String oldCode, String manualCode)
            throws ValidationException, RepositoryException {

        validator.validateDocument(documento);

        String newCode;
        if (manualCode != null && !manualCode.isEmpty()) {
            validator.validateManualCode(manualCode);
            newCode = manualCode;
        } else {
            newCode = CodeManager.getCodeGenerator().generateCodeWithoutAppendix(documento);
        }

        // Strip appendix (.001) from old code for comparison
        String oldCodeBase = oldCode.matches(".*\\.\\d{3}$")
                ? oldCode.substring(0, oldCode.length() - 4)
                : oldCode;

        if (oldCodeBase.equals(newCode)) {
            // Code unchanged — just overwrite the JSON
            documento.setCodigo(oldCode);
            RepositoryManager.updateEntry(documento);
        } else {
            // Code changed — create new entry, update group, delete old entry
            String finalCode = createDocument(documento, null, manualCode);
            if (documento.getGrupo() != null) {
                updateGroup(documento, oldCode, finalCode);
            }
            RepositoryManager.removeEntry(oldCode);
        }
    }

    /**
     * Removes a document from the repository.
     *
     * @param code the document code
     * @throws RepositoryException if deletion fails
     */
    public void deleteDocument(String code) throws RepositoryException {
        RepositoryManager.removeEntry(code);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void updateGroup(Documento doc, String oldCode, String newCode) throws RepositoryException {
        MultiSourcedDocument group = MultiSourcedDocumentManager.getEntryById(doc.getGrupo());
        if (group != null) {
            group.getDocuments().remove(oldCode);
            group.getDocuments().add(newCode);
            MultiSourcedDocumentManager.updateEntry(group);
        }
    }
}

