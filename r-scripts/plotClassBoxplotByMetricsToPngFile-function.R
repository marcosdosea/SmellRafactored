library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotClassBoxplotByClocDesignRoleToPngFile <- function(data, projectName, imgFileName) {
  designRoles <- data$designRole
  designRoles <- unique(designRoles)
  for (dr in designRoles){
    dataDr <- filter(data, designRole == dr)
    if (length(dataDr$designRole) > 0) {
      print(dr)
      print(length(dataDr$designRole))
      resultDrPlot <- ggplot(dataDr, aes(x=recordType, y=cloc, group=recordType)) +
        geom_boxplot(aes(colour=recordType, fill=recordType), alpha=0.3) +
        # theme_ipsum() +
        # ggtitle(projectName) +
        xlab("") +
        ylab("Lines of code") +
        theme(legend.position="none") +
        scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
        scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
      drSuffix <- paste0("-", dr, ".png")
      imgDrFileName <-sub(".png", drSuffix, imgFileName)
      savePlotToPngFile(resultDrPlot, imgDrFileName)
    }
  }
  # return(densityPlot)
}
  
  

plotClassBoxplotByClocToPngFile <- function(data, projectName, csvClassFileName) {
  resultPlot <- ggplot(data, aes(x=recordType, y=cloc, group=recordType)) +
    geom_boxplot(aes(colour=recordType, fill=recordType), alpha=0.3) +
    # theme_ipsum() +
    # ggtitle(projectName) +
    xlab("") +
    ylab("Lines of code") +
    theme(legend.position="none") +
    scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
    scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
  imgFileName <-sub(".csv", "-boxplot-cloc.png", csvClassFileName)
  savePlotToPngFile(resultPlot, imgFileName)

  # plotClassBoxplotByClocDesignRoleToPngFile(data, projectName, imgFileName)
  
  # return(densityPlot)
}


plotClassBoxplotByMetricsToPngFile <- function(csvClassFileName) {
  
  # csvClassFileName <- "aet-Class_Longa-D-18-15-22-7-19-23-21-2-9-8-17-classes-plot.csv"
  # csvClassFileName <- "Weasis-Class_Longa-A-18-15-22-7-19-23-21-2-9-8-17-classes-plot.csv"
  fileIsEmpty <- file.info(csvClassFileName)$size == 0
  if (fileIsEmpty) {
    print(paste("Empty file:", csvClassFileName))
    return ();
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

  recordTypes <- data$recordType
  recordTypes <- unique(recordTypes)
  if (length(recordTypes) >= 2) { # (length(data[, 1]) > 0)
    if (grepl("Class_Longa", csvClassFileName, fixed=TRUE)) {
      plotClassBoxplotByClocToPngFile(data, projectName, csvClassFileName)
    }
  }

  # warnings()
}
