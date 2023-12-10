package com.digitallib.reference.generator;

import com.digitallib.reference.block.*;

import java.util.ArrayList;
import java.util.List;

public class AcademicReferenceGenerator extends BaseReferenceGenerator {

    @Override
    protected List<ReferenceBlockBuilder> getReferenceBuildingBlock() {

        List<ReferenceBlockBuilder> referenceBlockBuilders = new ArrayList<>();
        referenceBlockBuilders.add(new AutorReferenceBlockBuilder()); // Autor
        referenceBlockBuilders.add(new TitleReferenceBlockBuilder(true, false, ".")); // Título
        referenceBlockBuilders.add(new SubmissionYearReferenceBlockBuilder(false, false, ".")); // Ano de depósito
        referenceBlockBuilders.add(new AcademicTypeReferenceBlockBuilder(false, false, "")); // Tipo trabalho
        referenceBlockBuilders.add(new DegreeAndCourseReferenceBlockBuilder(false, false, " -")); // Grau e curso
        referenceBlockBuilders.add(new InstitutionReferenceBlockBuilder(false, false, ",")); // Instituição
        referenceBlockBuilders.add(new LocalReferenceBlockBuilder(false, false, ",")); //Local
        referenceBlockBuilders.add(new DataPubliReferenceBlockBuilder(false, false, ".")); //Data
        referenceBlockBuilders.add(new PagesReferenceBlockBuilder(false, false, ".")); //Pág
        referenceBlockBuilders.add(new ExtraInfoReferenceBlockBuilder(false, false, ".")); // Info extra
        referenceBlockBuilders.add(new OnlineReferenceBlockBuilder(false, false, ".")); // Link

        return referenceBlockBuilders;
    }
}
