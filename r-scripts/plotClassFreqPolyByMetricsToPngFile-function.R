library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-class.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-recordtype.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotFreqPolyByMetricsToPngFile-function.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotDensityByMetricsToPngFile-function.R", sep="", collapse=NULL))

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotClassFreqPolyByMetricsToPngFile <- function(csvClassFileName, deepenForDesignRole) {
  
  # deepenForDesignRole <- FALSE
  # csvClassFileName <- "openmrs-Class_Longa-ADRVX-18-15-22-7-19-21-2-9-8-17-classes-plot.csv"
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
  data <- select(data, commitDateTime, className, designRole, cloc, recordType, technique)
  data <- unique(data)

  if (isFileOfLongClassSmell(csvClassFileName)) {
    data$targetMetric <-as.numeric(as.character(data$cloc))
    plotFreqPolyByMetricToPngFile(data, projectName, csvClassFileName, "cloc", "Lines of code", deepenForDesignRole)
  }

  # warnings()
}


plotClassFreqPolyByMetricsToPngFiles <- function(csvClassFileNames, deepenForDesignRole) {
  lapply(csvClassFileNames, function(csvClassFileName) {
    executeFunctionWithCsvFileAndDeepenForDesignRole(plotClassFreqPolyByMetricsToPngFile, csvClassFileName, deepenForDesignRole)
  })
}

plotClassFreqPolyByMetricsFromDirToPngFiles <- function(workDir, deepenForDesignRole) {
  csvClassFileNames <- getClassPlotCsvFiles(workDir)
  plotClassFreqPolyByMetricsToPngFiles(csvClassFileNames, deepenForDesignRole)
}

