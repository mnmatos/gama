package com.digitallib.reference.block;

import com.digitallib.model.Documento;
import com.digitallib.utils.ReferenceUtils;

import java.time.LocalDate;

public class OnlineReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public OnlineReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        String link = doc.getDisponivelEm();
        LocalDate access = doc.getAcessoEm();
        if(link == null || link.isEmpty()) return "";
        else {
            if(access == null) {
                return String.format("Disponível em: %s.", link);
            }
            else {
                return String.format("Disponível em: %s. Acesso em: %02d %s %d", link, access.getDayOfMonth(), ReferenceUtils.getExtendedMonth(access.getMonthValue()), access.getYear());
            }
        }
    }
}
