package logging

import org.slf4j.LoggerFactory

// Simple logging trait that any class can mix in to have access to a logger

trait AppLogger {
  
  val log = LoggerFactory.getLogger(this.getClass)
  
}