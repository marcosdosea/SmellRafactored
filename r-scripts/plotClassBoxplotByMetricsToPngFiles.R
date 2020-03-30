rm(list = ls())

library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-class.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotClassBoxplotByMetricsToPngFile-function.R", sep="", collapse=NULL))

setupWorkDir()

deepenForDesignRole <- FALSE
plotClassBoxplotByMetricsFromDirToPngFiles(getWorkDir(), deepenForDesignRole)

