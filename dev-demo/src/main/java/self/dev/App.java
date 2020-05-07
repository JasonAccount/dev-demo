package self.dev;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import self.dev.common.model.ExcelModel;
import self.dev.common.util.ExcelHelp;

import java.util.*;

/**
 * Hello world!
 *
 */
public class App {
    public static Set<Integer> sets = new HashSet<>();

    public static void main( String[] args )
    {
        /*for(int i=0; i<3; i++){
            System.out.println(sets.add(Integer.valueOf("123")));
        }*/


        /*System.out.println((byte) (Integer.parseInt("127") & 0xff));*/

        String[] title = {
                "姓名",
                "证件号",
                "序号",
                "模型分值",
                "类别",
                "最早一次登记时间",
                "最近一次登记时间"
        };

        HSSFWorkbook workbook = new HSSFWorkbook();
        // Sheet页
        HSSFSheet sheet = workbook.createSheet();
        workbook.setSheetName(0, "ZhaoLian");
        // 表头
        HSSFRow titleRow = sheet.createRow(0);
        for(int i=0; i<title.length; i++){
            HSSFCell titleCell = titleRow.createCell(i);
            titleCell.setCellValue(title[i]);
        }
        // 数据
        List<ExcelModel> read = ExcelHelp.read("C:\\Users\\pc\\Desktop\\zhaolian20200113.xlsx", 0);
        int startRow = 1;
        for (ExcelModel excelModel : read) {
            HSSFRow bodyRow = sheet.createRow(startRow);

            String xm = excelModel.getXm();
            HSSFCell bodyCell_0 = bodyRow.createCell(0);
            bodyCell_0.setCellValue(xm);
            String idCard = excelModel.getIdCard();
            HSSFCell bodyCell_1 = bodyRow.createCell(1);
            bodyCell_1.setCellValue(idCard);
            String xuhao = excelModel.getXuhao();
            HSSFCell bodyCell_2 = bodyRow.createCell(2);
            bodyCell_2.setCellValue(xuhao);
            String score = excelModel.getScore();
            HSSFCell bodyCell_3 = bodyRow.createCell(3);
            bodyCell_3.setCellValue(score);

            int incre = 0;
            if(StringUtils.isNotBlank(score)){
                Map<String, String[]> types = getType(score);
                Set<String> keySet = types.keySet();
                int start = startRow;
                int temp = keySet.size();
                for (String type : keySet) {
                    if(start == startRow){
                        HSSFCell bodyCell_4 = bodyRow.createCell(4);
                        bodyCell_4.setCellValue(type);

                        String[] dates = types.get(type);
                        HSSFCell bodyCell_5 = bodyRow.createCell(5);
                        bodyCell_5.setCellValue(dates[0]);
                        HSSFCell bodyCell_6 = bodyRow.createCell(6);
                        bodyCell_6.setCellValue(dates[1]);
                    }else{
                        HSSFRow newRow = sheet.createRow(start);
                        HSSFCell bodyCell_4 = newRow.createCell(4);
                        bodyCell_4.setCellValue(type);
                        String[] dates = types.get(type);
                        HSSFCell bodyCell_5 = newRow.createCell(5);
                        bodyCell_5.setCellValue(dates[0]);
                        HSSFCell bodyCell_6 = newRow.createCell(6);
                        bodyCell_6.setCellValue(dates[1]);
                    }
                    if((temp = temp - 1) > 0){
                        start++;
                        incre += 1;
                    }
                }
                if(start != startRow){
                    CellRangeAddress region_0 = new CellRangeAddress(startRow, start, 0, 0);
                    sheet.addMergedRegion(region_0);

                    CellRangeAddress region_1 = new CellRangeAddress(startRow, start, 1, 1);
                    sheet.addMergedRegion(region_1);

                    CellRangeAddress region_2 = new CellRangeAddress(startRow, start, 2, 2);
                    sheet.addMergedRegion(region_2);

                    CellRangeAddress region_3 = new CellRangeAddress(startRow, start , 3, 3);
                    sheet.addMergedRegion(region_3);
                }

            }
            startRow += (incre+1);
        }

        ExcelHelp.write2File(workbook, "C:\\Users\\pc\\Desktop\\zl_20200113.xlsx");
    }

    private static Map<String, String[]> getType(String score){
        Map<String, String[]> res = new HashMap<>();;
        String base = "ABCDEFGH";
        String[] type = score.split("\\$");

        for (int i = 0; i < type.length; i++) {
            if(type[i].trim().equals("0")){
                continue;
            }
            String[] split = type[i].split("#");
            String types = base.charAt(i)+split[0];
            String[] dates = new String[2];
            dates[0] = split[1].equals("N") ? "无" : convert2date(split[1]);
            dates[1] = split[2].equals("N") ? "无" : convert2date(split[2]);
            res.put(types, dates);
        }

        return res;
    }

    private static String convert2date(String date){
        String[] split = date.split("~");
        String start = String.valueOf(Integer.parseInt(split[0]) + 2015) ;
        String end = String.valueOf(Integer.parseInt(split[1]) + 2015) ;
        return "["+start+"~"+end+"]";
    }
}
