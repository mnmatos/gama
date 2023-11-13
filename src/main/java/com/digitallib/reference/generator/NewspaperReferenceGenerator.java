package com.digitallib.reference.generator;

import com.digitallib.reference.block.*;

import java.util.ArrayList;
import java.util.List;

public class NewspaperReferenceGenerator extends BaseReferenceGenerator {

    @Override
    protected List<ReferenceBlockBuilder> getReferenceBuildingBlock() {
        List<ReferenceBlockBuilder> referenceBlockBuilders = new ArrayList<>();
        referenceBlockBuilders.add(new AutorReferenceBlockBuilder());
        referenceBlockBuilders.add(new TitleReferenceBlockBuilder(false, false, "."));
        referenceBlockBuilders.add(new EncontradoEmReferenceBlockBuilder(true, false, ","));
        referenceBlockBuilders.add(new LocalReferenceBlockBuilder(true, false, ","));
        referenceBlockBuilders.add(new AnoPubliReferenceBlockBuilder(true, false, ","));
        referenceBlockBuilders.add(new NumPubliReferenceBlockBuilder(true, false, ","));
        referenceBlockBuilders.add(new DataPubliReferenceBlockBuilder(true, false, ","));
        referenceBlockBuilders.add(new PagPubliReferenceBlockBuilder(true, false, "."));
        return referenceBlockBuilders;
    }
}
