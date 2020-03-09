rm(list = ls())

library(rstudioapi)
scriptFilePath <- getActiveDocumentContext()$path
scriptFileDir <- dirname(scriptFilePath)

source(paste(scriptFileDir, "/plotClassDistributionByClassAndCommitToPngFiles.R", sep="", collapse=NULL))
source(paste(scriptFileDir, "/plotClassBoxplotByMetricsToPngFiles.R", sep="", collapse=NULL))
# source(paste(scriptFileDir, "/plotClassDensityByMetricsToPngFiles.R", sep="", collapse=NULL))
source(paste(scriptFileDir, "/plotClassFreqPolyByMetricsToPngFiles.R", sep="", collapse=NULL))

source(paste(scriptFileDir, "/plotMethodDistributionByMethodAndCommitToPngFiles.R", sep="", collapse=NULL))
source(paste(scriptFileDir, "/plotMethodBoxplotByMetricsToPngFiles.R", sep="", collapse=NULL))
source(paste(scriptFileDir, "/plotMethodDensityByMetricsToPngFiles.R", sep="", collapse=NULL))
source(paste(scriptFileDir, "/plot3dMethodDistribuitionByCcEcLocNopToHtmlFiles.R", sep="", collapse=NULL))

"Finished!"


