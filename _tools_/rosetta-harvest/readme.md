# Known Issues
* Cannot harvest C++ because it redirects to itself
* The pie chart is missing the second chart, and the labels (is this possible to add?)
* Restore the rest of the pom
* Restore the rest of the notes (if there were any)
* ~/github/ncoe/rosetta/_tools_/rosetta-harvest/src/main/java/com/github/ncoe/rosetta/io/SpreadsheetWriter.java:[145,69] org.openxmlformats.schemas.drawingml.x2006.chart.CTChart.getPlotArea() in package org.openxmlformats.schemas.drawingml.x2006.chart is not accessible
  (package org.openxmlformats.schemas.drawingml.x2006.chart is declared in the unnamed module, but module org.openxmlformats.schemas.drawingml.x2006.chart does not read it)
    * The poi dependencies need to at least declare an automatic module name for `mvn clean install` to function
    * A workaround is to build and execute with an IDE
* Consider using a logging framework like slf4j
