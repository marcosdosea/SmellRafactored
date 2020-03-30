

getClassPlotCsvFiles <- function(workDir) {
  files <- list.files(path=workDir, pattern="-classes-plot.csv$", full.names=TRUE, recursive=FALSE)
  return (files)
}

isFileOfLongClassSmell <- function(fileName) {
 return (grepl("Class_Longa", fileName, fixed=TRUE))
}