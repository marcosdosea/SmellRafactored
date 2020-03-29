library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotClassFreqPolyByClocDesignRoleToPngFile <- function(data, projectName, imgFileName) {
  designRoles <- data$designRole
  designRoles <- unique(designRoles)
  for (dr in designRoles){
    # dataDr <- subset(data,designRole == dr) 
    # dataDr <- data[data$designRole==dr, ]
    dataDr <- filter(data, designRole == dr)
    if (length(dataDr$designRole) > 2) {
      print(dr)
      print(length(dataDr$designRole))
      resultDrPlot <- ggplot(dataDr, aes(x=cloc, group=recordType)) +
        geom_freqpoly(aes(colour=recordType), alpha=0.3, bins = 30, position = 'identity') + # , fill=recordType
        # theme_ipsum() +
        # ggtitle(projectName) +
        xlab("Lines of code") +
        scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
        scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
      drSuffix <- paste0("-", dr, ".png")
      imgDrFileName <-sub(".png", drSuffix, imgFileName)
      savePlotToPngFile(resultDrPlot, imgDrFileName)
    }
  }
  # return(densityPlot)
}
  
  

plotClassFreqPolyByClocToPngFile <- function(data, projectName, csvClassFileName) {
  resultPlot <- ggplot(data, aes(x=cloc, group=recordType)) +
    geom_freqpoly(aes(colour=recordType), alpha=0.3, bins = 30, position = 'identity') + # , fill=recordType
    # theme_ipsum() +
    # ggtitle(projectName) +
    xlab("Lines of code") +
    scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
    scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
  imgFileName <-sub(".csv", "-freqpoly-cloc.png", csvClassFileName)
  savePlotToPngFile(resultPlot, imgFileName)

  ### plotClassFreqPolyByClocDesignRoleToPngFile(data, projectName, imgFileName)
  
  # return(densityPlot)
}


plotClassFreqPolyByMetricsToPngFile <- function(csvClassFileName) {
  
  # csvClassFileName <- "aet-Class_Longa-D-18-15-22-7-19-21-2-9-8-17-classes-plot.csv"
  # csvClassFileName <- "Weasis-Class_Longa-A-18-15-22-7-19-23-21-2-9-8-17-classes-plot.csv"
  fileIsEmpty <- (file.info(csvClassFileName)$size == 0)
  if (fileIsEmpty) {
    print(paste("Empty file:", csvClassFileName))
    return (false);
  }
  print(csvClassFileName)
  projectName <- basename(csvClassFileName)

  data <- read.csv(csvClassFileName)
  data[data=="null"] <- NA
  data <- na.omit(data)

  data$recordType[data$recordType == "Ignored Smell"] <- "Smell"
  data <- select(data, commitDateTime, className, designRole, cloc, recordType, techniques)
  data <- unique(data)

  data$cloc <-as.numeric(as.character(data$cloc))

  if (length(data[, 1]) > 2) {
    if (grepl("Class_Longa", csvClassFileName, fixed=TRUE)) {
      plotClassFreqPolyByClocToPngFile(data, projectName, csvClassFileName)
    }
  }

  # warnings()
}
