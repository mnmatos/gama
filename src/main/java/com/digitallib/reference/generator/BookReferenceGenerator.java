package com.digitallib.reference.generator;

import com.digitallib.reference.block.*;

import java.util.ArrayList;
import java.util.List;

public class BookReferenceGenerator extends BaseReferenceGenerator {

    @Override
    protected List<ReferenceBlockBuilder> getReferenceBuildingBlock() {
        List<ReferenceBlockBuilder> referenceBlockBuilders = new ArrayList<>();
        referenceBlockBuilders.add(new CompositeAuthorTitleReferenceBlockBuilder(".")); // Autor  e Título
        referenceBlockBuilders.add(new EditionReferenceBlockBuilder(false, false, "."));
        referenceBlockBuilders.add(new LocalReferenceBlockBuilder(false, false, ":")); //Local
        referenceBlockBuilders.add(new EditorReferenceBlockBuilder(false, false, ",")); //Editora
        referenceBlockBuilders.add(new DataPubliReferenceBlockBuilder(false, false, ".")); //Data
        referenceBlockBuilders.add(new PagesReferenceBlockBuilder(false, false, ".")); //Pág
        referenceBlockBuilders.add(new ExtraInfoReferenceBlockBuilder(false, false, ".")); // Info extra

        return referenceBlockBuilders;
    }
}
