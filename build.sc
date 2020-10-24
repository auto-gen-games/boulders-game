import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._

import $ivy.`io.indigoengine::mill-indigo:0.4.0`, millindigo._

object boulders extends ScalaJSModule with MillIndigo {
  def scalaVersion   = "2.13.3"
  def scalaJSVersion = "1.2.0"

  val gameAssetsDirectory: os.Path = os.pwd / "assets"
  val showCursor: Boolean          = true
  val title: String                = "Boulders"

  val magnification = 2
  val windowStartHeight = 352 * magnification
  val windowStartWidth = 352 * magnification

  def ivyDeps = Agg (
    ivy"io.indigoengine::indigo-json-circe::0.4.0",
    ivy"io.indigoengine::indigo::0.4.0"
  )

  def buildGame() = T.command {
    T {
      compile()
      fastOpt()
      indigoBuild()() // Note the double parenthesis!
    }
  }

  def runGame() = T.command {
    T {
      compile()
      fastOpt()
      indigoRun()() // Note the double parenthesis!
    }
  }

  def buildGameFull() = T.command {
    T {
      compile()
      fullOpt()
      indigoBuildFull()() // Note the double parenthesis!
    }
  }

  def runGameFull() = T.command {
    T {
      compile()
      fullOpt()
      indigoRunFull()() // Note the double parenthesis!
    }
  }

  object test extends Tests {
    def ivyDeps = Agg (
      ivy"com.lihaoyi::utest::0.7.4"
    )
    def testFrameworks = Seq ("utest.runner.Framework")
  }
}