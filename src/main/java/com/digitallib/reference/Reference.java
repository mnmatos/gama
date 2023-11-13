package com.digitallib.reference;

import com.digitallib.reference.block.ReferenceBlock;
import com.digitallib.reference.block.ReferenceBlockBuilder;

import java.util.List;

public class Reference {
    List<ReferenceBlock> referenceBlocks;

    public Reference(List<ReferenceBlock> referenceBlocks) {
        this.referenceBlocks = referenceBlocks;
    }

    public List<ReferenceBlock> getReferenceBlocks() {
        return referenceBlocks;
    }

    public void setReferenceBlocks(List<ReferenceBlock> referenceBlocks) {
        this.referenceBlocks = referenceBlocks;
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (ReferenceBlock block : referenceBlocks){
            stringBuilder.append(block.getContent());
        }
        return stringBuilder.toString();
    }
}
