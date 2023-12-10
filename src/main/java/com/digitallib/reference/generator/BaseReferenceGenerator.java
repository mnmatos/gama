package com.digitallib.reference.generator;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.Documento;
import com.digitallib.reference.Reference;
import com.digitallib.reference.block.ReferenceBlock;
import com.digitallib.reference.block.ReferenceBlockBuilder;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseReferenceGenerator implements ReferenceGenerator {

    protected abstract List<ReferenceBlockBuilder> getReferenceBuildingBlock();

    @Override
    public Reference generate(Documento documento) throws ReferenceBlockBuilderException {
        List<ReferenceBlock> referenceBlocks = new ArrayList<>();
        for (ReferenceBlockBuilder builder : getReferenceBuildingBlock()) {
            referenceBlocks.addAll(builder.build(documento));
        }

        ReferenceBlock lastBlock = null;
        for(ReferenceBlock referenceBlock : referenceBlocks) {
            if(!referenceBlock.getContent().isEmpty()) lastBlock = referenceBlock;
        }
        lastBlock.setContent(lastBlock.getContent().trim());
        if(lastBlock != null && !lastBlock.getContent().endsWith(".")){ //Make sure it ends with .
            lastBlock.setContent(lastBlock.getContent().substring(0,lastBlock.getContent().length() - 1)+".");
        }
        return new Reference(referenceBlocks);
    }
}
