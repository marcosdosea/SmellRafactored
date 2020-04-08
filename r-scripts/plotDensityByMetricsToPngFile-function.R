
plotDensityByMetricDesignRoleToPngFile <- function(data, projectName, imgFileName, metricCode, xLabel) {
  designRoles <- data$designRole
  designRoles <- unique(designRoles)
  for (dr in designRoles){
    # dataDr <- subset(data,designRole == dr) 
    # dataDr <- data[data$designRole==dr, ]
    dataDr <- filter(data, designRole == dr)
    if (length(dataDr$designRole) > 2) {
      print(dr)
      print(length(dataDr$designRole))
      resultDrPlot <- ggplot(dataDr, aes(x=metricCode, group=recordType)) +
        geom_density(aes(colour=recordType, fill=recordType), alpha=0.3) +
        # theme_ipsum() +
        # ggtitle(projectName) +
        xlab(xLabel) +
        scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
        scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
      drSuffix <- paste0("-", dr, ".png")
      imgDrFileName <-sub(".png", drSuffix, imgFileName)
      savePlotToPngFile(resultDrPlot, imgDrFileName, 1)
    }
  }
  # return(resultDrPlot)
}

plotDensityByTechniqueMetricToPngFile <- function(dataTechnique, projectName, imgFileName, metricCode, xLabel, deepenForDesignRole) {
  recordTypes <- dataTechnique$recordType
  recordTypes <- unique(recordTypes)
  if (length(dataTechnique$commitDateTime) > 2) {
    resultPlot <- ggplot(data=dataTechnique, aes(x=targetMetric, group=recordType)) +
      geom_density(aes(colour=recordType, fill=recordType), alpha=0.3) +
      # theme_ipsum() +
      # ggtitle(projectName) +
      xlab(xLabel) +
      scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
      scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
    savePlotToPngFile(resultPlot, imgFileName, 1)
    # deepenForDesignRole <- TRUE
    if (deepenForDesignRole) {
      plotDensityByMetricDesignRoleToPngFile(dataTechnique, projectName, imgFileName, metricCode, xLabel)
    }
  }
  # return(resultPlot)
}

plotDensityByMetricToPngFile <- function(data, projectName, csvFileName, metricCode, xLabel, deepenForDesignRole) {
  techniqueList <- data$technique
  techniqueList <- unique(techniqueList)
  for (tech in techniqueList){
    if (tech != "") {
      # dataRefacoring <- filter(data, recordType == "Refactoring")
      # dataTechnique <- filter(data, technique == tech)
      dataTechnique <- filter(data, (technique == tech) | (recordType == "Refactoring") ) 
      fileSuffix <- paste0("-", tech, "-density-", metricCode, ".png")
      imgFileName <-sub("-plot.csv", fileSuffix, csvFileName)
      plotDensityByTechniqueMetricToPngFile(dataTechnique, projectName, imgFileName, metricCode, xLabel, deepenForDesignRole);
    }
  }
}
