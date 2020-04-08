
plotDistribuitionByEntityAndCommitDesignRoleToPngFile <- function(data, projectName, imgFileName, yLabel) {
  designRoles <- data$designRole
  designRoles <- unique(designRoles)
  for (dr in designRoles){
    dataDr <- filter(data, designRole == dr)
    if (length(dataDr$recordTypes) >= 1) {
      print(dr)
      print(length(dataDr$designRole))
      resultDrPlot <- 
        ggplot(dataDr, aes(x=dataDr$commitDateTime, y=dataDr$entityName)) +
        geom_point(aes(shape=recordType, colour=recordType, fill=recordType), alpha=0.3) +
        # labs(color = "Type") +
        labs(
          # title = projectName,
          # subtitle = "Entity by commit",
          # caption = "Only first commit with smells, commit with refactorings and their predecessors", 
          x = "Commits with observations", y = yLabel
        ) +
        theme(
          axis.text.x = element_blank()
          # axis.text.x = element_text(angle = 90, hjust = 1)
          , axis.text.y = element_blank()
          , legend.position = "bottom"
        ) +
        scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
        scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) +  
        scale_shape_manual(getRecordTypeLegend(), values = getRecordTypeShapes())  
      drSuffix <- paste0("-", dr, ".png")
      imgDrFileName <-sub(".png", drSuffix, imgFileName)
      savePlotToPngFile(resultDrPlot, imgDrFileName, 1)
    }
  }
  # return(resultDrPlot)
}

plotDistribuitionByTechniqueToPngFile <- function(dataTechnique, projectName, imgFileName, yLabel, deepenForDesignRole) {
  recordTypes <- dataTechnique$recordType
  recordTypes <- unique(recordTypes)
  if (length(dataTechnique$commitDateTime) > 0) {
    resultPlot <- 
      ggplot(dataTechnique, aes(x=dataTechnique$commitDateTime, y=dataTechnique$entityName)) +
      geom_point(aes(shape=recordType, colour=recordType, fill=recordType), alpha=0.3) +
      # labs(color = "Type") +
      labs(
        # title = projectName,
        # subtitle = "Entity by commit",
        # caption = "Only first commit with smells, commit with refactorings and their predecessors", 
        x = "Commits with observations", y = yLabel
      ) +
      theme(
        axis.text.x = element_blank()
        # axis.text.x = element_text(angle = 90, hjust = 1)
        , axis.text.y = element_blank()
        , legend.position = "bottom"
      ) +
      scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
      scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) +  
      scale_shape_manual(getRecordTypeLegend(), values = getRecordTypeShapes())  
    savePlotToPngFile(resultPlot, imgFileName, 1)
    # deepenForDesignRole <- TRUE
    if (deepenForDesignRole) {
      plotDistribuitionByEntityAndCommitDesignRoleToPngFile(dataTechnique, projectName, imgFileName, yLabel)
    }
  }
  # return(resultPlot)
}


plotDistribuitionByEntityAndCommitToPngFile <- function(data, projectName, csvFileName, yLabel, deepenForDesignRole) {
  techniqueList <- data$technique
  techniqueList <- unique(techniqueList)
  for (tech in techniqueList){
    if (tech != "") {
      # dataRefacoring <- filter(data, recordType == "Refactoring")
      # dataTechnique <- filter(data, technique == tech)
      dataTechnique <- filter(data, (technique == tech) | (recordType == "Refactoring") ) 
      fileSuffix <- paste0("-", tech, "-DistribuitionByEntityAndCommit", ".png")
      imgFileName <-sub("-plot.csv", fileSuffix, csvFileName)
      plotDistribuitionByTechniqueToPngFile(dataTechnique, projectName, imgFileName, yLabel, deepenForDesignRole);
    }
  }
}
