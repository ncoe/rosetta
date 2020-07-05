package com.github.ncoe.rosetta.io;

import com.github.ncoe.rosetta.dto.LanguageInfo;
import com.github.ncoe.rosetta.dto.TaskInfo;
import com.github.ncoe.rosetta.exception.UtilException;
import com.github.ncoe.rosetta.util.LanguageUtil;
import com.github.ncoe.rosetta.util.LocalUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
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
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSortCondition;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSortState;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;

/**
 * For generating a spreadsheet for easily manipulating open tasks, tracking progress, and viewing gathered statistics.
 */
public final class SpreadsheetWriter {
    /**
     * Output file name.
     */
    public static final String FILENAME = "rosetta.xlsx";

    private static final Logger LOG = LoggerFactory.getLogger(SpreadsheetWriter.class);

    private SpreadsheetWriter() {
        throw new NotImplementedException("No SpreadsheetWriter for you!");
    }

    /**
     * @param workbook the workbook to add a sheet for task info
     * @param taskList the list of tasks where there is work that could be done
     */
    private static void writeOpenTasks(XSSFWorkbook workbook, List<TaskInfo> taskList) {
        XSSFCreationHelper helper = new XSSFCreationHelper(workbook);

        XSSFFont boldFont = workbook.createFont();
        boldFont.setBold(true);
        XSSFCellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);

        XSSFCellStyle hLinkStyle = workbook.createCellStyle();
        XSSFFont hLinkFont = workbook.createFont();
        hLinkFont.setUnderline(XSSFFont.U_SINGLE);
        hLinkFont.setColor(IndexedColors.BLUE.index);
        hLinkStyle.setFont(hLinkFont);

        XSSFSheet sheet = workbook.createSheet("In Progress");

        int rowNum = 0;
        int colNum = 0;
        Cell cell;

        // Write a header for the tasks
        Row header = sheet.createRow(rowNum++);

        // category header
        int categoryCol = colNum;
        cell = header.createCell(colNum++);
        cell.setCellValue("Category");

        // task name header
        int taskNameCol = colNum;
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
        int lastModifiedCol = colNum;
        cell = header.createCell(colNum++);
        cell.setCellValue("Last modified");

