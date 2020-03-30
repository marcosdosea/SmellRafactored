
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
        scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
        scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
      drSuffix <- paste0("-", dr, ".png")
      imgDrFileName <- sub(".png", drSuffix, imgFileName)
      print(imgDrFileName)
      savePlotToPngFile(resultDrPlot, imgDrFileName)
    }
  }
  # return(resultDrPlot)
}


plotFreqPolyByMetricToPngFile <- function(data, projectName, csvFileName, metricCode, xLabel, deepenForDesignRole) {
  if (length(data$commitDateTime) > 2) {
    resultPlot <- ggplot(data=data, aes(x=targetMetric, group=recordType)) +
      geom_freqpoly(aes(colour=recordType), alpha=0.3, bins = 30, position = 'identity') + # , fill=recordType
      # theme_ipsum() +
      # ggtitle(projectName) +
      xlab(xLabel) +
      scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
      scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
    fileSuffix <- paste0("-freqpoly-", metricCode, ".png")
    imgFileName <-sub(".csv", fileSuffix, csvFileName)
    savePlotToPngFile(resultPlot, imgFileName)
    # deepenForDesignRole <- TRUE
    if (deepenForDesignRole) {
      plotFreqPolyByMetricDesignRoleToPngFile(data, projectName, imgFileName, metricCode, xLabel)
    }
  }
  # return(resultPlot)
}
