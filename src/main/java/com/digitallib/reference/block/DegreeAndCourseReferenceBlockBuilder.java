package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class DegreeAndCourseReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public DegreeAndCourseReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getInfoAdicionais() == null) return "";
        String degree = doc.getInfoAdicionais().getGrau();
        String course = doc.getInfoAdicionais().getCurso();
        if(degree == null || degree.isEmpty()) return "";
        else {
            if(course == null || course.isEmpty()) {
                return String.format("(%s)", degree);
            }
            else {
                return String.format("(%s em %s)", degree, course);
            }
        }
    }
}
