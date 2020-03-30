rm(list = ls())

library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotMethodFreqPolyByMetricsToPngFile-function.R", sep="", collapse=NULL))

setupWorkDir()

deepenForDesignRole <- FALSE

plotMethodFreqPolyByMetricsitFromDirToPngFiles(getWorkDir(), deepenForDesignRole)
