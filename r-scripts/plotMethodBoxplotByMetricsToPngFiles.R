rm(list = ls())

library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotMethodBoxplotByMetricsToPngFile-function.R", sep="", collapse=NULL))

setupWorkDir()

deepenForDesignRole <- FALSE

plotMethodBoxplotByMetricsFromDirToPngFiles(getWorkDir(), deepenForDesignRole)


