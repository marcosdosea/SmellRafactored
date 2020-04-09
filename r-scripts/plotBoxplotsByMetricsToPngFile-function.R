
plotBoxplotsByMetricDesignRoleToPngFile <- function(data, projectName, imgFileName, metricCode, yLabel) {
  designRoles <- data$designRole
  designRoles <- unique(designRoles)
  for (dr in designRoles){
    dataDr <- filter(data, designRole == dr)
    if (length(dataDr$designRole) > 1) {
      print(dr)
      print(length(dataDr$designRole))
      resultDrPlot <- ggplot(dataDr, aes(x=recordType, y=metricCode, group=technique)) +
        geom_boxplot(aes(colour=recordType, fill=technique), alpha=0.3) +
        # theme_ipsum() +
        # ggtitle(projectName) +
        xlab("Event Type") +
        ylab(yLabel) +
        # theme(legend.position="none") +
        scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors(), breaks=getTechniqueKeys(), labels = getTechniqueLabels()) +  
        scale_fill_manual(getTechniqueLegend(), values = getTechniqueFills(), breaks=getTechniqueKeys(), labels = getTechniqueLabels()) 
      # scale_shape_manual(getGenericLegend(), values = getRecordTypeShapes(), breaks=getTechniqueKeys(), labels = getTechniqueLabels())  
      drSuffix <- paste0("-", dr, ".png")
      imgDrFileName <-sub(".png", drSuffix, imgFileName)
      savePlotToPngFile(resultDrPlot, imgDrFileName, 1)
    }
  }
  # return(resultDrPlot)
}


plotBoxplotsByTechniqueMetricToPngFile <- function(data, projectName, imgFileName, metricCode, yLabel, deepenForDesignRole) {
  recordTypes <- data$recordType
  recordTypes <- unique(recordTypes)
  if (length(recordTypes) >= 2) { # (length(data[, 1]) > 0)  
    resultPlot <- ggplot(data=data, aes(x=recordType, y=targetMetric, group=technique)) +
      geom_boxplot(aes(colour=recordType, fill=technique), alpha=0.3) +
      # geom_boxplot(aes(colour=recordType, fill=recordType), alpha=0.3, outlier.shape = NA) + 
      # scale_y_continuous(limits = quantile(data$targetMetric, c(0.1, 0.9))) +
      # theme_ipsum() +
      # ggtitle(projectName) +
      xlab("Event Type") +
      ylab(yLabel) +
      # theme(legend.position="none") +
      scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors(), breaks=getTechniqueKeys(), labels = getTechniqueLabels()) +  
      scale_fill_manual(getTechniqueLegend(), values = getTechniqueFills(), breaks=getTechniqueKeys(), labels = getTechniqueLabels()) 
      # scale_shape_manual(getGenericLegend(), values = getRecordTypeShapes(), breaks=getTechniqueKeys(), labels = getTechniqueLabels())  
      savePlotToPngFile(resultPlot, imgFileName, 1)
    # deepenForDesignRole <- TRUE
    if (deepenForDesignRole) {
      plotBoxplotsByMetricDesignRoleToPngFile(data, projectName, imgFileName, metricCode, yLabel)
    }
  }
  # return(resultPlot)
}


plotBoxplotsByMetricToPngFile <- function(data, projectName, csvFileName, metricCode, yLabel, deepenForDesignRole) {
  # dataRefacoring <- filter(data, recordType == "Refactoring")
  # dataTechnique <- filter(data, technique == tech)
  # dataTechnique <- filter(data, (technique == tech) | (recordType == "Refactoring") ) 
  fileSuffix <- paste0("-boxplots-", metricCode, ".png")
  imgFileName <-sub("-plot.csv", fileSuffix, csvFileName)
  plotBoxplotsByTechniqueMetricToPngFile(data, projectName, imgFileName, metricCode, yLabel, deepenForDesignRole);
}
