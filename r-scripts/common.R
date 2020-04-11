library(rstudioapi)


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

savePlotToPngFile <- function(plotToSave, imgFileName, imageScale) { # imageScale = 0.3
  print(basename(imgFileName))
  ggsave(imgFileName, plot = plotToSave, "png", path = NULL,
       scale = imageScale, width = NA, height = NA, units = "mm",
       dpi = 300, limitsize = FALSE)
}

