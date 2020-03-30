
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
      savePlotToPngFile(resultDrPlot, imgDrFileName)
    }
  }
  # return(resultDrPlot)
}


plotDensityByMetricToPngFile <- function(data, projectName, csvFileName, metricCode, xLabel, deepenForDesignRole) {
  recordTypes <- data$recordType
  recordTypes <- unique(recordTypes)
  if (length(data$commitDateTime) > 2) {
    resultPlot <- ggplot(data=data, aes(x=targetMetric, group=recordType)) +
      geom_density(aes(colour=recordType, fill=recordType), alpha=0.3) +
      # theme_ipsum() +
      # ggtitle(projectName) +
      xlab(xLabel) +
      scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
      scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
    fileSuffix <- paste0("-density-", metricCode, ".png")
    imgFileName <-sub(".csv", fileSuffix, csvFileName)
    savePlotToPngFile(resultPlot, imgFileName)
    # deepenForDesignRole <- TRUE
    if (deepenForDesignRole) {
      plotDensityByMetricDesignRoleToPngFile(data, projectName, imgFileName, metricCode, xLabel)
    }
  }
  # return(resultPlot)
}
