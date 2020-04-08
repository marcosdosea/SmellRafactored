library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-method.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotBoxplotByMetricsToPngFile-function.R", sep="", collapse=NULL))

setupWorkDir()

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotMethodBoxplotByMetricsToPngFile <- function(csvMethodFileName, deepenForDesignRole) {
  
  # deepenForDesignRole <- true
  # csvMethodFileName <- "aet-Alto_Acoplamento_Efferent-A-0-7-21-8-methods-plot.csv"
  # csvMethodFileName <- "aet-Metodo_Longo-A-0-7-21-8-methods-plot.csv"
  # csvMethodFileName <- "aet-Muitos_Desvios-A-0-21-methods-plot.csv"
  # csvMethodFileName <- "aet-Muitos_Parametros-A-0-21-26-methods-plot.csv"
  # csvMethodFileName <- "aet-Alto_Acoplamento_Efferent-A-0-7-21-8-methods-plot.csv"
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

  data$recordType[data$recordType == "Ignored Smell"] <- "Smell"
  data <- select(data, commitDateTime, className, methodName, cc, ec, loc, nop, recordType, technique)
  data <- unique(data)

  if (isFileOfHighComplexitySmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$cc))
    plotBoxplotByMetricToPngFile(data, projectName, csvMethodFileName, "cc", "Cyclomatic complexity", deepenForDesignRole)
  } else if (isFileOfHighEfferentCouplingSmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$ec))
    plotBoxplotByMetricToPngFile(data, projectName, csvMethodFileName, "ec", "Efferent coupling", deepenForDesignRole)
  } else if (isFileOfLongMethodSmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$loc))
    plotBoxplotByMetricToPngFile(data, projectName, csvMethodFileName, "loc", "Lines of code", deepenForDesignRole)
  } else if (isFileOfManyParametersSmell(csvMethodFileName)) {
    data$targetMetric <-as.numeric(as.character(data$nop))
    plotBoxplotByMetricToPngFile(data, projectName, csvMethodFileName, "nop", "Number of parameters", deepenForDesignRole)
  }

  # warnings()
}


plotMethodBoxplotByMetricsToPngFiles <- function(csvMethodFileNames, deepenForDesignRole) {
  lapply(csvMethodFileNames, function(csvMethodFileName) {
    executeFunctionWithCsvFileAndDeepenForDesignRole(plotMethodBoxplotByMetricsToPngFile, csvMethodFileName, deepenForDesignRole)
  })
}

plotMethodBoxplotByMetricsFromDirToPngFiles <- function(workDir, deepenForDesignRole) {
  csvMethodFileNames <- getMethodPlotCsvFiles(workDir)
  plotMethodBoxplotByMetricsToPngFiles(csvMethodFileNames, deepenForDesignRole)
}

