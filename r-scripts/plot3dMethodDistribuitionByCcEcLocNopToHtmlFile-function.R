library(rstudioapi)
source(paste(dirname(getActiveDocumentContext()$path), "/common.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-method.R", sep="", collapse=NULL))
source(paste(dirname(getActiveDocumentContext()$path), "/common-recordtype.R", sep="", collapse=NULL))

library(rgl)
library(dplyr)
library(rmarkdown)


plot3dMethodDistribuitionByTechniqueToHtmlFile <- function(dataTech, projectName, htmlFileName, xLabel, yLabel, zLabel) {
  mycolors <- c('red1', 'blue1', 'red1')
  dataTech$color <- mycolors[ as.factor(dataTech$recordType) ]

  if (length(dataTech[, 1]) > 0) {
    par(mar=c(0,0,0,0))
    plot3d( 
      x=dataTech$xAxis, y=dataTech$yAxis, z=dataTech$zAxis, 
      col = dataTech$color, 
      alpha = 0.3,
      type = 's', 
      radius = 3,
      xlab=xLabel, ylab=yLabel, zlab=zLabel
    )
    writeWebGL(filename=htmlFileName,  width=600, height=600)
  }
}


plot3dMethodDistribuitionByCcEcLocNopToHtmlFile <- function(csvFileName) {

  # csvFileName <- "openmrs-Metodo_Longo-ADRVX-0-7-21-8-methods-plot.csv"
  fileIsEmpty <- file.info(csvFileName)$size == 0
  if (fileIsEmpty) {
    print(paste("Empty file:", csvFileName))
    return ();
  }
  print(csvFileName)
  projectName <- basename(csvFileName)
  
  data <- read.csv(csvFileName, stringsAsFactors = TRUE)
  # data[data=="null"] <- NA
  # data <- na.omit(data)
  data <- unique(data)

  data <- select(data, loc, cc, ec, nop, recordType, technique)
  # data <- unique(data)

  xLabel <- "NoP" 
  data$xAxis <-as.numeric(as.character(data$nop))
  data$xAxis <- jitter(data$xAxis)
  yLabel <- "EC" 
  data$yAxis <-as.numeric(as.character(data$ec))
  data$yAxis <- jitter(data$yAxis)
  zLabel <- "LoC"
  data$zAxis <-as.numeric(as.character(data$loc))

  techniqueList <- data$technique
  techniqueList <- unique(techniqueList)
  for (tech in techniqueList){
    if (tech != "") {
      # dataRefacoring <- filter(data, recordType == getRecordTypeRefactoredKey())
      # dataTechnique <- filter(data, technique == tech)
      dataTechnique <- filter(data, (technique == tech) | (recordType == getRecordTypeRefactoredKey()) ) 
      fileSuffix <- paste0("-", tech, "-3dscatter", ".html")
      htmlFileName <-sub("-plot.csv", fileSuffix, csvFileName)
      plot3dMethodDistribuitionByTechniqueToHtmlFile(dataTechnique, projectName, htmlFileName, xLabel, yLabel, zLabel);
    }
  }
}
