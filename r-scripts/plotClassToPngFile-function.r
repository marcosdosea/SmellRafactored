library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotClassToPngFile <- function(csvClassFileName) {
  # csvClassFileName <- "aet-Class_Longa-A-18-15-22-7-19-23-21-2-9-8-17-classes-plot.csv"
  # csvClassFileName <- "aet-Class_Longa-A-CONVERT_ANONYMOUS_CLASS_TO_TYPE-classes-plot.csv"
  fileIsEmpty <- file.info(csvClassFileName)$size == 0
  if (fileIsEmpty) {
    print(paste("Empty file:", csvClassFileName))
    return ();
  }
  print(csvClassFileName)
  projectName <- basename(csvClassFileName)
  
  data <- read.csv(csvClassFileName)
  data[data=="null"] <- NA
  data <- na.omit(data)

  data <- select(data, commitDateTime, className, cloc, recordType, techniques)
  data <- unique(data)
  
  #data$commitDate <-as.numeric(as.character(data$commitDate))
  data$cloc <-as.numeric(as.character(data$cloc))
  data <- data[order(data$commitDateTime, data$className),]
  
  typeColors <- as.character(c("grey1", "blue1", "red1"))
  typeScale <- scale_colour_manual(name="Legend", values=typeColors)
  
  #typeColors <- as.character(c("red1", "grey1", "blue1"))
  #recordTypes <- as.character(c("Smell", "Smell Ignored", "Refactoring"))
  #typeScale <- scale_colour_manual(name=recordTypes, values=typeColors)
  #typeFill <- scale_fill_manual(values = c("red1", "grey1", "blue1"),
  #                  labels = c("Smell", "Smell Ignored", "Refactoring"), 
  #                  drop = FALSE)
  
  
  resultPlot <- 
    ggplot(data, aes(x=data$commitDateTime, y=data$className, color=data$recordType)) +
    geom_point(alpha=0.3) +
    # labs(color = "Type") +
    labs(title = projectName,
         # subtitle = "Classes by commit",
         # caption = "Only first commit with smells, commit with refactorings and their predecessors", 
         x = "Commits with observations", y = "Classes with observations"
         , fill = "Legend") +
    theme(
      axis.text.x = element_blank()
      , axis.text.y = element_blank()
      ) +
    # typeFill
    typeScale
  

  imgFileName <-sub(".csv", ".png", csvClassFileName)
  ggsave(imgFileName, plot = resultPlot, "png", path = NULL,
         scale = 1, width = NA, height = NA, units = c("in", "cm", "mm"),
         dpi = 300, limitsize = FALSE)
  #return (resultPlot)
}
