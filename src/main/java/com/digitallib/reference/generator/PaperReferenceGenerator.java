package com.digitallib.reference.generator;

import com.digitallib.reference.block.*;

import java.util.ArrayList;
import java.util.List;

public class PaperReferenceGenerator extends BaseReferenceGenerator {

    @Override
    protected List<ReferenceBlockBuilder> getReferenceBuildingBlock() {
        List<ReferenceBlockBuilder> referenceBlockBuilders = new ArrayList<>();
        referenceBlockBuilders.add(new CompositeAuthorTitleReferenceBlockBuilder(",")); // Autor  e Título
        referenceBlockBuilders.add(new LocalReferenceBlockBuilder(false, false, ",")); //Local
        referenceBlockBuilders.add(new AnoPubliReferenceBlockBuilder(false, false, ",")); //Ano publi
        referenceBlockBuilders.add(new VolumeReferenceBlockBuilder(false, false, ",")); // Volume
        referenceBlockBuilders.add(new NumPubliReferenceBlockBuilder(false, false, ",")); //Num publi
        referenceBlockBuilders.add(new EditionReferenceBlockBuilder(false, false, ".")); // Edição
        referenceBlockBuilders.add(new PagesReferenceBlockBuilder(false, false, ",")); //Pág
        referenceBlockBuilders.add(new DataPubliReferenceBlockBuilder(false, false, ".")); //Data
        referenceBlockBuilders.add(new DoiReferenceBlockBuilder(false, false, ".")); // DOI
        referenceBlockBuilders.add(new ExtraInfoReferenceBlockBuilder(false, false, ".")); // Info extra
        referenceBlockBuilders.add(new OnlineReferenceBlockBuilder(false, false, ".")); // Link
        return referenceBlockBuilders;
    }
}
