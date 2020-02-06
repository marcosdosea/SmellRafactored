library(rstudioapi)
scriptFilePath <- getActiveDocumentContext()$path
scriptFileDir <- dirname(scriptFilePath)
workDir <- paste(scriptFileDir, "/../../MiningStudies/refactoring", sep="", collapse=NULL)
setwd(workDir)



# files <- list.files(pattern="-classes-plot.csv")
# data_in <- lapply(files, read.csv)
# head(data_in[[1]])
# projectName <- "SmellRefactored"
#projectName <- "aet"
#projectName <- "libreplan"
#projectName <- "heritrix3"
projectName <- "bamboobsc"
##projectName <- "bigbluebutton"
#projectName <- "ice"
#projectName <- "kafka-webview"
#projectName <- "metl"
#projectName <- "openmpf"
#projectName <- "optaweb-vehicle-routing"
#projectName <- "phenotips"
#projectName <- "Weasis"
#projectName <- "webbudget"
csvFileName <- paste(projectName, "-refactoredAndNotRefactored-classes-plot.csv", sep="", collapse=NULL)

data <- read.csv(csvFileName)
data[data=="null"] <- NA
data <- na.omit(data)
data <- select(data, commitDateTime, className, cloc, isRefactoring, techniques)
data <- unique(data)

# data$commitDate <-as.numeric(as.character(data$commitDate))
data <- data[order(data$commitDateTime, data$className),]

library(ggplot2)
library(dplyr)
library(ggalt)
ggplot(data, aes(x=data$commitDateTime, y=data$className, color=data$isRefactoring)
) +
  geom_point(size=data$isRefactoring, alpha=0.3) +
  ggtitle(paste(projectName, " - classes by commit", sep="", collapse=NULL)) +
  xlab("First range commit and other commits prior to refactorings") + ylab("Classes") +
  labs(color = "Refactoring") +
  theme(axis.text.x = element_blank(), axis.text.y = element_blank())

warnings()
rlang::last_error()

imgFileName <- paste(projectName, "-refactoredAndNotRefactored-classes-plot.png", sep="", collapse=NULL)
ggsave(imgFileName, plot = last_plot(), "png", path = NULL,
       scale = 1, width = NA, height = NA, units = c("in", "cm", "mm"),
       dpi = 300, limitsize = TRUE)

