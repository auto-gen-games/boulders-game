/** Utility functions for 2D matrices. */
object Matrix {
  /** Create an empty 2D Boolean matrix of a given size. */
  def emptyBooleanMatrix (width: Int, height: Int): Vector[Vector[Boolean]] =
    Vector.fill (width)(Vector.fill (height)(false))

  /** Update an element of a 2D matrix to a new value. */
  def updated[E] (matrix: Vector[Vector[E]], index1: Int, index2: Int, value: E): Vector[Vector[E]] =
    matrix.updated (index1, matrix (index1).updated (index2, value))
}
