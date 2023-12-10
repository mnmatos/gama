package com.digitallib.reference.block;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.Documento;

import java.util.ArrayList;
import java.util.List;

public class CompositeAuthorTitleReferenceBlockBuilder implements ReferenceBlockBuilder{

    String separator;

    public CompositeAuthorTitleReferenceBlockBuilder(String separator) {
        this.separator = separator;
    }

    @Override
    public List<ReferenceBlock> build(Documento doc) throws ReferenceBlockBuilderException {
        List<ReferenceBlock> blockList = new ArrayList<>();
        if(doc.getAutoresPubli() != null && doc.getAutoresPubli().size() > 0){  //colaboração
            blockList.addAll(new AutorReferenceBlockBuilder().build(doc));
            blockList.addAll(new TitleReferenceBlockBuilder(false, false, ".").build(doc));
            blockList.addAll(new StaticTextReferenceBlockBuilder("In", false, true,":").build(doc));
            blockList.addAll(new AutorPubliReferenceBlockBuilder().build(doc));
            blockList.addAll(new PublicationTitleReferenceBlockBuilder(true, false, separator).build(doc));
        } else { //sem colaboração
            if(doc.getTituloPublicacao() != null && !doc.getTituloPublicacao().isEmpty()) { // Publicação em revista
                blockList.addAll(new AutorReferenceBlockBuilder().build(doc));
                blockList.addAll(new TitleReferenceBlockBuilder(false, false, ".").build(doc)); //Título do artigo
                blockList.addAll(new PublicationTitleReferenceBlockBuilder(true, false, separator).build(doc)); //Título da revista
            } else {
                blockList.addAll(new AutorReferenceBlockBuilder().build(doc));
                blockList.addAll(new TitleReferenceBlockBuilder(true, false, separator).build(doc));
            }
        }

        return blockList;
    }
}
