package com.digitallib.reference.block;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.Documento;

import java.util.ArrayList;
import java.util.List;

public class TitleReferenceBlockBuilder implements ReferenceBlockBuilder {

    protected boolean bold = false;
    protected boolean italic = false;
    protected String separator = ".";
    boolean shouldUpperCaseWhenNoAuthor = true;

    public TitleReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        this.bold = bold;
        this.italic = italic;
        this.separator = separator;
    }

    public TitleReferenceBlockBuilder(boolean bold, boolean italic, String separator, boolean shouldUpperCaseWhenNoAuthor) {
        this.bold = bold;
        this.italic = italic;
        this.separator = separator;
        this.shouldUpperCaseWhenNoAuthor = shouldUpperCaseWhenNoAuthor;
    }

    @Override
    public List<ReferenceBlock> build(Documento doc) throws ReferenceBlockBuilderException {
        List<ReferenceBlock> blockList = new ArrayList<>();
        if(doc.getSubtitulo() != null && !doc.getSubtitulo().isEmpty()) { //com subtítulo
            blockList.add(new ReferenceBlock(formatTitle(doc.getTitulo(), doc), bold, italic));
            blockList.add(new ReferenceBlock(String.format(": %s%s ", doc.getSubtitulo(), separator), false, false));
        } else {  //sem subtítulo
            blockList.add(new ReferenceBlock(String.format("%s%s ", formatTitle(doc.getTitulo(), doc), separator), bold, italic));
        }
        return blockList;
    }

    private String formatTitle(String titulo, Documento doc) {
        if(shouldUpperCaseWhenNoAuthor && (doc.getAutores() == null || doc.getAutores().size() == 0)) {
            StringBuilder stringBuilder = new StringBuilder();
            String[] brokenTitle = titulo.split(" ");
            for(int i = 0; i< brokenTitle.length; i++){
                stringBuilder.append(i == 0 ? brokenTitle[i].toUpperCase() : brokenTitle[i]);
                if(i < brokenTitle.length-1) stringBuilder.append(" ");
            }
            return stringBuilder.toString();
        } else return titulo;
    }
}
