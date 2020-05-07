package self.dev.common.util;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import self.dev.common.model.ExcelModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelHelp {
    private static final int titleRowNo = 0;

    /**
     * 读取原始数据
     * @param filePath 文件绝对路径
     * @param sheetIndex sheet页索引 0 开始
     * @return
     */
    public static List<ExcelModel> read(String filePath, int sheetIndex){
        List<ExcelModel> result = new ArrayList<>();
        FileInputStream fio = null;
        XSSFWorkbook wb = null;
        try {
            fio = new FileInputStream(filePath);
            wb = new XSSFWorkbook(fio);
            XSSFSheet sheet = wb.getSheetAt(sheetIndex);
            System.out.println("读取sheet页["+sheet.getSheetName()+"]数据, 共"+ sheet.getLastRowNum() +"行");
            for(int i=0; i<=sheet.getLastRowNum(); i++){
                XSSFRow row = sheet.getRow(i);
                if(null == row){
                    continue;
                }
                ExcelModel model = new ExcelModel();

                XSSFCell cell_0 = row.getCell(0);
                if(cell_0 != null){
                    String value_0 = cell_0.getStringCellValue();
                    model.setXm(value_0);
                }
                XSSFCell cell_1 = row.getCell(1);
                if(cell_1 != null){
                    String value_1 = cell_1.getStringCellValue();
                    model.setIdCard(value_1);
                }
                XSSFCell cell_2 = row.getCell(2);
                if(cell_2 != null){
                    Double value_2 = Double.valueOf(cell_2.getNumericCellValue());
                    String s = String.valueOf(value_2);
                    model.setXuhao(s.substring(0, s.indexOf(".")));
                }
                XSSFCell cell_3 = row.getCell(3);
                if(cell_3 != null){
                    String value_3 = cell_3.getStringCellValue();
                    model.setScore(value_3);
                }
                result.add(model);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(wb != null) {
                try {
                    wb.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fio != null) {
                try {
                    fio.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 创建Excel
     * @param title 标题
     * @param body 数据
     * @param sheetName sheet名称
     * @return
     */
    public static HSSFWorkbook createExcel(String[] title, List<String[]> body, String sheetName){
        HSSFWorkbook workbook = new HSSFWorkbook();

        // Sheet页
        HSSFSheet sheet = workbook.createSheet();
        workbook.setSheetName(0, sheetName);
        // 表头
        HSSFRow titleRow = sheet.createRow(titleRowNo);
        for(int i=0; i<title.length; i++){
            HSSFCell titleCell = titleRow.createCell(i);
            titleCell.setCellValue(title[i]);
        }
        // 数据
        for (int k=1; k<=body.size(); k++){
            HSSFRow bodyRow = sheet.createRow(k);
            String[] bodys = body.get(k-1);
            for (int j=0; j<bodys.length; j++) {
                HSSFCell bodyCell = bodyRow.createCell(j);
                String value = bodys[j];
                bodyCell.setCellValue(value);
            }
        }
        return workbook;
    }

    /**
     * 写入文件
     * @param workbook
     * @param filePath
     */
    public static void write2File(HSSFWorkbook workbook, String filePath){
        File file = new File(filePath);
        FileOutputStream fou = null;
        try {
            file.createNewFile();
            fou = FileUtils.openOutputStream(file);
            workbook.write(fou);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fou != null){
                try {
                    fou.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
