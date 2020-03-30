rm(list = ls())

library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-method.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plot3dMethodDistribuitionByCcEcLocNopToHtmlFile-function.R", sep="", collapse=NULL))

setupWorkDir()

files <- list.files(path=getWorkDir(), pattern="-methods-plot.csv$", full.names=TRUE, recursive=FALSE)
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

