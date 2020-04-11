library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-class.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-recordtype.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotDistributionByEntityAndCommitToPngFile-function.R", sep="", collapse=NULL))

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotClassDistributionByClassAndCommitToPngFile <- function(csvClassFileName, deepenForDesignRole) {
  
  # deepenForDesignRole <- FALSE
  # csvClassFileName <- "openmrs-Class_Longa-ADRVX-18-15-22-7-19-21-2-9-8-17-classes-plot.csv"
  # csvClassFileName <- "ice-Class_Longa-R-CONVERT_ANONYMOUS_CLASS_TO_TYPE-classes-plot.csv"
  fileIsEmpty <- file.info(csvClassFileName)$size == 0
  if (fileIsEmpty) {
    print(paste("Empty file:", csvClassFileName))
    return ();
  }
  print(csvClassFileName)
  projectName <- basename(csvClassFileName)
  
  data <- read.csv(csvClassFileName, stringsAsFactors = TRUE)
  data[data=="null"] <- NA
  data <- na.omit(data)

  data <- select(data, commitDateTime, className, cloc, recordType, technique, designRole)
  data <- unique(data)
  
  #data$commitDate <-as.numeric(as.character(data$commitDate))
  data$entityName <- data$className
  data <- data[order(data$commitDateTime, data$entityName),]

  plotDistribuitionByEntityAndCommitToPngFile(data, projectName, csvClassFileName, "Classes with observations", deepenForDesignRole)
}



plotClassDistributionByClassAndCommitToPngFiles <- function(csvClassFileNames, deepenForDesignRole) {
  lapply(csvClassFileNames, function(csvClassFileName) {
    executeFunctionWithCsvFileAndDeepenForDesignRole(plotClassDistributionByClassAndCommitToPngFile, csvClassFileName, deepenForDesignRole)
  })
}

plotClassDistributionByClassAndCommitFromDirToPngFiles <- function(workDir, deepenForDesignRole) {
  csvClassFileNames <- getClassPlotCsvFiles(workDir)
  plotClassDistributionByClassAndCommitToPngFiles(csvClassFileNames, deepenForDesignRole)
}

