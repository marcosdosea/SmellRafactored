library(rstudioapi)
scriptFilePath <- getActiveDocumentContext()$path
scriptFileDir <- dirname(scriptFilePath)
source(paste(scriptFileDir, "/plot3dMethodToHtmlFile-function.r", sep="", collapse=NULL))

workDir <- paste(scriptFileDir, "/../../MiningStudies/refactoring", sep="", collapse=NULL)
setwd(workDir)

classFiles <- list.files(path=workDir, pattern="-methods-plot.csv$", full.names=TRUE, recursive=FALSE)
lapply(classFiles, function(x) {
  result = tryCatch({
      plot3MethodToHtmlFile(x)
  }, warning = function(warning_condition) {
    warning_condition
    warnings()
  }, error = function(error_condition) {
    error_condition
    rlang::last_error()
  })
})