        int maxCol = colNum;
        configureSortState(sheet, categoryCol, taskNameCol, lastModifiedCol, taskList.size());

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
                if (info.getCategory() == 0.0) {
                    cell.setCellValue("ppr for " + info.getNext());
                } else {
                    if (info.getCategory() == 1.0) {
                        cell.setCellStyle(boldStyle);
                    }
                    cell.setCellValue(info.getNext());
                }
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
    }

    private static void configureSortState(XSSFSheet sheet, int categoryCol, int taskNameCol, int lastModifiedCol, int numTasks) {
        CTWorksheet ctWorksheet = sheet.getCTWorksheet();
        CTSortState sortState;
        CTSortCondition ctSortCondition;
        CellRangeAddress rangeAddress;

        int minCol = NumberUtils.min(categoryCol, taskNameCol, lastModifiedCol);
        int maxCol = NumberUtils.max(categoryCol, taskNameCol, lastModifiedCol);
        rangeAddress = new CellRangeAddress(1, numTasks, minCol, maxCol);
        sortState = ctWorksheet.addNewSortState();
        sortState.setRef(rangeAddress.formatAsString());

        rangeAddress = new CellRangeAddress(1, numTasks, categoryCol, categoryCol);
        ctSortCondition = sortState.addNewSortCondition();
        ctSortCondition.setRef(rangeAddress.formatAsString());

        rangeAddress = new CellRangeAddress(1, numTasks, lastModifiedCol, lastModifiedCol);
        ctSortCondition = sortState.addNewSortCondition();
        ctSortCondition.setRef(rangeAddress.formatAsString());

        rangeAddress = new CellRangeAddress(1, numTasks, taskNameCol, taskNameCol);
        ctSortCondition = sortState.addNewSortCondition();
        ctSortCondition.setRef(rangeAddress.formatAsString());
    }

    private static void insertChart(XSSFSheet sheet, String title, int firstRow, int lastRow, int labelCol, int dataCol, int slot, int chartColumn) {
        int rowNum = 31 * (slot / 2);
        int colNum = chartColumn + 11 * (slot % 2);

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, colNum, rowNum, colNum + 10, rowNum + 30);

        XSSFChart chart = drawing.createChart(anchor);
        //chart.setTitleText("X Language Breakdown")

        //XDDFChartLegend legend = chart.getOrAddLegend()
        //legend.setPosition(LegendPosition.TOP_RIGHT)

        CellRangeAddress labelRange = new CellRangeAddress(firstRow, lastRow, labelCol, labelCol);
        XDDFDataSource<String> labelData = XDDFDataSourcesFactory.fromStringCellRange(sheet, labelRange);

        CellRangeAddress dataRange = new CellRangeAddress(firstRow, lastRow, dataCol, dataCol);
        XDDFNumericalDataSource<Double> dataData = XDDFDataSourcesFactory.fromNumericCellRange(sheet, dataRange);

        // connect the data to the pie chart
        XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);
        data.setVaryColors(true);
        XDDFChartData.Series series = data.addSeries(labelData, dataData);
        series.setShowLeaderLines(true);
        series.setTitle(title, null);
        chart.plot(data);
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
        statList.sort(Collections.reverseOrder(Entry.comparingByValue()));

        XSSFFont incFont = workbook.createFont();
        incFont.setColor(IndexedColors.GREEN.index);
        XSSFCellStyle incStyle = workbook.createCellStyle();
        incStyle.setFont(incFont);

        XSSFFont decFont = workbook.createFont();
        decFont.setColor(IndexedColors.RED.index);
        XSSFCellStyle decStyle = workbook.createCellStyle();
        decStyle.setFont(decFont);

        XSSFCellStyle numStyle = workbook.createCellStyle();
        numStyle.setDataFormat(0xa); //BuiltinFormats: 0.00%

        XSSFFont boldFont = workbook.createFont();
        boldFont.setBold(true);
        XSSFCellStyle numBoldStyle = numStyle.copy();
        numBoldStyle.setFont(boldFont);
        XSSFCellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);

        // create the worksheet
        XSSFSheet sheet = workbook.createSheet("Breakdown");
        int rowNum = 0;

        // create the header
        XSSFRow header = sheet.createRow(rowNum++);

        // language header
        header.createCell(0).setCellValue("Language");

        // size header
        header.createCell(1).setCellValue("Size");

        // percent header
        header.createCell(2).setCellValue("Percent");

        // per chart header
        header.createCell(3).setCellValue("Per Chart");

        // direction
        header.createCell(4).setCellValue("Direction");

        List<Integer> startList = new ArrayList<>();
        startList.add(0);
        List<CellRangeAddress> rangeList = new ArrayList<>();

        // process each language
        double cumulative = 0.0;
        for (Pair<String, Long> entry : statList) {
            Row row = sheet.createRow(rowNum++);

            // language name
            Cell langCell = row.createCell(0);
            langCell.setCellValue(entry.getKey());

            // total size
            Cell sizeCell = row.createCell(1);
            sizeCell.setCellValue(entry.getValue());

            // directionality
            Cell dirCell = row.createCell(4);
            Optional<LanguageInfo> langOpt = LanguageUtil.find(entry.getKey());
            int direction = langOpt.map(LanguageInfo::getHarvest).orElse(0);
            switch (direction) {
                case -1:
                    dirCell.setCellValue("V");
                    dirCell.setCellStyle(decStyle);
                    break;
                case 0:
                    dirCell.setCellValue("-");
                    break;
                case 1:
                    dirCell.setCellValue("^");
                    dirCell.setCellStyle(incStyle);
                    break;
                default:
                    dirCell.setCellValue("error");
                    break;
            }

            cumulative += entry.getValue();
            Integer startIndex = startList.get(startList.size() - 1);
            double ratio = 100.0 * entry.getValue() / cumulative;
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    "Relative abundance of {} is {}",
                    value("language", entry.getKey()),
                    value("ratio", ratio)
                );
            }
            if (ratio < 5.7) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Resetting {} to 100%", value("language", entry.getKey()));
                }

                langCell.setCellStyle(boldStyle);
                sizeCell.setCellStyle(boldStyle);

                cumulative = entry.getValue();
                startList.add(rowNum - 2);

                CellRangeAddress prevRange = new CellRangeAddress(startIndex + 1, rowNum - 2, 1, 1);
                rangeList.add(prevRange);
            } else if (rowNum == 2) {
                langCell.setCellStyle(boldStyle);
                sizeCell.setCellStyle(boldStyle);
            }
        }

        // Calculate chart ranges
        Integer lastIndex = startList.get(startList.size() - 1);
        CellRangeAddress finalRange = new CellRangeAddress(lastIndex + 1, rowNum - 1, 1, 1);
        rangeList.add(finalRange);

        CellRangeAddress totalRange = new CellRangeAddress(1, rowNum - 1, 1, 1);
        String totalRangeStr = totalRange.formatAsString(null, true);

        int rangeIndex = 0;
        for (int i = 1; i <= statList.size(); i++) {
            Row row = sheet.getRow(i);

            CellRangeAddress subsetRange = rangeList.get(rangeIndex);
            if (!subsetRange.containsRow(i)) {
                rangeIndex++;
                subsetRange = rangeList.get(rangeIndex);
            }

            CellRangeAddress dataRange = new CellRangeAddress(i, i, 1, 1);
            String dataRefStr = dataRange.formatAsString(null, true);
            String rangeStr = subsetRange.formatAsString(null, true);

            Cell cell;

            cell = row.createCell(2);
            cell.setCellFormula(String.format("%s / SUM(%s)", dataRefStr, totalRangeStr));
            if (i == subsetRange.getFirstRow()) {
                cell.setCellStyle(numBoldStyle);
            } else {
                cell.setCellStyle(numStyle);
            }

            cell = row.createCell(3);
            cell.setCellFormula(String.format("%s / SUM(%s)", dataRefStr, rangeStr));
            if (i == subsetRange.getFirstRow()) {
                cell.setCellStyle(numBoldStyle);
            } else {
                cell.setCellStyle(numStyle);
            }
        }

        // autosize each column to fit contents
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);

        //there may be a chart with a single range if other languages are relatively too dominant
        for (int i = 0; i < rangeList.size(); i++) {
            CellRangeAddress range = rangeList.get(i);
            insertChart(sheet, "C" + (i + 1), range.getFirstRow(), range.getLastRow(), 0, 1, i, 6);
        }
    }

    /**
     * @param taskInfoCollection the tasks which should be considered for inclusion
     * @param langStatMap        statistics to track for each language
     */
    public static void writeReport(Collection<TaskInfo> taskInfoCollection, Map<String, Long> langStatMap) {
        Path filePath = Path.of(LocalUtil.OUTPUT_DIRECTORY, FILENAME);

        // Make tasks that need a buddy, but not for any chosen language less visible
        taskInfoCollection.stream()
            .filter(task -> task.getCategory() == 1.0 && task.getLanguageSet().isEmpty())
            .forEach(task -> task.setCategory(5.0));

        // Make tasks that have one final language to be implemented in more visible
        taskInfoCollection.stream()
            .filter(task -> task.getCategory() == 2.0 && task.getLanguageSet().size() == 1)
            .forEach(task -> task.setCategory(1.999));

        // pre-filter the tasks so only actionable data is written
        List<TaskInfo> taskList = taskInfoCollection.stream()
            .filter(
                task -> !task.getLanguageSet().isEmpty() && task.getCategory() < 5.0
                    || task.getCategory() < 0.5
            ).sorted()
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
