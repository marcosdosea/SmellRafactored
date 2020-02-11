library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))

library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotClassDistributionByClassAndCommitToPngFile <- function(csvClassFileName) {
  
  # csvClassFileName <- "ice-Class_Longa-A-18-15-22-7-19-23-21-2-9-8-17-classes-plot.csv"
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

  data <- select(data, commitDateTime, className, cloc, recordType, techniques)
  data <- unique(data)
  
  #data$commitDate <-as.numeric(as.character(data$commitDate))
  data$cloc <-as.numeric(as.character(data$cloc))
  data <- data[order(data$commitDateTime, data$className),]

  if (length(data[, 1]) > 0) {
    resultPlot <- 
      ggplot(data, aes(x=data$commitDateTime, y=data$className)) +
      geom_point(aes(shape=recordType, colour=recordType, fill=recordType), alpha=0.3) +
      # labs(color = "Type") +
      labs(
        # title = projectName,
         # subtitle = "Classes by commit",
         # caption = "Only first commit with smells, commit with refactorings and their predecessors", 
         x = "Commits with observations", y = "Classes with observations"
         ) +
      theme(
        axis.text.x = element_blank()
        # axis.text.x = element_text(angle = 90, hjust = 1)
        , axis.text.y = element_blank()
        , legend.position = "bottom"
        ) +
      scale_colour_manual(getRecordTypeLegend(), values = getRecordTypeColors()) +  
      scale_fill_manual(getRecordTypeLegend(), values = getRecordTypeFills()) +  
      scale_shape_manual(getRecordTypeLegend(), values = getRecordTypeShapes())  

    imgFileName <-sub(".csv", "-DistribuitionByClassAndCommit.png", csvClassFileName)
    savePlotToPngFile(resultPlot, imgFileName)
  }
  #return (resultPlot)
}

