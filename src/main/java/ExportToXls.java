import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Xiaomi on 29.09.2020.
 */
class ExportToXls {

    private ArrayList<Product> products;

    ExportToXls(ArrayList<Product> products) {
        this.products = products;
    }

    void launchExportProcess() {
        try {
            String pathToFile = "C:/Users/Xiaomi/Desktop/Posudapark_v2/market.xls";
            FileInputStream inputStream = new FileInputStream(new File(pathToFile));
            Workbook workbook = WorkbookFactory.create(inputStream);

            Sheet sheet = workbook.getSheetAt(2);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    sheet.removeRow(row);
                }
            }

            int rowCount = 2;
            for (Product product : this.products) {
                Row row = sheet.createRow(rowCount++);

                Cell idCell = row.createCell(0);
                idCell.setCellValue(product.getId());

                Cell stockStatusCell = row.createCell(1);
                stockStatusCell.setCellValue("В наличии");

                Cell noOrderBuyCell = row.createCell(8);
                noOrderBuyCell.setCellValue("Нельзя");

                Cell productUrlCell = row.createCell(9);
                productUrlCell.setCellValue(product.getProductUrl());

                Cell tradeMarkCell = row.createCell(10);
                tradeMarkCell.setCellValue(product.getTradeMark());

                Cell nameCell = row.createCell(11);
                nameCell.setCellValue(product.getName());

                Cell categoryCell = row.createCell(12);
                categoryCell.setCellValue(product.getCategory());

                Cell priceCell = row.createCell(13);
                priceCell.setCellValue(product.getPrice());

                Cell currencyCell = row.createCell(15);
                currencyCell.setCellValue("RUR");

                Cell imagesCell = row.createCell(16);
                imagesCell.setCellValue(product.getImagesUrl());

                Cell descCell = row.createCell(17);
                descCell.setCellValue(product.getDescription());

                Cell attrCell = row.createCell(18);
                String attrStr = "";
                if (!product.getColor().equals("")) {
                    attrStr += "Цвет|" + product.getColor() + ";";
                }
                if (!product.getMaterial().equals("")) {
                    attrStr += "Материал|" + product.getMaterial() + ";";
                }
                attrCell.setCellValue(attrStr);

                Cell warrantyCell = row.createCell(21);
                warrantyCell.setCellValue("Есть");

                Cell countryCell = row.createCell(22);
                countryCell.setCellValue("Китай");
            }

            inputStream.close();

            FileOutputStream outputStream = new FileOutputStream(pathToFile);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
