package com.digitallib.reference.generator;

import com.digitallib.reference.block.AutorReferenceBlockBuilder;
import com.digitallib.reference.block.EncontradoEmReferenceBlockBuilder;
import com.digitallib.reference.block.ReferenceBlockBuilder;
import com.digitallib.reference.block.TitleReferenceBlockBuilder;

import java.util.ArrayList;
import java.util.List;

public class MonographReferenceGenerator extends BaseReferenceGenerator {

    @Override
    protected List<ReferenceBlockBuilder> getReferenceBuildingBlock() {
        List<ReferenceBlockBuilder> referenceBlockBuilders = new ArrayList<>();
        referenceBlockBuilders.add(new AutorReferenceBlockBuilder());
        referenceBlockBuilders.add(new TitleReferenceBlockBuilder(false, false, "."));
        referenceBlockBuilders.add(new EncontradoEmReferenceBlockBuilder(false, false, ","));
        return referenceBlockBuilders;
    }
}
