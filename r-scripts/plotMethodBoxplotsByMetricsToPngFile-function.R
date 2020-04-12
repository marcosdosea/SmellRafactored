library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-method.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-recordtype.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-technique.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotBoxplotsByMetricsToPngFile-function.R", sep="", collapse=NULL))

setupWorkDir()


library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotMethodBoxplotsByMetricsToPngFile <- function(csvMethodFileName, deepenForDesignRole) {
  
  # deepenForDesignRole <- FALSE
  # csvMethodFileName <- "aet-Method_Longa-A-18-15-22-7-19-21-2-9-8-17-Methods-plot.csv"
  # csvMethodFileName <- "openmrs-Metodo_Longo-ADRVX-0-7-21-8-methods-plot.csv"
  # csvMethodFileName <- "Weasis-Method_Longa-A-18-15-22-7-19-23-21-2-9-8-17-Methods-plot.csv"
  fileIsEmpty <- file.info(csvMethodFileName)$size == 0
  if (fileIsEmpty) {
    print(paste("Empty file:", csvMethodFileName))
    return ();
  }
  print(csvMethodFileName)
  projectName <- basename(csvMethodFileName)

  data <- read.csv(csvMethodFileName)
  # data[data=="null"] <- NA
  # data <- na.omit(data)
  data <- unique(data)
  
  # data$recordType[data$recordType == getRecordTypeIgnoredSmellKey()] <- getRecordTypeSmellKey()
  data$recordType[data$recordType == getRecordTypeIgnoredSmellKey()] <- NA
  data <- na.omit(data)
  data <- select(data, commitDateTime, className, methodName, cc, ec, loc, nop, recordType, technique)
  # data <- unique(data)
  
  data$technique <- factor(data$technique)
  # techniqueKeys <- getTechniqueKeys();
  # techniqueValues <- getTechniqueValues();
  # for (i in 1:length(techniqueKeys)) {
  #   key <- techniqueKeys[i]
  #   data$technique[data$technique == key] <- techniqueValues[i]
  # }

  if (isFileOfHighComplexitySmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$cc))
    plotBoxplotsByMetricToPngFile(data, projectName, csvMethodFileName, "cc", "Cyclomatic complexity", deepenForDesignRole)
  } else if (isFileOfHighEfferentCouplingSmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$ec))
    plotBoxplotsByMetricToPngFile(data, projectName, csvMethodFileName, "ec", "Efferent coupling", deepenForDesignRole)
  } else if (isFileOfLongMethodSmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$loc))
    plotBoxplotsByMetricToPngFile(data, projectName, csvMethodFileName, "loc", "Lines of code", deepenForDesignRole)
  } else if (isFileOfManyParametersSmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$nop))
    plotBoxplotsByMetricToPngFile(data, projectName, csvMethodFileName, "nop", "Number of parameters", deepenForDesignRole)
  }
  
  
  # warnings()
}


plotMethodBoxplotsByMetricsToPngFiles <- function(csvMethodFileNames, deepenForDesignRole) {
  lapply(csvMethodFileNames, function(csvMethodFileName) {
    executeFunctionWithCsvFileAndDeepenForDesignRole(plotMethodBoxplotsByMetricsToPngFile, csvMethodFileName, deepenForDesignRole)
  })
}

plotMethodBoxplotsByMetricsFromDirToPngFiles <- function(workDir, deepenForDesignRole) {
  csvMethodFileNames <- getMethodPlotCsvFiles(workDir)
  plotMethodBoxplotsByMetricsToPngFiles(csvMethodFileNames, deepenForDesignRole)
}
