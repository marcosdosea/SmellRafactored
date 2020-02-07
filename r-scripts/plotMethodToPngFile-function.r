library(ggplot2)
library(dplyr)
library(stringr) 
library(ggalt)

plotMethodToPngFile <- function(csvFileName) {
  # csvFileName <- "aet-Metodo_Longo-A-0-7-21-8-methods-plot.csv"
  fileIsEmpty <- file.info(csvFileName)$size == 0
  if (fileIsEmpty) {
    print(paste("Empty file:", csvFileName))
    return ();
  }
  print(csvFileName)
  projectName <- basename(csvFileName)
  
  data <- read.csv(csvFileName)
  data[data=="null"] <- NA
  data <- na.omit(data)
  
  data <- select(data, commitDateTime, className, methodName, loc, recordType, techniques)
  data <- unique(data)
  
  #data$commitDate <-as.numeric(as.character(data$commitDate))
  data$loc <-as.numeric(as.character(data$loc))
  data <- data[order(data$commitDateTime, data$className, data$methodName),]
  
  typeColors <- as.character(c("grey1", "blue1", "red1"))
  typeScale <- scale_colour_manual(name="Legend", values=typeColors)
  
  resultPlot <- 
    ggplot(data, aes(x=data$commitDateTime, y=data$methodName, color=data$recordType) ) +
    geom_point(alpha=0.3) +
    # labs(color = "Type") +
    labs(title = projectName,
         # subtitle = "Method by commit",
         # caption = "Only first commit with smells, commit with refactorings and their predecessors", 
         x = "Commits with observations", y = "Methods with observations"
         , fill = "Legend") +
    theme(
      axis.text.x = element_blank()
      , axis.text.y = element_blank()
    ) +
    typeScale
  
  imgFileName <-sub(".csv", ".png", csvFileName)
  ggsave(imgFileName, plot = resultPlot, "png", path = NULL,
         scale = 1, width = NA, height = NA, units = c("in", "cm", "mm"),
         dpi = 300, limitsize = FALSE)
  #return (resultPlot)
}
