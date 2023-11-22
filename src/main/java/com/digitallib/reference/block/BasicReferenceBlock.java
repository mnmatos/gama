package com.digitallib.reference.block;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.Documento;

import java.util.Collections;
import java.util.List;

public abstract class BasicReferenceBlock implements ReferenceBlockBuilder {

    protected boolean bold = false;
    protected boolean italic = false;
    protected String separator = ".";

    public BasicReferenceBlock(boolean bold, boolean italic, String separator) {
        this.bold = bold;
        this.italic = italic;
        this.separator = separator;
    }

    @Override
    public List<ReferenceBlock> build(Documento doc) throws ReferenceBlockBuilderException {
        String content = getContent(doc);
        String finalContent;
        if (content.isEmpty()){
            finalContent = content;
        } else {
            finalContent = String.format("%s%s ", getContent(doc), separator);
        }
        return Collections.singletonList(new ReferenceBlock(finalContent, bold, italic));
    }

    protected abstract String getContent(Documento doc) throws ReferenceBlockBuilderException;
}
