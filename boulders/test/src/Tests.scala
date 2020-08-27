import utest._

object Tests extends TestSuite {
  val tests: Tests =
    utest.Tests {
      "maze loading" - {
        "matrix update" - {
          val matrix0 = Matrix.emptyBooleanMatrix (width = 10, height = 10)
          val matrix1 = Matrix.updated (matrix0, 2, 3, true)
          val matrix2 = Matrix.updated (matrix1, 5, 3, true)

          matrix2 (2)(3) ==> true
          matrix2 (5)(3) ==> true
          matrix2 (4)(4) ==> false
        }

        "set walls" - {
          val maze1 = Level.empty (2, 2, GridPoint (1, 0), GridPoint (0, 0), GridPoint (0, 1))
          val maze2 = Level.addLeftWall (maze1, 0, 0)
          val maze3 = Level.addLeftWall (maze2, 0, 1)

          maze3.leftWalls (0)(0) ==> true
          maze3.leftWalls (0)(1) ==> true
          maze3.leftWalls (1)(1) ==> false
        }

        "set wall and floor" - {
          val maze1 = Level.empty (2, 2, GridPoint (1, 0), GridPoint (0, 0), GridPoint (0, 1))
          val maze2 = Level.addLeftWall (maze1, 0, 0)
          val maze3 = Level.addFloor (maze2, 0, 1)

          maze3.leftWalls (0)(0) ==> true
        }

        "load walls A" - {
          val levelCode = "2,2;1,0;0,1;0,0;CAAA"
          val level = Level.levelFromCode (levelCode)

          level.leftWalls (0)(0) ==> true
        }

        "load walls B" - {
          val levelCode = "2,2;1,0;0,1;0,0;CAFA"
          val level = Level.levelFromCode (levelCode)

          level.leftWalls (0)(0) ==> true
        }
      }
    }
}
