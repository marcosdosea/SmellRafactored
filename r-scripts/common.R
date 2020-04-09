library(rstudioapi)

getTechniqueKeys <- function() {
  return (c("A", "X", "D", "R", "V"))
}

getTechniqueValues <- function() {
  return (
    c(
      "Alves 2010"
      , "Aniche 2016"
      , "Dosea 2018"
      , "Dosea 2016"
      , "Vale 2015"
      )
    )
}

getTechniqueLabels <- function() {
  return (
    c(
      "A" = "Alves 2010"
      , "X" = "Aniche 2016"
      , "D" = "Dosea 2018"
      , "R" = "Dosea 2016"
      , "V" = "Vale 2015"
      )
    )
}

getScriptFileDir <- function() {
  scriptFilePath <- getActiveDocumentContext()$path
  scriptFileDir <- dirname(scriptFilePath)
  return (scriptFileDir)
}

getWorkDir <- function() {
  scriptFileDir <- getScriptFileDir();
  return (paste(scriptFileDir, "/../../MiningStudies/refactoring", sep="", collapse=NULL))
}

setupWorkDir <- function() {
  workDir <- getWorkDir()
  setwd(workDir)
}

executeFunctionWithCsvFileAndDeepenForDesignRole <- function(functionName, csvFileName, deepenForDesignRole) {
  tryCatch({
    functionName(csvFileName, deepenForDesignRole)
  }, warning = function(warning_condition) {
    warning_condition
    # warnings()
  }, error = function(error_condition) {
    error_condition
    # rlang::last_error()
  })
}


getGenericLegend <- function() {
  return ("Legend:")
}

getRecordTypeLegend <- function() {
  return ("Record Types:")
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

getTechniqueLegend <- function() {
  return ("Techniques:")
}

getTechniqueColors <- function() {
  return (c(" " = "blue", "A" = "red", "X" = "green", "D" = "yellow", "R" = "orange", "V" = "purple"))
}

getTechniqueFills <- function() {
  return (getTechniqueColors())
}

savePlotToPngFile <- function(plotToSave, imgFileName, imageScale) { # imageScale = 0.3
  print(basename(imgFileName))
  ggsave(imgFileName, plot = plotToSave, "png", path = NULL,
       scale = imageScale, width = NA, height = NA, units = "mm",
       dpi = 300, limitsize = FALSE)
}

