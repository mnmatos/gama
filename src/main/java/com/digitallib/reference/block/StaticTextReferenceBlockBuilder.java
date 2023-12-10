package com.digitallib.reference.block;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.Documento;

import java.util.List;

public class StaticTextReferenceBlockBuilder extends BasicReferenceBlockBuilder {
    private final String content;

    public StaticTextReferenceBlockBuilder(String content, boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
        this.content = content;
    }

    @Override
    protected String getContent(Documento doc) throws ReferenceBlockBuilderException {
        return content;
    }
}
