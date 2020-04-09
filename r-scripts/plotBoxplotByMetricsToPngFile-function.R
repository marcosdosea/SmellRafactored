
plotBoxplotByMetricDesignRoleToPngFile <- function(data, projectName, imgFileName, metricCode, yLabel) {
  designRoles <- data$designRole
  designRoles <- unique(designRoles)
  for (dr in designRoles){
    dataDr <- filter(data, designRole == dr)
    if ( (length(recordTypes) >= 2) & (length(dataDr$designRole) > 1) ) {
      print(dr)
      print(length(dataDr$designRole))
      resultDrPlot <- ggplot(dataDr, aes(x=recordType, y=metricCode, group=recordType)) +
        geom_boxplot(aes(colour=recordType, fill=recordType), alpha=0.3) +
        # theme_ipsum() +
        # ggtitle(projectName) +
        xlab("") +
        ylab(yLabel) +
        theme(legend.position="none") +
        scale_colour_manual(getGenericLegend(), values = getRecordTypeColors()) +  
        scale_fill_manual(getGenericLegend(), values = getRecordTypeFills()) 
      drSuffix <- paste0("-", dr, ".png")
      imgDrFileName <-sub(".png", drSuffix, imgFileName)
      savePlotToPngFile(resultDrPlot, imgDrFileName, 1)
    }
  }
  # return(resultDrPlot)
}


plotBoxplotByTechniqueMetricToPngFile <- function(data, projectName, imgFileName, metricCode, yLabel, deepenForDesignRole) {
  recordTypes <- data$recordType
  recordTypes <- unique(recordTypes)
  if (length(recordTypes) >= 2) { # (length(data[, 1]) > 0)
    resultPlot <- ggplot(data=data, aes(x=recordType, y=targetMetric, group=recordType)) +
      geom_boxplot(aes(colour=recordType, fill=recordType), alpha=0.3) +
      # geom_boxplot(aes(colour=recordType, fill=recordType), alpha=0.3, outlier.shape = NA) + 
      # scale_y_continuous(limits = quantile(data$targetMetric, c(0.1, 0.9))) +
      # theme_ipsum() +
      # ggtitle(projectName) +
      xlab("") +
      ylab(yLabel) +
      theme(legend.position="none") +
      scale_colour_manual(getGenericLegend(), values = getRecordTypeColors()) +  
      scale_fill_manual(getGenericLegend(), values = getRecordTypeFills()) 
    savePlotToPngFile(resultPlot, imgFileName, 1)
    # deepenForDesignRole <- TRUE
    if (deepenForDesignRole) {
      plotBoxplotByMetricDesignRoleToPngFile(data, projectName, imgFileName, metricCode, yLabel)
    }
  }
  # return(resultPlot)
}


plotBoxplotByMetricToPngFile <- function(data, projectName, csvFileName, metricCode, yLabel, deepenForDesignRole) {
  techniqueList <- data$technique
  techniqueList <- unique(techniqueList)
  for (tech in techniqueList){
    if (tech != "") {
      # dataRefacoring <- filter(data, recordType == "Refactoring")
      # dataTechnique <- filter(data, technique == tech)
      dataTechnique <- filter(data, (technique == tech) | (recordType == "Refactoring") ) 
      fileSuffix <- paste0("-", tech, "-boxplot-", metricCode, ".png")
      imgFileName <-sub("-plot.csv", fileSuffix, csvFileName)
      plotBoxplotByTechniqueMetricToPngFile(dataTechnique, projectName, imgFileName, metricCode, yLabel, deepenForDesignRole);
    }
  }
}
