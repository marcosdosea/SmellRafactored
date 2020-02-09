rm(list = ls())

library(rstudioapi)
scriptFilePath <- getActiveDocumentContext()$path
scriptFileDir <- dirname(scriptFilePath)
source(paste(scriptFileDir, "/plotClassDensityByMetricsToPngFile-function.R", sep="", collapse=NULL))

workDir <- paste(scriptFileDir, "/../../MiningStudies/refactoring", sep="", collapse=NULL)
setwd(workDir)

files <- list.files(path=workDir, pattern="-classes-plot.csv$", full.names=TRUE, recursive=FALSE)
lapply(files, function(x) {
  tryCatch({
    plotClassDensityByMetricsToPngFile(x)
  }, warning = function(warning_condition) {
    warning_condition
    # warnings()
  }, error = function(error_condition) {
    error_condition
    # rlang::last_error()
  })
})

