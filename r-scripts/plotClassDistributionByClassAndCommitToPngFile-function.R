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
  
  typeValuesColors <- c("Smell" = "red1", "Ignored Smell" = "grey1", "Refactoring" = "blue1")

  resultPlot <- 
    ggplot(data, aes(x=data$commitDateTime, y=data$className)) +
    geom_point(aes(colour=data$recordType), alpha=0.3) +
    # labs(color = "Type") +
    labs(
        # title = projectName,
         # subtitle = "Classes by commit",
         # caption = "Only first commit with smells, commit with refactorings and their predecessors", 
         x = "Commits with observations", y = "Classes with observations"
         , fill = "Legend") +
    theme(
      axis.text.x = element_blank()
      # axis.text.x = element_text(angle = 90, hjust = 1)
      , axis.text.y = element_blank()
      , legend.position = "bottom"
      ) +
    scale_colour_manual("Legend:", values = typeValuesColors)  


  imgFileName <-sub(".csv", "-DistribuitionByClassAndCommit.png", csvClassFileName)
  ggsave(imgFileName, plot = resultPlot, "png", path = NULL,
         scale = 1, width = NA, height = NA, units = c("in", "cm", "mm"),
         dpi = 300, limitsize = FALSE)
  #return (resultPlot)
}

