# This procedure fixes a bug in ggplo2 that generates empty files.
# Just run successfully once, close and reopen RStudio.
require(devtools)
require(installr)
install.Rtools(choose_version = TRUE, check = FALSE, GUI = TRUE,
               page_with_download_url = "https://cran.r-project.org/bin/windows/Rtools/")
install_version("ggplot2", version = "2.2.1", repos = "http://cran.us.r-project.org")
