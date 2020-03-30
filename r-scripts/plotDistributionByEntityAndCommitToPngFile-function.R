
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
      savePlotToPngFile(resultDrPlot, imgDrFileName)
    }
  }
  # return(resultDrPlot)
}


plotDistribuitionByEntityAndCommitToPngFile <- function(data, projectName, csvFileName, yLabel, deepenForDesignRole) {
  recordTypes <- data$recordType
  recordTypes <- unique(recordTypes)
  if (length(data$commitDateTime) > 0) {
    resultPlot <- 
      ggplot(data, aes(x=data$commitDateTime, y=data$entityName)) +
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
    fileSuffix <- paste0("-DistribuitionByEntityAndCommit", ".png")
    imgFileName <-sub(".csv", fileSuffix, csvFileName)
    savePlotToPngFile(resultPlot, imgFileName)
    # deepenForDesignRole <- TRUE
    if (deepenForDesignRole) {
      plotDistribuitionByEntityAndCommitDesignRoleToPngFile(data, projectName, imgFileName, yLabel)
    }
  }
  # return(resultPlot)
}
