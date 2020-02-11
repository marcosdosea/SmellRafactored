library(rgl)
library(dplyr)
library(rmarkdown)

plot3dMethodDistribuitionByCcEcLocNopToHtmlFile <- function(csvMethodFileName) {
  # csvMethodFileName <- "aet-Metodo_Longo-D-0-7-21-8-methods-plot.csv"
  fileIsEmpty <- file.info(csvMethodFileName)$size == 0
  if (fileIsEmpty) {
    print(paste("Empty file:", csvMethodFileName))
    return ();
  }
  print(csvMethodFileName)
  projectName <- basename(csvMethodFileName)
  
  data <- read.csv(csvMethodFileName, stringsAsFactors = TRUE)
  data[data=="null"] <- NA
  data <- na.omit(data)
  data <- select(data, loc, cc, ec, nop, recordType)
  data <- unique(data)
  
  data$loc <-as.numeric(as.character(data$loc))
  data$cc <-as.numeric(as.character(data$cc))
  data$ec <-as.numeric(as.character(data$ec))
  data$nop <-as.numeric(as.character(data$nop))
  
  data$ec <- jitter(data$ec)
  data$nop <- jitter(data$nop)
  

  # Add a new column with color
  # typeValuesColors <- c("Smell" = "red1", "Ignored Smell" = "grey1", "Refactoring" = "blue1")
  # typeValuesColors <- c("Smell" = "red1", "Ignored Smell" = "grey1", "Refactoring" = "blue1")
  mycolors <- c('red1', 'blue1', 'red1')
  data$color <- mycolors[ as.factor(data$recordType) ]
  ## mycolors <- c('royalblue1', 'darkcyan', 'oldlace')
  ## data$color <- mycolors[ as.numeric(data$recordType) ]
  # data$color <- typeValuesColors[as.numeric(data$recordType)]
  
  if (length(data[, 1]) > 0) {
    par(mar=c(0,0,0,0))
    plot3d( 
      x=data$nop, y=data$ec, z=data$loc, 
      col = data$color, 
      alpha = 0.3,
      type = 's', 
      radius = 3,
      xlab="nop", ylab="ec", zlab="loc"
      )
    htmlFileName <-sub(".csv", "-3dscatter.html", csvMethodFileName)
    writeWebGL(filename=htmlFileName,  width=600, height=600)
  }

}
