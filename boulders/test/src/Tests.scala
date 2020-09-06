import indigo.shared.FrameContext
import indigo.shared.animation.AnimationAction.Play
import indigo.shared.animation.AnimationKey
import indigo.shared.datatypes.{Effects, Radians}
import indigo.{BindingKey, Depth, GameTime, Point, Vector2}
import indigo.shared.scenegraph.Sprite
import utest._

object Tests extends TestSuite {
  val tests: Tests =
    utest.Tests {
      /*"animation" - {
        "highlight box" - {
          val frameContext = new FrameContext[StartupData] (GameTime.zero, null, null, null, null)
          val levelCode = "2,2;1,0;0,1;0,0;CAAA"
          val level = Level.levelFromCode (levelCode)
          val line = TutorialGuideLine (PlayerIndicator, "", "", "", Set.empty)
          val sprite = new Sprite (BindingKey ("a"), Point (0, 0), Depth (3), rotation = Radians.TAU,
            Vector2 (1, 1), AnimationKey ("k"), Point (0, 0), Effects.default,
            _ => List.empty, List.empty)
          val playModel = PlayModel (level, GridPoint (0, 0), Matrix.emptyBooleanMatrix (2, 2),
            false, false, Playing, Vector.empty, Vector.empty, Vector (line), sprite)
          val viewModel = ViewModel (List.empty, List.empty)

          assert (PlayScene.present (frameContext, playModel, viewModel).gameLayer.nodes.exists { node =>
            node match {
              case s: Sprite => s.animationActions.contains (Play)
              case _ => false
            }
          })
        }
      }*/

      /*
      final class Sprite(
    val bindingKey: BindingKey,
    val position: Point,
    val depth: Depth,
    val rotation: Radians,
    val scale: Vector2,
    val animationKey: AnimationKey,
    val ref: Point,
    val effects: Effects,
    val eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent],
    val animationActions: List[AnimationAction]
)
       */

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
