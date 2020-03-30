library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-method.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotFreqPolyByMetricsToPngFile-function.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotDensityByMetricsToPngFile-function.R", sep="", collapse=NULL))

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotMethodFreqPolyByMetricsToPngFile <- function(csvMethodFileName, deepenForDesignRole) {
  
  # csvMethodFileName <- "aet-...-plot.csv"
  fileIsEmpty <- (file.info(csvMethodFileName)$size == 0)
  if (fileIsEmpty) {
    print(paste("Empty file:", csvMethodFileName))
    return (false);
  }
  print(csvMethodFileName)
  projectName <- basename(csvMethodFileName)
  
  data <- read.csv(csvMethodFileName)
  data[data=="null"] <- NA
  data <- na.omit(data)
  
  data$recordType[data$recordType == "Ignored Smell"] <- "Smell"
  data <- select(data, commitDateTime, className, designRole, cc, ec, loc, nop, recordType, techniques)
  data <- unique(data)
  
  if (isFileOfHighComplexitySmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$cc))
    plotFreqPolyByMetricToPngFile(data, projectName, csvMethodFileName, "cc", "Cyclomatic complexity", deepenForDesignRole)
  } else if (isFileOfHighEfferentCouplingSmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$ec))
    plotFreqPolyByMetricToPngFile(data, projectName, csvMethodFileName, "ec", "Efferent coupling", deepenForDesignRole)
  } else if (isFileOfLongMethodSmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$loc))
    plotFreqPolyByMetricToPngFile(data, projectName, csvMethodFileName, "loc", "Lines of code", deepenForDesignRole)
  } else if (isFileOfManyParametersSmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$nop))
    plotFreqPolyByMetricToPngFile(data, projectName, csvMethodFileName, "nop", "Number of parameters", deepenForDesignRole)
  }
  
  
  # warnings()
}


plotMethodFreqPolyByMetricsToPngFiles <- function(csvMethodFileNames, deepenForDesignRole) {
  lapply(csvMethodFileNames, function(csvMethodFileName) {
    executeFunctionWithCsvFileAndDeepenForDesignRole(plotMethodFreqPolyByMetricsToPngFile, csvMethodFileName, deepenForDesignRole)
  })
}

plotMethodFreqPolyByMetricsitFromDirToPngFiles <- function(workDir, deepenForDesignRole) {
  csvMethodFileNames <- getMethodPlotCsvFiles(workDir)
  plotMethodFreqPolyByMetricsToPngFiles(csvMethodFileNames, deepenForDesignRole)
}

