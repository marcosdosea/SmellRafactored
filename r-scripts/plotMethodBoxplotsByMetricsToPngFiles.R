rm(list = ls())

library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-method.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotMethodBoxplotsByMetricsToPngFile-function.R", sep="", collapse=NULL))

setupWorkDir()

deepenForDesignRole <- FALSE
plotMethodBoxplotsByMetricsFromDirToPngFiles(getWorkDir(), deepenForDesignRole)

