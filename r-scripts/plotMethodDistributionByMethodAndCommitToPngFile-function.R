library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-method.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotDistributionByEntityAndCommitToPngFile-function.R", sep="", collapse=NULL))

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotMethodDistributionByMethodAndCommitToPngFile <- function(csvMethodFileName, deepenForDesignRole) {

  # csvMethodFileName <- "aet-Metodo_Longo-A-0-7-21-8-methods-plot.csv"
  # csvMethodFileName <- "Weasis-Alto_Acoplamento_Efferent-D-EXTRACT_AND_MOVE_OPERATION-methods-plot.csv"
  fileIsEmpty <- file.info(csvMethodFileName)$size == 0
  if (fileIsEmpty) {
    print(paste("Empty file:", csvMethodFileName))
    return ();
  }
  print(csvMethodFileName)
  projectName <- basename(csvMethodFileName)
  
  data <- read.csv(csvMethodFileName)
  data[data=="null"] <- NA
  data <- na.omit(data)
  
  data <- select(data, commitDateTime, className, methodName, loc, recordType, techniques, designRole)
  data <- unique(data)
  
  # data$commitDate <-as.numeric(as.character(data$commitDate))
  # data$loc <-as.numeric(as.character(data$loc))
  data$entityName <- paste0(data$className, ".", data$methodName)
  data <- data[order(data$commitDateTime, data$entityName),]
  
  plotDistribuitionByEntityAndCommitToPngFile(data, projectName, csvMethodFileName, "Methods with observations", deepenForDesignRole)

}


plotMethodDistributionByMethodAndCommitToPngFiles <- function(csvMethodFileNames, deepenForDesignRole) {
  lapply(csvMethodFileNames, function(csvMethodFileName) {
    executeFunctionWithCsvFileAndDeepenForDesignRole(plotMethodDistributionByMethodAndCommitToPngFile, csvMethodFileName, deepenForDesignRole)
  })
}

plotMethodDistributionByMethodAndCommitFromDirToPngFiles <- function(workDir, deepenForDesignRole) {
  csvMethodFileNames <- getMethodPlotCsvFiles(workDir)
  plotMethodDistributionByMethodAndCommitToPngFiles(csvMethodFileNames, deepenForDesignRole)
}

