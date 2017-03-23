
import org.apache.commons.math3.linear.{OpenMapRealVector, RealVector, RealVectorFormat}

/**
  * Created by shubhrojyotiroy on 3/12/17.
  */
class FeatureVector(numTerms: Int) {
  private val vector = new OpenMapRealVector(numTerms)
  var normalizedVector: RealVector= _

  def addTerm(pos: Int, weight: Double): Unit = {
    vector.setEntry(pos, weight)
  }

  def normalize(): Unit = {
    normalizedVector = vector.mapDivide(vector.getL1Norm)
  }

  override def toString: String = {
    val formatter = new RealVectorFormat()
    formatter.format(normalizedVector)
  }


}
