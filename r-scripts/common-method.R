

getMethodPlotCsvFiles <- function(workDir) {
  files <- list.files(path=workDir, pattern="-methods-plot.csv$", full.names=TRUE, recursive=FALSE)
  return (files)
}


isFileOfHighComplexitySmell <- function(fileName) {
  return (grepl("Muitos_Desvios", fileName, fixed=TRUE))
}

isFileOfHighEfferentCouplingSmell <- function(fileName) {
  return (grepl("Alto_Acoplamento_Efferent", fileName, fixed=TRUE))
}

isFileOfLongMethodSmell <- function(fileName) {
  return (grepl("Metodo_Longo", fileName, fixed=TRUE))
}
  
isFileOfManyParametersSmell <- function(fileName) {
  return (grepl("Muitos_Parametros", fileName, fixed=TRUE))
}
