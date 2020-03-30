library(rstudioapi)



getTechniques <- function() {
  return (
    c(
      "A" = "Alves 2010"
      , "X" = "Aniche 2016"
      , "D" = "Dósea 2018"
      , "R" = "Dósea 2016"
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
       scale = 0.3, width = NA, height = NA, units = "mm",
       dpi = 300, limitsize = FALSE)
}

