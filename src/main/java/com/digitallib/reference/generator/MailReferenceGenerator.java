package com.digitallib.reference.generator;

import com.digitallib.model.Documento;
import com.digitallib.reference.block.*;

import java.util.ArrayList;
import java.util.List;

//Documentos civis e de cartórios
public class MailReferenceGenerator extends BaseReferenceGenerator  {

    @Override
    protected List<ReferenceBlockBuilder> getReferenceBuildingBlock() {
        List<ReferenceBlockBuilder> referenceBlockBuilders = new ArrayList<>();
        referenceBlockBuilders.add(new AutorReferenceBlockBuilder()); //Autor
        referenceBlockBuilders.add(new TitleReferenceBlockBuilder(true, false, "."));  //Título
        referenceBlockBuilders.add(new RecipientReferenceBlockBuilder(false, false, "."));  //Título
        referenceBlockBuilders.add(new LocalReferenceBlockBuilder(false, false, ",")); //Local
        referenceBlockBuilders.add(new DataPubliReferenceBlockBuilder(false, false, ".")); //Ano publi
        referenceBlockBuilders.add(new PhysicalDescriptionReferenceBlockBuilder(false, false, ".")); //Descrição
        referenceBlockBuilders.add(new ExtraInfoReferenceBlockBuilder(false, false, ".")); // Info extra

        return referenceBlockBuilders;
    }

}
