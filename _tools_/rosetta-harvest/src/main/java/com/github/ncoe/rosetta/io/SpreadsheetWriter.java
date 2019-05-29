package com.github.ncoe.rosetta.io;

import com.github.ncoe.rosetta.dto.TaskInfo;
import com.github.ncoe.rosetta.exception.UtilException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.math.NumberUtils;
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
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;

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

/**
 * For generating a spreadsheet for easily manipulating open tasks, tracking progress, and viewing gathered statistics.
 */
public final class SpreadsheetWriter {
    private SpreadsheetWriter() {
        throw new NotImplementedException("No SpreadsheetWriter for you!");
    }

    private static void writeOpenTasks(Workbook workbook, List<TaskInfo> taskList) {
        Sheet sheet = workbook.createSheet("In Progress");

        int rowNum = 0;
        int colNum = 0;
        Cell cell;

        // Write a header for the tasks
        Row header = sheet.createRow(rowNum++);

        // category header
        cell = header.createCell(colNum++);
        cell.setCellValue("Category");

        // task name header
        cell = header.createCell(colNum++);
        cell.setCellValue("Task Name");

        // languages header
        cell = header.createCell(colNum++);
        cell.setCellValue("Languages");

        // task notes header
        cell = header.createCell(colNum++);
        cell.setCellValue("Task Notes");

        // next step header
        cell = header.createCell(colNum++);
        cell.setCellValue("Next Step");

        // last modified header
        cell = header.createCell(colNum++);
        cell.setCellValue("Last modified");

        int maxCol = colNum;

        // Fill in the task info as additional rows
        for (TaskInfo info : taskList) {
            List<String> langList = info.getLanguageSet().stream().sorted().collect(Collectors.toList());
            String languageStr = String.join(", ", langList);

            // prepare a new row
            Row taskRow = sheet.createRow(rowNum++);
            colNum = 0;

            // task category
            cell = taskRow.createCell(colNum++);
            cell.setCellValue(info.getCategory());

            // task name
            cell = taskRow.createCell(colNum++);
            String taskName = info.getTaskName();
            if (NumberUtils.isParsable(taskName)) {
                cell.setCellValue("'" + taskName);
            } else {
                cell.setCellValue(taskName);
            }

            // task languages
            cell = taskRow.createCell(colNum++);
            cell.setCellValue(languageStr);

            if (null != info.getNote()) {
                cell = taskRow.createCell(colNum++);
                cell.setCellValue(info.getNote());
            } else {
                ++colNum;
            }

            // task next steps
            if (null != info.getNext()) {
                cell = taskRow.createCell(colNum++);
                cell.setCellValue("ppr for " + info.getNext());
            }

            // last modified (for pending solutions)
            if (null != info.getLastModified()) {
                cell = taskRow.createCell(colNum);
                cell.setCellValue(info.getLastModified().toString());
            }
        }

        // have the sheet adjust the column sizes to fit the text
        for (int i = 0; i < maxCol; ++i) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * @param workbook    the workbook to add a sheet for language statistics to
     * @param langStatMap the total file sizes by language
     */
    private static void writeLanguageDown(XSSFWorkbook workbook, Map<String, Long> langStatMap) {
        // transform the data so it is ordered
        List<Pair<String, Long>> statList = new ArrayList<>();
        for (Entry<String, Long> entry : langStatMap.entrySet()) {
            statList.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        statList.sort(Collections.reverseOrder(Comparator.comparing(Pair::getValue)));

        // create the worksheet
        XSSFSheet sheet = workbook.createSheet("Breakdown");
        int rowNum = 0;

        // create the header
        XSSFRow header = sheet.createRow(rowNum++);
        Cell cell;

        // language header
        cell = header.createCell(0);
        cell.setCellValue("Language");

        // size header
        cell = header.createCell(1);
        cell.setCellValue("Size");

        // process each language
        for (Pair<String, Long> entry : statList) {
            Row row = sheet.createRow(rowNum++);

            // language name
            cell = row.createCell(0);
            cell.setCellValue(entry.getKey());

            // total size
            cell = row.createCell(1);
            cell.setCellValue(entry.getValue());
        }

        // autosize each column to fit contents
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        // add a pie chart for visual comparision
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

        // connect the data to the pie chart (also showed the missing requirement for command-line building)
        CTChart ctChart = chart.getCTChart();
        CTPlotArea plotArea = ctChart.getPlotArea();
        CTPieChart pieChart = plotArea.addNewPieChart();
        XDDFChartData data = new XDDFPieChartData(pieChart);
        data.setVaryColors(true);
        data.addSeries(cat, val);
        chart.plot(data);
    }

    /**
     * @param taskInfoCollection the tasks which should be considered for inclusion
     * @param langStatMap        statistics to track for each language
     */
    public static void writeReport(Collection<TaskInfo> taskInfoCollection, Map<String, Long> langStatMap) {
        // pre-filter the tasks so only actionable data is written
        List<TaskInfo> taskList = taskInfoCollection.stream()
            .filter(task -> !task.getLanguageSet().isEmpty())
            .sorted()
            .collect(Collectors.toList());

        // create and populate a workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        writeOpenTasks(workbook, taskList);
        writeLanguageDown(workbook, langStatMap);

        // Write the workbook out to disk
        try {
            FileOutputStream outputStream = new FileOutputStream("target/rosetta.xlsx");
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new UtilException(e);
        }
    }
}
