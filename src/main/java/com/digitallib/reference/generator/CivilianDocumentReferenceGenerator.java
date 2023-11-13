package com.digitallib.reference.generator;

import com.digitallib.model.Documento;
import com.digitallib.reference.block.ReferenceBlockBuilder;

import java.util.List;

//Documentos civis e de cartórios
public class CivilianDocumentReferenceGenerator extends BaseReferenceGenerator {

    @Override
    protected List<ReferenceBlockBuilder> getReferenceBuildingBlock() {
        return null;
    }
}
