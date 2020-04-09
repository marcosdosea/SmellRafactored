
plotFreqPolyByMetricDesignRoleToPngFile <- function(data, projectName, imgFileName, metricCode, xLabel) {
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
        geom_freqpoly(aes(colour=recordType), alpha=0.3, bins = 30, position = 'identity') + # , fill=recordType
        # theme_ipsum() +
        # ggtitle(projectName) +
        xlab(xLabel) +
        scale_colour_manual(getGenericLegend(), values = getRecordTypeColors()) +  
        scale_fill_manual(getGenericLegend(), values = getRecordTypeFills()) 
      drSuffix <- paste0("-", dr, ".png")
      imgDrFileName <- sub(".png", drSuffix, imgFileName)
      print(imgDrFileName)
      savePlotToPngFile(resultDrPlot, imgDrFileName, 1)
    }
  }
  # return(resultDrPlot)
}

plotFreqPolyByTechniqueMetricToPngFile <- function(dataTechnique, projectName, imgFileName, metricCode, xLabel, deepenForDesignRole) {
  if (length(dataTechnique$commitDateTime) > 2) {
    resultPlot <- ggplot(data=dataTechnique, aes(x=targetMetric, group=recordType)) +
      geom_freqpoly(aes(colour=recordType), alpha=0.3, bins = 30, position = 'identity') + # , fill=recordType
      # theme_ipsum() +
      # ggtitle(projectName) +
      xlab(xLabel) +
      scale_colour_manual(getGenericLegend(), values = getRecordTypeColors()) +  
      scale_fill_manual(getGenericLegend(), values = getRecordTypeFills()) 
    savePlotToPngFile(resultPlot, imgFileName, 1)
    # deepenForDesignRole <- TRUE
    if (deepenForDesignRole) {
      plotFreqPolyByTechniqueMetricToPngFile(dataTechnique, projectName, imgFileName, metricCode, xLabel)
    }
  }
  # return(resultPlot)
}

plotFreqPolyByMetricToPngFile <- function(data, projectName, csvFileName, metricCode, xLabel, deepenForDesignRole) {
  techniqueList <- data$technique
  techniqueList <- unique(techniqueList)
  for (tech in techniqueList){
    if (tech != "") {
      # dataRefacoring <- filter(data, recordType == "Refactoring")
      # dataTechnique <- filter(data, technique == tech)
      dataTechnique <- filter(data, (technique == tech) | (recordType == "Refactoring") ) 
      fileSuffix <- paste0("-", tech, "-freqpoly-", metricCode, ".png")
      imgFileName <-sub("-plot.csv", fileSuffix, csvFileName)
      plotFreqPolyByTechniqueMetricToPngFile(dataTechnique, projectName, imgFileName, metricCode, xLabel, deepenForDesignRole);
    }
  }
}
