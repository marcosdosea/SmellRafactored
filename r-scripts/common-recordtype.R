

getRecordTypeRefactoredKey  <- function() {
  return ("Refactored")
}
getRecordTypeSmellKey  <- function() {
  return ("Smell")
}
getRecordTypeIgnoredSmellKey  <- function() {
  return ("Ignored Smell")
}

getRecordTypeShapes <- function() {
  return (c("Smell" = 19, "Ignored Smell" = 13, "Refactored" = 17))
}

getRecordTypeColors <- function() {
  return (c("Smell" = "red1", "Ignored Smell" = "grey1", "Refactored" = "blue1"))
}

getRecordTypeFills <- function() {
  return (c("Smell" = "red1", "Ignored Smell" = "grey1", "Refactored" = "blue1"))
}


getRecordTypeRefactoredLabel  <- function() {
  return (getRecordTypeRefactoredKey())
}
getRecordTypeSmellLabel  <- function() {
  return (getRecordTypeSmellKey())
}
getRecordTypeIgnoredSmellLabel  <- function() {
  return (getRecordTypeIgnoredSmellKey())
}


getRecordTypeLegend <- function() {
  return ("Record Types:")
}

getRecordTypeValues <- function() {
  return (
    c(
      getRecordTypeRefactoredLabel()
      , getRecordTypeSmellLabel()
      , getRecordTypeIgnoredSmellLabel()
    )
  )
}

