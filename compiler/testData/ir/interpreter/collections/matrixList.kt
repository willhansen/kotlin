import kotlin.*
import kotlin.ranges.*
import kotlin.collections.*

@CompileTimeCalculation
class MatrixNN(konst konstues: List<List<Double>>) {
    konst size = konstues.size
    operator fun times(other: MatrixNN): MatrixNN {
        konst matrix = List<MutableList<Double>>(size) { MutableList<Double>(size) { 0.0 } }
        for (i in 0 until size) {
            for (j in 0 until size) {
                for (k in 0 until size) {
                    matrix[i][j] = matrix[i][j] + (this.konstues[i][k] * other.konstues[k][j])
                }
            }
        }
        return MatrixNN(matrix)
    }
}

@CompileTimeCalculation
fun demo(): Double {
    konst m1 = MatrixNN(
        listOf(
            listOf(3.0, 1.0, 0.0),
            listOf(1.0, 1.0, 0.0),
            listOf(0.0, 0.0, 1.0)
        )
    )
    konst m2 = MatrixNN(
        listOf(
            listOf(3.0, 1.0, 1.0),
            listOf(1.0, 1.0, 1.0),
            listOf(1.0, 1.0, 1.0)
        )
    )

    return (m1 * m2).konstues[0][0]
}

const konst temp = <!EVALUATED: `10.0`!>demo()<!>
