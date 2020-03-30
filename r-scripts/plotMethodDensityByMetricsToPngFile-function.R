library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-method.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotDensityByMetricsToPngFile-function.R", sep="", collapse=NULL))

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)


plotMethodDensityByMetricsToPngFile <- function(csvMethodFileName, deepenForDesignRole) {
  
  # csvMethodFileName <- "aet-Metodo_Longo-A-0-7-21-8-methods-plot.csv"
  # csvMethodFileName <- "Weasis-Alto_Acoplamento_Efferent-A-0-7-21-8-methods-plot.csv"
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
  data <- select(data, commitDateTime, className, methodName, cc, ec, loc, nop, recordType, techniques)
  data <- unique(data)

  if (length(data[, 1]) > 2) {
    if (isFileOfHighComplexitySmell(csvMethodFileName)) {
      data$targetMetric <-as.numeric(as.character(data$cc))
      plotDensityByMetricToPngFile(data, projectName, csvMethodFileName, "cc", "Cyclomatic complexity", deepenForDesignRole)
    } else if (isFileOfHighEfferentCouplingSmell(csvMethodFileName)) {
      data$targetMetric <-as.numeric(as.character(data$ec))
      plotDensityByMetricToPngFile(data, projectName, csvMethodFileName, "ec", "Efferent coupling", deepenForDesignRole)
    } else if (isFileOfLongMethodSmell(csvMethodFileName)) {
      data$targetMetric <-as.numeric(as.character(data$loc))
      plotDensityByMetricToPngFile(data, projectName, csvMethodFileName, "loc", "Lines of code", deepenForDesignRole)
    } else if (isFileOfManyParametersSmell(csvMethodFileName)) {
      data$targetMetric <-as.numeric(as.character(data$nop))
      plotDensityByMetricToPngFile(data, projectName, csvMethodFileName, "nop", "Number of parameters", deepenForDesignRole)
    }
  }

  # warnings()
}



plotMethodDensityByCcToPngFiles <- function(csvMethodFileNames, deepenForDesignRole) {
  lapply(csvMethodFileNames, function(csvMethodFileName) {
    executeFunctionWithCsvFileAndDeepenForDesignRole(plotMethodDensityByMetricsToPngFile, csvMethodFileName, deepenForDesignRole)
  })
}

plotMethodDensityByMetricsFromDirToPngFiles <- function(workDir, deepenForDesignRole) {
  csvMethodFileNames <- getMethodPlotCsvFiles(workDir)
  plotMethodDensityByCcToPngFiles(csvMethodFileNames, deepenForDesignRole)
}


