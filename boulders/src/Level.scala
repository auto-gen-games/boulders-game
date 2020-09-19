import Matrix.{emptyBooleanMatrix, updated}

/** A level specification, from which a game can be started. Each level has a width and height in grid cells,
  * a matrix specifying which cells have walls to the left, a matrix for which cells have floors underneath,
  * a matrix specifying which cells contain a boulder initially, the start position of the player, and the
  * positions of the diamond and exit.
  */
final case class Level(
    kind: String,
    number: Int,
    width: Int,
    height: Int,
    leftWalls: Vector[Vector[Boolean]],
    floors: Vector[Vector[Boolean]],
    boulders: Vector[Vector[Boolean]],
    start: GridPoint,
    diamond: GridPoint,
    exit: GridPoint
)

object Level {

  /** Creates an empty level of a given width and height, start, diamond and exit positions. */
  def empty(
      kind: String,
      number: Int,
      width: Int,
      height: Int,
      start: GridPoint,
      diamond: GridPoint,
      exit: GridPoint
  ): Level =
    Level(
      kind,
      number,
      width,
      height,
      emptyBooleanMatrix(width, height),
      emptyBooleanMatrix(width, height),
      emptyBooleanMatrix(width, height),
      start,
      diamond,
      exit
    )

  def flipPosition(position: GridPoint, level: Level): GridPoint =
    GridPoint(position.x, level.height - 1 - position.y)

  def flipped(level: Level): Level =
    level.copy(
      start = flipPosition(level.start, level),
      diamond = flipPosition(level.diamond, level),
      exit = flipPosition(level.exit, level),
      leftWalls = Matrix.flipped(level.leftWalls),
      floors = Matrix.cycleUp(Matrix.flipped(level.floors)),
      boulders = Matrix.flipped(level.boulders)
    )

  /** Returns true if the given position is within the grid for this level. */
  def inBounds(maze: Level, point: GridPoint): Boolean =
    point.x >= 0 && point.x < maze.width && point.y >= 0 && point.y < maze.height

  /** Returns true if there is a left wall at given grid position, where every position outside the level's
    * grid is assumed to have a wall (returns true).
    */
  def hasLeftWall(maze: Level, point: GridPoint): Boolean =
    !inBounds(maze, point) || maze.leftWalls(point.x)(point.y)

  /** Returns true if there is a floor at given grid position, where every position outside the level's
    * grid is assumed to have a floor (returns true).
    */
  def hasFloor(maze: Level, point: GridPoint): Boolean =
    !inBounds(maze, point) || maze.floors(point.x)(point.y)

  /** Adds a boulder to the given level grid cell at the given coordinates. */
  def addBoulder(maze: Level, x: Int, y: Int): Level =
    maze.copy(boulders = updated(maze.boulders, x, y, value = true))

  /** Adds a floor to the given level grid cell at the given coordinates. */
  def addFloor(maze: Level, x: Int, y: Int): Level =
    maze.copy(floors = updated(maze.floors, x, y, value = true))

  /** Adds a lefthand wall to the given level grid cell at the given coordinates. */
  def addLeftWall(maze: Level, x: Int, y: Int): Level =
    maze.copy(leftWalls = updated(maze.leftWalls, x, y, value = true))

  /** Decodes a newline-delimited list of serialised level specs. */
  def decodeLevels(kind: String, codes: String): Vector[Level] =
    codes.split('\n').zipWithIndex.map(ln => levelFromCode(kind, ln._2, ln._1)).toVector

  /** Decodes a single serialised level spec. */
  def levelFromCode(kind: String, number: Int, code: String): Level = {
    def coordinate(point: String): GridPoint = {
      val coordinates = point.split(",")
      GridPoint(coordinates(0).toInt, coordinates(1).toInt)
    }
    def setCell(maze: Level, x: Int, y: Int, code: Char): Level =
      code match {
        case 'B' => addBoulder(maze, x, y)
        case 'C' => addLeftWall(maze, x, y)
        case 'D' => addFloor(maze, x, y)
        case 'E' => addFloor(addBoulder(maze, x, y), x, y)
        case 'F' => addLeftWall(addFloor(maze, x, y), x, y)
        case _   => maze
      }

    val parts   = code.split(";")
    val area    = coordinate(parts(0))
    val width   = area.x
    val height  = area.y
    val player  = coordinate(parts(1))
    val exit    = coordinate(parts(2))
    val diamond = coordinate(parts(3))
    val empty   = Level.empty(kind, number, width, height, player, diamond, exit)
    parts(4).zipWithIndex.foldLeft(empty) {
      case (maze, (code, position)) => setCell(maze, position % width, position / width, code)
    }
  }
}
