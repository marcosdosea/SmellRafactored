rm(list = ls())

library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
setupWorkDir()


source(paste(dirname(getActiveDocumentContext()$path), "/plotClassDistributionByClassAndCommitToPngFiles.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotClassBoxplotByMetricsToPngFiles.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotClassBoxplotsByMetricsToPngFiles.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotClassFreqPolyByMetricsToPngFiles.R", sep="", collapse=NULL))
# source(paste(dirname(getActiveDocumentContext()$path), "/plotClassDensityByMetricsToPngFiles.R", sep="", collapse=NULL))

source(paste(dirname(getActiveDocumentContext()$path), "/plotMethodDistributionByMethodAndCommitToPngFiles.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotMethodBoxplotByMetricsToPngFiles.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotMethodBoxplotsByMetricsToPngFiles.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotMethodFreqPolyByMetricsToPngFiles.R", sep="", collapse=NULL))
# source(paste(dirname(getActiveDocumentContext()$path), "/plotMethodDensityByMetricsToPngFiles.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plot3dMethodDistribuitionByCcEcLocNopToHtmlFiles.R", sep="", collapse=NULL))

"Finished!"


