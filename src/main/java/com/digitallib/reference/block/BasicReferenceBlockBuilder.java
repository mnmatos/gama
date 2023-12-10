package com.digitallib.reference.block;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.Documento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BasicReferenceBlockBuilder implements ReferenceBlockBuilder {

    protected boolean bold = false;
    protected boolean italic = false;
    protected String separator = ".";

    public BasicReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
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
            if(getContent(doc).endsWith(separator)) {
                finalContent = String.format("%s ", getContent(doc));
            }
            else {
                if(content.startsWith("[") && content.endsWith("]")){ // When value between [] remove italic form the sides
                    List<ReferenceBlock> referenceBlocks = new ArrayList<>();
                    referenceBlocks.add(new ReferenceBlock("[", bold, false));
                    referenceBlocks.add(new ReferenceBlock(content.substring(1, content.length()-1), bold, italic));
                    referenceBlocks.add(new ReferenceBlock("]", bold, false));
                    referenceBlocks.add(new ReferenceBlock(String.format("%s ", separator), false, false));
                    return referenceBlocks;
                } else {
                    List<ReferenceBlock> referenceBlocks = new ArrayList<>();
                    referenceBlocks.add(new ReferenceBlock(getContent(doc),  bold, italic));
                    referenceBlocks.add(new ReferenceBlock(String.format("%s ", separator), false, false));
                    return referenceBlocks;
                }
            }
        }
        return Collections.singletonList(new ReferenceBlock(finalContent, bold, italic));
    }

    protected abstract String getContent(Documento doc) throws ReferenceBlockBuilderException;
}
