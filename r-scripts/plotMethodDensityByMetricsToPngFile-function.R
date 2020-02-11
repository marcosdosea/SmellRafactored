library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)


plotMethodDensityByCcToPngFile <- function(data, projectName, csvMethodFileName) {
  resultPlot <- ggplot(data=data, aes(x=cc, group=recordType)) +
    geom_density(aes(colour=recordType, fill=recordType), alpha=0.3) +
    # theme_ipsum() +
    # ggtitle(projectName) +
    xlab("Cyclomatic complexity") +
    scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
    scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
  # densityPlot
  imgFileName <-sub(".csv", "-density-cc.png", csvMethodFileName)
  savePlotToPngFile(resultPlot, imgFileName)
  # return(densityPlot)
}

plotMethodDensityByEcToPngFile <- function(data, projectName, csvMethodFileName) {
  resultPlot <- ggplot(data=data, aes(x=ec, group=recordType)) +
    geom_density(aes(colour=recordType, fill=recordType), alpha=0.3) +
    # theme_ipsum() +
    # ggtitle(projectName) +
    xlab("Efferent coupling") +
    scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
    scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
  # densityPlot
  imgFileName <-sub(".csv", "-density-ec.png", csvMethodFileName)
  savePlotToPngFile(resultPlot, imgFileName)
  # return(densityPlot)
}

plotMethodDensityByLocToPngFile <- function(data, projectName, csvMethodFileName) {
  resultPlot <- ggplot(data=data, aes(x=loc, group=recordType)) +
    geom_density(aes(colour=recordType, fill=recordType), alpha=0.3) +
    # theme_ipsum() +
    # ggtitle(projectName) +
    xlab("Lines of code") +
    scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
    scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
  # densityPlot
  imgFileName <-sub(".csv", "-density-loc.png", csvMethodFileName)
  savePlotToPngFile(resultPlot, imgFileName)
  # return(densityPlot)
}

plotMethodDensityByNopToPngFile <- function(data, projectName, csvMethodFileName) {
  resultPlot <- ggplot(data=data, aes(x=nop, group=recordType)) +
    geom_density(aes(colour=recordType, fill=recordType), alpha=0.3) +
    # theme_ipsum() +
    # ggtitle(projectName) +
    xlab("Number of parameters") +
    scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
    scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
  # densityNopPlot
  imgFileName <-sub(".csv", "-density-nop.png", csvMethodFileName)
  savePlotToPngFile(resultPlot, imgFileName)
  # return(densityNopPlot)
}


plotMethodDensityByMetricsToPngFile <- function(csvMethodFileName) {
  
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

  data$cc <-as.numeric(as.character(data$cc))
  data$ec <-as.numeric(as.character(data$ec))
  data$loc <-as.numeric(as.character(data$loc))
  data$nop <-as.numeric(as.character(data$nop))

  if (length(data[, 1]) > 2) {
    if (grepl("Muitos_Desvios", csvMethodFileName, fixed=TRUE)) {
      plotMethodDensityByCcToPngFile(data, projectName, csvMethodFileName)
    } else if (grepl("Alto_Acoplamento_Efferent", csvMethodFileName, fixed=TRUE)) {
      plotMethodDensityByEcToPngFile(data, projectName, csvMethodFileName)
    } else if (grepl("Metodo_Longo", csvMethodFileName, fixed=TRUE)) {
      plotMethodDensityByLocToPngFile(data, projectName, csvMethodFileName)
    } else if (grepl("Muitos_Parametros", csvMethodFileName, fixed=TRUE)) {
      plotMethodDensityByNopToPngFile(data, projectName, csvMethodFileName)
    }
  }

  # warnings()
}

