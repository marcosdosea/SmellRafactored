rm(list = ls())

library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotMethodDensityByMetricsToPngFile-function.R", sep="", collapse=NULL))

setupWorkDir()

deepenForDesignRole <- FALSE

plotMethodDensityByMetricsFromDirToPngFiles(getWorkDir(), deepenForDesignRole)


