library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotClassDensityByclocToPngFile <- function(data, projectName, csvClassFileName) {
  typeValuesColors <- c("Smell" = "red1", "Ignored Smell" = "grey1", "Refactoring" = "blue1")
  resultPlot <- ggplot(data=data, aes(x=cloc, group=recordType)) +
    geom_density(aes(fill=recordType), alpha=0.3) +
    # theme_ipsum() +
    # ggtitle(projectName) +
    xlab("Lines of code") +
    scale_fill_manual("Legend:", values = typeValuesColors)
  # densityPlot
  imgFileName <-sub(".csv", "-density-loc.png", csvClassFileName)
  ggsave(imgFileName, plot = resultPlot, "png", path = NULL,
       scale = 1, width = NA, height = NA, units = "mm",
       dpi = 300, limitsize = FALSE)
  # return(densityPlot)
}


plotClassDensityByMetricsToPngFile <- function(csvClassFileName) {
  
  # csvClassFileName <- "Weasis-Class_Longa-A-18-15-22-7-19-23-21-2-9-8-17-classes-plot.csv"
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

  data$recordType[data$recordType == "Ignored Smell"] <- "Smell"
  data <- select(data, commitDateTime, className, cloc, recordType, techniques)
  data <- unique(data)

  data$cloc <-as.numeric(as.character(data$cloc))

  if (grepl("Class_Longa", csvClassFileName, fixed=TRUE)) {
    plotClassDensityByclocToPngFile(data, projectName, csvClassFileName)
  }

  # warnings()
}
