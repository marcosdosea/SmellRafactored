rm(list = ls())

library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/plotClassDistributionByClassAndCommitToPngFile-function.R", sep="", collapse=NULL))


setupWorkDir()

deepenForDesignRole <- FALSE
plotClassDistributionByClassAndCommitFromDirToPngFiles(getWorkDir(), deepenForDesignRole)
