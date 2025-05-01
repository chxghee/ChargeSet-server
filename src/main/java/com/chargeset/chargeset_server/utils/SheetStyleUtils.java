package com.chargeset.chargeset_server.utils;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

@Component
public class SheetStyleUtils {

    public static CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();

        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16); // 글자 크기
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    public static CellStyle createBoldStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    public static CellStyle createPercentStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.createDataFormat().getFormat("0.0%"));
        return style;
    }

    public static CellStyle createMoneyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
        return style;
    }
}
