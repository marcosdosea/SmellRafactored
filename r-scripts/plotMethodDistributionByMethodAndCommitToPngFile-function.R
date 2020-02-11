library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotMethodDistributionByMethodAndCommitToPngFile <- function(csvMethodFileName) {

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
  
  data <- select(data, commitDateTime, className, methodName, loc, recordType, techniques)
  data <- unique(data)
  
  #data$commitDate <-as.numeric(as.character(data$commitDate))
  data$loc <-as.numeric(as.character(data$loc))
  data$classMethodName <- paste0(data$className, ".", data$methodName)
  data <- data[order(data$commitDateTime, data$classMethodName),]
  
  if (length(data[, 1]) > 0) {
    resultPlot <- 
      ggplot(data, aes(x=data$commitDateTime, y=data$classMethodName) ) +
      geom_point(aes(shape=recordType, colour=recordType, fill=recordType), alpha=0.3) +
      # labs(color = "Type") +
      labs(
         # title = projectName,
         # subtitle = "Method by commit",
         # caption = "Only first commit with smells, commit with refactorings and their predecessors", 
         x = "Commits with observations", y = "Methods with observations"
         ) +
      theme(
        axis.text.x = element_blank()
        , axis.text.y = element_blank()
        , legend.position = "bottom"
      ) +
      scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
      scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) +  
      scale_shape_manual(getRecordTypeLegend(), values = getRecordTypeShapes())  
  
    imgFileName <-sub(".csv", "-DistribuitionByMethodAndCommit.png", csvMethodFileName)
    savePlotToPngFile(resultPlot, imgFileName)
  }
  #return (resultPlot)
}
