package com.digitallib.reference.block;

import com.digitallib.model.DataDocumento;
import com.digitallib.model.Documento;

import static com.digitallib.utils.ReferenceUtils.getExtendedMonth;

public class DataPubliReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public DataPubliReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        DataDocumento data = doc.getDataDocumento();
        if(data == null || data.getAno() == null) return "";
        if(data.isDataIncerta()){
            return data.getAno();
        }
        StringBuilder stringBuilder = new StringBuilder();

        if(data.getMes() != null && (data.getMes() > 0 && data.getMes() < 13)) {

            if(data.getDia() != null && data.getDia() > 0 && data.getDia() < 31) {
                stringBuilder.append(data.getDia());
                stringBuilder.append(" ");
            }

            stringBuilder.append(getExtendedMonth(data.getMes()));
            stringBuilder.append(" ");
        }
        stringBuilder.append(data.getAno());
        return stringBuilder.toString();
    }
}
