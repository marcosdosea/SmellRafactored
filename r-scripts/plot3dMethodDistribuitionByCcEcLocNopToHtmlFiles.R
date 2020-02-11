rm(list = ls())

library(rstudioapi)
scriptFilePath <- getActiveDocumentContext()$path
scriptFileDir <- dirname(scriptFilePath)
source(paste(scriptFileDir, "/plot3dMethodDistribuitionByCcEcLocNopToHtmlFile-function.R", sep="", collapse=NULL))

workDir <- paste(scriptFileDir, "/../../MiningStudies/refactoring", sep="", collapse=NULL)
setwd(workDir)

files <- list.files(path=workDir, pattern="-methods-plot.csv$", full.names=TRUE, recursive=FALSE)
lapply(files, function(x) {
  result = tryCatch({
    plot3dMethodDistribuitionByCcEcLocNopToHtmlFile(x)
  }, warning = function(warning_condition) {
    warning_condition
    # warnings()
  }, error = function(error_condition) {
    error_condition
    # rlang::last_error()
  })
})

