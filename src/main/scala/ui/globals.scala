package ui

import org.scalajs.dom

def isWindows: Boolean = dom.window.navigator.appVersion.toLowerCase.contains("windows")

enum BrowserType:
  case Chrome
  case Firefox
  case Edge
  case Unknown

def browserType: BrowserType =
  val agent = dom.window.navigator.userAgent.toLowerCase
  if agent.contains("firefox") then BrowserType.Firefox
  else if agent.contains("edg") then BrowserType.Edge
  else if agent.contains("chrome") then BrowserType.Chrome
  else BrowserType.Unknown
