package com.github.ncoe.rosetta.io;

import com.github.ncoe.rosetta.dto.TaskInfo;
import com.github.ncoe.rosetta.exception.UtilException;
import com.github.ncoe.rosetta.util.LocalUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFPieChartData;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
    /**
     * Output file name.
     */
    public static final String FILENAME = "rosetta.xlsx";

    private SpreadsheetWriter() {
        throw new NotImplementedException("No SpreadsheetWriter for you!");
    }

    /**
     * @param workbook the workbook to add a sheet for task info
     * @param taskList the list of tasks where there is work that could be done
     */
    private static void writeOpenTasks(XSSFWorkbook workbook, List<TaskInfo> taskList) {
        XSSFCreationHelper helper = new XSSFCreationHelper(workbook);

        XSSFCellStyle hLinkStyle = workbook.createCellStyle();
        XSSFFont hLinkFont = workbook.createFont();
        hLinkFont.setUnderline(XSSFFont.U_SINGLE);
        hLinkFont.setColor(IndexedColors.BLUE.index);
        hLinkStyle.setFont(hLinkFont);

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
            List<String> langList = info.getLanguageSet()
                .stream()
                .sorted()
                .collect(Collectors.toList());
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
            String taskNameUrl = String.format(
                "http://rosettacode.org/wiki/%s", info.getTaskName().replace("\"", "%22")
            );

            XSSFHyperlink link = helper.createHyperlink(HyperlinkType.URL);
            link.setAddress(taskNameUrl);
            link.setLabel(taskName);

            cell.setHyperlink(link);
            if (NumberUtils.isParsable(taskName)) {
                cell.setCellValue("'" + taskName);
            } else {
                cell.setCellValue(taskName);
            }
            cell.setCellStyle(hLinkStyle);

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

        sheet.createFreezePane(0, 1);

        /* support initial sorting definition (may not be currently accessible with current builds)
         * <sortState ref="A2:F1087">
         *     <sortCondition ref="A2:A1087"/>
         *     <sortCondition ref="F2:F1087"/>
         *     <sortCondition ref="B2:B1087"/>
         * </sortState>
         */
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

        Long total = statList.stream()
            .map(Pair::getValue)
            .reduce(Long::sum)
            .orElse(0L);
        int cutPoint = 0;

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

            if (100.0 * entry.getValue() / total > 5.0) {
                cutPoint = rowNum;
            }
        }

        // autosize each column to fit contents
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        // add data labels -> add data callouts (microsoft 2012 is the schema seen locally)
        // ^ a version of this can be accomplished with the setShowLeaderLines property with the available charts
        /* chart type from "pie" to "pipe of pie" or "bar of pie" (min of 5%)(microsoft 2012 is the schema seen locally)
         * <c:ofPieChart>
         *      <c:ofPieType val="pie"/>
         * ...
         * </c:ofPieChart>
         */
        // ^ that can be done manually by putting some data in one chart and the rest in another

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // add a pie chart for visual comparision of primary languages
        XSSFDrawing primaryDrawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor primaryAnchor = primaryDrawing.createAnchor(0, 0, 0, 0, 4, 0, 14, 30);

        XSSFChart primaryChart = primaryDrawing.createChart(primaryAnchor);
//        primaryChart.setTitleText("Primary Language Breakdown");

//        XDDFChartLegend primaryLegend = primaryChart.getOrAddLegend();
//        primaryLegend.setPosition(LegendPosition.TOP_RIGHT);

        CellRangeAddress primaryCatRange = new CellRangeAddress(1, cutPoint - 1, 0, 0);
        XDDFDataSource<String> primaryCat = XDDFDataSourcesFactory.fromStringCellRange(sheet, primaryCatRange);

        CellRangeAddress primaryValRange = new CellRangeAddress(1, cutPoint - 1, 1, 1);
        XDDFNumericalDataSource<Double> primaryVal = XDDFDataSourcesFactory.fromNumericCellRange(sheet, primaryValRange);

        // connect the data to the pie chart (also showed the missing requirement for command-line building)
        CTChart primaryCtChart = primaryChart.getCTChart();
        CTPlotArea primaryPlotArea = primaryCtChart.getPlotArea();
        CTPieChart primaryPieChart = primaryPlotArea.addNewPieChart();
        XDDFChartData primaryData = new XDDFPieChartData(primaryPieChart);
        primaryData.setVaryColors(true);
        XDDFChartData.Series primarySeries = primaryData.addSeries(primaryCat, primaryVal);
        primarySeries.setShowLeaderLines(true);
        primarySeries.setTitle("Primary", null);
        primaryChart.plot(primaryData);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // add a pie chart for visual comparision of secondary languages
        XSSFDrawing secondaryDrawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor secondaryAnchor = secondaryDrawing.createAnchor(0, 0, 0, 0, 15, 0, 25, 30);

        XSSFChart secondaryChart = secondaryDrawing.createChart(secondaryAnchor);
//        secondaryChart.setTitleText("Secondary Language Breakdown");

//        XDDFChartLegend secondaryLegend = secondaryChart.getOrAddLegend();
//        secondaryLegend.setPosition(LegendPosition.TOP_RIGHT);

        CellRangeAddress secondaryCatRange = new CellRangeAddress(cutPoint, rowNum - 1, 0, 0);
        XDDFDataSource<String> secondaryCat = XDDFDataSourcesFactory.fromStringCellRange(sheet, secondaryCatRange);

        CellRangeAddress secondaryValRange = new CellRangeAddress(cutPoint, rowNum - 1, 1, 1);
        XDDFNumericalDataSource<Double> secondaryVal = XDDFDataSourcesFactory.fromNumericCellRange(sheet, secondaryValRange);

        // connect the data to the pie chart (also showed the missing requirement for command-line building)
        CTChart secondaryCtChart = secondaryChart.getCTChart();
        CTPlotArea secondaryPlotArea = secondaryCtChart.getPlotArea();
        CTPieChart secondaryPieChart = secondaryPlotArea.addNewPieChart();
        XDDFChartData secondaryData = new XDDFPieChartData(secondaryPieChart);
        secondaryData.setVaryColors(true);
        XDDFChartData.Series secondarySeries = secondaryData.addSeries(secondaryCat, secondaryVal);
        secondarySeries.setShowLeaderLines(true);
        secondarySeries.setTitle("Secondary", null);
        secondaryChart.plot(secondaryData);
    }

    /**
     * @param taskInfoCollection the tasks which should be considered for inclusion
     * @param langStatMap        statistics to track for each language
     */
    public static void writeReport(Collection<TaskInfo> taskInfoCollection, Map<String, Long> langStatMap) {
        Path filePath = Path.of(LocalUtil.OUTPUT_DIRECTORY, FILENAME);

        // Make tasks that need a buddy, but not for any chosen language more visible
        taskInfoCollection.stream()
            .filter(task -> task.getCategory() == 1.0 && task.getLanguageSet().isEmpty())
            .forEach(task -> task.setCategory(5.0));

        // pre-filter the tasks so only actionable data is written
        List<TaskInfo> taskList = taskInfoCollection.stream()
            .filter(task -> !task.getLanguageSet().isEmpty() && task.getCategory() < 5.0)
            .sorted()
            .collect(Collectors.toList());

        // create and populate a workbook
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            writeOpenTasks(workbook, taskList);
            writeLanguageDown(workbook, langStatMap);

            // Write the workbook out to disk
            try (OutputStream writer = Files.newOutputStream(filePath)) {
                workbook.write(writer);
            }
        } catch (IOException e) {
            throw new UtilException(e);
        }
    }
}
