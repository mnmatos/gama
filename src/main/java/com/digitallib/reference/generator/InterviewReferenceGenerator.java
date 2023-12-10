package com.digitallib.reference.generator;

import com.digitallib.model.Documento;
import com.digitallib.reference.block.*;

import java.util.ArrayList;
import java.util.List;

//Documentos civis e de cartórios
public class InterviewReferenceGenerator extends BaseReferenceGenerator {

    @Override
    protected List<ReferenceBlockBuilder> getReferenceBuildingBlock() {
        List<ReferenceBlockBuilder> referenceBlockBuilders = new ArrayList<>();
        referenceBlockBuilders.add(new AutorReferenceBlockBuilder()); //Autor
        referenceBlockBuilders.add(new TitleReferenceBlockBuilder(false, false, "."));  //Título
        referenceBlockBuilders.add(new InterviewerReferenceBlockBuilder(false, false, "."));  //Entrevistador
        referenceBlockBuilders.add(new EncontradoEmReferenceBlockBuilder(true, false, ",")); //Encontrado em
        referenceBlockBuilders.add(new LocalReferenceBlockBuilder(false, false, ",")); //Local
        referenceBlockBuilders.add(new AnoPubliReferenceBlockBuilder(false, false, ",")); //Ano publi
        referenceBlockBuilders.add(new NumPubliReferenceBlockBuilder(false, false, ",")); //Num publi
        referenceBlockBuilders.add(new DataPubliReferenceBlockBuilder(false, false, ".")); //Data
        referenceBlockBuilders.add(new SinglePageReferenceBlockBuilder(false, false, ".")); //Pág
        referenceBlockBuilders.add(new ExtraInfoReferenceBlockBuilder(false, false, ".")); // Info extra
        referenceBlockBuilders.add(new OnlineReferenceBlockBuilder(false, false, ".")); // Link

        return referenceBlockBuilders;
    }
}
