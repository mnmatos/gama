package com.digitallib.reference.generator;

import com.digitallib.model.Documento;
import com.digitallib.reference.block.*;

import java.util.ArrayList;
import java.util.List;

//Documentos civis e de cartórios
public class CivilianDocumentReferenceGenerator extends BaseReferenceGenerator {

    @Override
    protected List<ReferenceBlockBuilder> getReferenceBuildingBlock() {
        List<ReferenceBlockBuilder> referenceBlockBuilders = new ArrayList<>();
        referenceBlockBuilders.add(new LocalReferenceBlockBuilder(false, false, ".")); //Local/jurisdição
        referenceBlockBuilders.add(new InstitutionReferenceBlockBuilder(false, false, ".")); //instituição
        referenceBlockBuilders.add(new TitleReferenceBlockBuilder(true, false, ".", false)); //titulo
        referenceBlockBuilders.add(new StaticTextReferenceBlockBuilder("Registro em", false, false, ":")); //Registro em
        referenceBlockBuilders.add(new DataPubliReferenceBlockBuilder(false, false, ".")); //Data
        referenceBlockBuilders.add(new ExtraInfoReferenceBlockBuilder(false, false, ".")); // Info extra
        return referenceBlockBuilders;
    }
}
