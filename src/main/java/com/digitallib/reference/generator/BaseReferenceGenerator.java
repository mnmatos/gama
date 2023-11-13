package com.digitallib.reference.generator;

import com.digitallib.model.Documento;
import com.digitallib.reference.Reference;
import com.digitallib.reference.block.ReferenceBlock;
import com.digitallib.reference.block.ReferenceBlockBuilder;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseReferenceGenerator implements ReferenceGenerator {

    protected abstract List<ReferenceBlockBuilder> getReferenceBuildingBlock();

    @Override
    public Reference generate(Documento documento) {
        List<ReferenceBlock> referenceBlocks = new ArrayList<>();
        for (ReferenceBlockBuilder builder : getReferenceBuildingBlock()) {
            referenceBlocks.add(builder.build(documento));
        }
        return new Reference(referenceBlocks);
    }
}
