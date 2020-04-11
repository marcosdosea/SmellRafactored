

getTechniqueKeys <- function() {
  return (c("A", "X", "R", "D", "V"))
}

getTechniqueValues <- function() {
  return (
    c(
      "Alves (2010)"
      , "Aniche (2016)"
      , "Dosea (2016)"
      , "Dosea (2018)"
      , "Vale (2015)"
    )
  )
}

getTechniqueLabels <- function() {
  return (
    c(
      "A" = "Alves (2010)"
      , "X" = "Aniche (2016)"
      , "R" = "Dosea (2016)"
      , "D" = "Dosea (2018)"
      , "V" = "Vale (2015)"
    )
  )
}

getTechniqueLegend <- function() {
  return ("Techniques:")
}

getTechniqueColors <- function() {
  # return (c(" " = "blue", "A" = "red", "X" = "green", "R" = "orange", "D" = "yellow", "V" = "purple"))
  return (
    c(
      " " = "blue"
      , "Alves (2010)" = "red"
      , "Aniche (2016)" = "green"
      , "Dosea (2016)" = "orange"
      , "Dosea (2018)" = "yellow"
      , "Vale (2015)" = "purple"
    )
  )
}

getTechniqueFills <- function() {
  return (getTechniqueColors())
}
