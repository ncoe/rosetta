package com.github.ncoe.rosetta.io;

import com.github.ncoe.rosetta.dto.TaskInfo;
import com.github.ncoe.rosetta.exception.UtilException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFPieChartData;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ExcelWriter {
    private ExcelWriter() {
        throw new NotImplementedException("No ExcelWriter for you!");
    }

    private static void writeOpenTasks(Workbook workbook, List<TaskInfo> taskList) {
        Sheet sheet = workbook.createSheet("In Progress");

        int rowNum = 0;
        int colNum = 0;
        Cell cell;

        Row header = sheet.createRow(rowNum++);

        cell = header.createCell(colNum++);
        cell.setCellValue("Category");

        cell = header.createCell(colNum++);
        cell.setCellValue("Task Name");

        cell = header.createCell(colNum++);
        cell.setCellValue("Languages");

        cell = header.createCell(colNum++);
        cell.setCellValue("Task Notes");

        cell = header.createCell(colNum);
        cell.setCellValue("Next Step");

        int maxCol = colNum;

        for (TaskInfo info : taskList) {
            List<String> langList = info.getLanguageSet().stream().sorted().collect(Collectors.toList());
            String languageStr = String.join(", ", langList);

            Row taskRow = sheet.createRow(rowNum++);
            colNum = 0;

            cell = taskRow.createCell(colNum++);
            cell.setCellValue(info.getCategory());

            cell = taskRow.createCell(colNum++);
            cell.setCellValue(info.getTaskName());

            cell = taskRow.createCell(colNum++);
            cell.setCellValue(languageStr);

            ++colNum;

            if (null != info.getNext()) {
                cell = taskRow.createCell(colNum);
                cell.setCellValue("ppr for " + info.getNext());
            }
        }

        for (int i = 0; i < maxCol; ++i) {
            sheet.autoSizeColumn(i);
        }

    }

    private static void writeLanguageDown(XSSFWorkbook workbook, Map<String, Long> langStatMap) {
        List<Pair<String, Long>> statList = new ArrayList<>();
        for (Entry<String, Long> entry : langStatMap.entrySet()) {
            statList.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        statList.sort(Collections.reverseOrder(Comparator.comparing(Pair::getValue)));

        XSSFSheet sheet = workbook.createSheet("Breakdown");
        int rowNum = 0;

        XSSFRow header = sheet.createRow(rowNum++);
        Cell cell;

        cell = header.createCell(0);
        cell.setCellValue("Language");

        cell = header.createCell(1);
        cell.setCellValue("Size");

        for (Pair<String, Long> entry : statList) {
            Row row = sheet.createRow(rowNum++);

            cell = row.createCell(0);
            cell.setCellValue(entry.getKey());

            cell = row.createCell(1);
            cell.setCellValue(entry.getValue());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 0, 14, 30);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Language Breakdown");

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        CellRangeAddress catRange = new CellRangeAddress(1, rowNum - 1, 0, 0);
        XDDFDataSource<String> cat = XDDFDataSourcesFactory.fromStringCellRange(sheet, catRange);

        CellRangeAddress valRange = new CellRangeAddress(1, rowNum - 1, 1, 1);
        XDDFNumericalDataSource<Double> val = XDDFDataSourcesFactory.fromNumericCellRange(sheet, valRange);

        XDDFChartData data = new XDDFPieChartData(chart.getCTChart().getPlotArea().addNewPieChart());
        data.setVaryColors(true);
        data.addSeries(cat, val);
        chart.plot(data);
    }

    public static void writeReport(Collection<TaskInfo> taskInfoCollection, Map<String, Long> langStatMap) {
        List<TaskInfo> taskList = taskInfoCollection.stream()
            .filter(task -> !task.getLanguageSet().isEmpty())
            .sorted()
            .collect(Collectors.toList());

        XSSFWorkbook workbook = new XSSFWorkbook();
        writeOpenTasks(workbook, taskList);
        writeLanguageDown(workbook, langStatMap);

        try {
            FileOutputStream outputStream = new FileOutputStream("target/rosetta.xlsx");
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new UtilException(e);
        }

        System.out.println("Done");
    }
}
