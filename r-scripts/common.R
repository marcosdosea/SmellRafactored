


getRecordTypeLegend <- function() {
  return ("Legend:")
}
  
getRecordTypeShapes <- function() {
  return (c("Smell" = 19, "Ignored Smell" = 13, "Refactoring" = 17))
}

getRecordTypeColors <- function() {
  return (c("Smell" = "red1", "Ignored Smell" = "grey1", "Refactoring" = "blue1"))
}

getRecordTypeFills <- function() {
  return (c("Smell" = "red1", "Ignored Smell" = "grey1", "Refactoring" = "blue1"))
}

savePlotToPngFile <- function(plotToSave, imgFileName) {
  print(basename(imgFileName))
  ggsave(imgFileName, plot = plotToSave, "png", path = NULL,
       scale = 1, width = NA, height = NA, units = "mm",
       dpi = 300, limitsize = FALSE)
}

