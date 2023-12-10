package com.digitallib.reference.generator;

import com.digitallib.model.Documento;
import com.digitallib.reference.block.*;

import java.util.ArrayList;
import java.util.List;

//Documento iconográfico
public class ImageReferenceGenerator extends BaseReferenceGenerator  {

    @Override
    protected List<ReferenceBlockBuilder> getReferenceBuildingBlock() {
        List<ReferenceBlockBuilder> referenceBlockBuilders = new ArrayList<>();
        referenceBlockBuilders.add(new AutorReferenceBlockBuilder()); //Autor
        referenceBlockBuilders.add(new TitleReferenceBlockBuilder(true, false, "."));  //Título
        referenceBlockBuilders.add(new DataPubliReferenceBlockBuilder(false, false, ".")); //Data
        referenceBlockBuilders.add(new PhysicalDescriptionReferenceBlockBuilder(false, false, ".")); //Descrição


        referenceBlockBuilders.add(new ExtraInfoReferenceBlockBuilder(false, false, ".")); // Info extra
        referenceBlockBuilders.add(new OnlineReferenceBlockBuilder(false, false, ".")); // Link
        return referenceBlockBuilders;
    }
}
