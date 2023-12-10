package com.digitallib.reference.block;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.Documento;

import java.util.ArrayList;
import java.util.List;

public class PublicationTitleReferenceBlockBuilder implements ReferenceBlockBuilder {

    protected boolean bold = false;
    protected boolean italic = false;
    protected String separator = ".";

    public PublicationTitleReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        this.bold = bold;
        this.italic = italic;
        this.separator = separator;
    }

    @Override
    public List<ReferenceBlock> build(Documento doc) throws ReferenceBlockBuilderException {
        List<ReferenceBlock> blockList = new ArrayList<>();
        if(doc.getSubtituloPublicacao() != null  && !doc.getSubtituloPublicacao().isEmpty()) { //com subtítulo
            blockList.add(new ReferenceBlock(doc.getTituloPublicacao(), bold, italic));
            blockList.add(new ReferenceBlock(String.format(": %s%s ", doc.getSubtituloPublicacao(), separator), false, false));
        } else {  //sem subtítulo
            blockList.add(new ReferenceBlock(String.format("%s%s ", doc.getTituloPublicacao(), separator), bold, italic));
        }
        return blockList;
    }
}
