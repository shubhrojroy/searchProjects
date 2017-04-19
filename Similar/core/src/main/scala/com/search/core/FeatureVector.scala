package com.search.core

import org.apache.commons.math3.linear.{OpenMapRealVector, RealVector, RealVectorFormat}
import org.apache.commons.math3.ml.clustering.Clusterable

/**
  * Created by shubhrojyotiroy on 3/12/17.
  */
case class FeatureVector(docId: Int, numTerms: Int) extends Clusterable{

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

  override def getPoint = {
    normalizedVector.toArray
  }


}
