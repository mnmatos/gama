package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class RecipientReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public RecipientReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getInfoAdicionais() == null) return "";
        String recipient = doc.getInfoAdicionais().getDestinatario();
        if(recipient == null || recipient.isEmpty()) return "";
        return String.format("Destinatário: %s", recipient);
    }
}
