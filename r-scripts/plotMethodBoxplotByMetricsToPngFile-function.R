library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)


plotMethodBoxplotByCcToPngFile <- function(data, projectName, csvMethodFileName) {
  resultPlot <- ggplot(data=data, aes(x=recordType, y=cc, group=recordType)) +
    geom_boxplot(aes(colour=recordType, fill=recordType), alpha=0.3) +
    # theme_ipsum() +
    # ggtitle(projectName) +
    xlab("") +
    ylab("Cyclomatic complexity") +
    theme(legend.position="none") +
    scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
    scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
  # densityPlot
  imgFileName <-sub(".csv", "-boxplot-cc.png", csvMethodFileName)
  savePlotToPngFile(resultPlot, imgFileName)
  # return(densityPlot)
}

plotMethodBoxplotByEcToPngFile <- function(data, projectName, csvMethodFileName) {
  resultPlot <- ggplot(data=data, aes(x=recordType, y=ec, group=recordType)) +
    geom_boxplot(aes(colour=recordType, fill=recordType), alpha=0.3) +
    # theme_ipsum() +
    # ggtitle(projectName) +
    xlab("") +
    ylab("Efferent coupling") +
    theme(legend.position="none") +
    scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
    scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
  # densityPlot
  imgFileName <-sub(".csv", "-boxplot-ec.png", csvMethodFileName)
  savePlotToPngFile(resultPlot, imgFileName)
  # return(densityPlot)
}

plotMethodBoxplotByLocToPngFile <- function(data, projectName, csvMethodFileName) {
  resultPlot <- ggplot(data=data, aes(x=recordType, y=loc, group=recordType)) +
    geom_boxplot(aes(colour=recordType, fill=recordType), alpha=0.3) +
    # theme_ipsum() +
    # ggtitle(projectName) +
    xlab("") +
    ylab("Lines of code") +
    theme(legend.position="none") +
    scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
    scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
  # densityPlot
  imgFileName <-sub(".csv", "-boxplot-loc.png", csvMethodFileName)
  savePlotToPngFile(resultPlot, imgFileName)
  # return(densityPlot)
}

plotMethodBoxplotByNopToPngFile <- function(data, projectName, csvMethodFileName) {
  resultPlot <- ggplot(data=data, aes(x=recordType, y=nop, group=recordType)) +
    geom_boxplot(aes(colour=recordType, fill=recordType), alpha=0.3) +
    # theme_ipsum() +
    # ggtitle(projectName) +
    xlab("") +
    ylab("Number of parameters") +
    theme(legend.position="none") +
    scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
    scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) 
  # densityNopPlot
  imgFileName <-sub(".csv", "-boxplot-nop.png", csvMethodFileName)
  savePlotToPngFile(resultPlot, imgFileName)
  # return(densityNopPlot)
}


plotMethodBoxplotByMetricsToPngFile <- function(csvMethodFileName) {
  
  # csvMethodFileName <- "aet-Metodo_Longo-A-0-7-21-8-methods-plot.csv"
  # MethodFileName <- "Weasis-Alto_Acoplamento_Efferent-A-0-7-21-8-methods-plot.csv"
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

  recordTypes <- data$recordType
  recordTypes <- unique(recordTypes)
  if (length(recordTypes) >= 2) { # (length(data[, 1]) > 0)
    if (grepl("Muitos_Desvios", csvMethodFileName, fixed=TRUE)) {
      plotMethodBoxplotByCcToPngFile(data, projectName, csvMethodFileName)
    } else if (grepl("Alto_Acoplamento_Efferent", csvMethodFileName, fixed=TRUE)) {
      plotMethodBoxplotByEcToPngFile(data, projectName, csvMethodFileName)
    } else if (grepl("Metodo_Longo", csvMethodFileName, fixed=TRUE)) {
      plotMethodBoxplotByLocToPngFile(data, projectName, csvMethodFileName)
    } else if (grepl("Muitos_Parametros", csvMethodFileName, fixed=TRUE)) {
      plotMethodBoxplotByNopToPngFile(data, projectName, csvMethodFileName)
    }
  }

  # warnings()
}

