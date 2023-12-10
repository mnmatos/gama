package com.digitallib.reference.generator;

import com.digitallib.reference.block.*;

import java.util.ArrayList;
import java.util.List;

public class OnlineVideoReferenceGenerator extends BaseReferenceGenerator {

    @Override
    protected List<ReferenceBlockBuilder> getReferenceBuildingBlock() {
        List<ReferenceBlockBuilder> referenceBlockBuilders = new ArrayList<>();
        referenceBlockBuilders.add(new TitleReferenceBlockBuilder(false, false, ".")); //titulo
        referenceBlockBuilders.add(new StaticTextReferenceBlockBuilder("[S.L.: s.n]", false, true, ","));
        referenceBlockBuilders.add(new DataPubliReferenceBlockBuilder(false, false, ".")); //data
        referenceBlockBuilders.add(new PhysicalDescriptionReferenceBlockBuilder(false, false, ".")); //Descrição
        referenceBlockBuilders.add(new PublisherReferenceBlockBuilder(false, false, ".")); //Publicado por
        referenceBlockBuilders.add(new ExtraInfoReferenceBlockBuilder(false, false, ".")); // Info extra
        referenceBlockBuilders.add(new OnlineReferenceBlockBuilder(false, false, ".")); // Link

        return referenceBlockBuilders;
    }
}
