package com.search.core

import java.io.Closeable

import SearchConstants._
import org.apache.commons.math3.linear._
import org.slf4j.LoggerFactory
/**
  * Created by shubhrojyotiroy on 4/10/17.
  */
class LatentSemanticIndexer(indexDir: String) extends Closeable{
  private val logger = LoggerFactory.getLogger(classOf[Searcher])
  val searcher = new Searcher(indexDir, IndexReaderMode)

  def createIndex(lsiRank: Int = DefaultLSIRank): Unit = {
    val termDocumentMatrix = new OpenMapRealMatrix(searcher.getNumTerms, searcher.getNumDocs)
    (0 until searcher.getNumDocs).foreach { docId =>
      val docVector = searcher.getFeatureVector(docId)
      termDocumentMatrix.setColumnVector(docId, docVector.normalizedVector)
    }

    logger.info(s"Performing Singular Value Decomposition ...")
    val svd = new SingularValueDecomposition(termDocumentMatrix)
    logger.info(s"Singular Values: ${svd.getSingularValues.toList}")



  }

  def testLSI(lsiRank: Int): Unit = {
    val mat: Array[Array[Double]] = Array(Array(1,0,1,0,0,0),Array(0,1,0,0,0,0),Array(1,1,0,0,0,0),Array(1,0,0,1,1,0), Array(0,0,0,1,0,1))
    val C = new Array2DRowRealMatrix(mat)
    println(s"C = \n $C")
    val svd = new SingularValueDecomposition(C)
    println(s"U= \n${svd.getU}")
    println(s"S= \n ${svd.getS}")
    println(s"VT = \n ${svd.getVT}")
    val lowerRankS = svd.getS
    val zeroVector = new OpenMapRealVector(lowerRankS.getColumnDimension)
    (lsiRank until lowerRankS.getRowDimension).foreach( row => lowerRankS.setRowVector(row, zeroVector))
    println(s"Lower rank S: \n $lowerRankS")
    val lsi = svd.getU.multiply(lowerRankS)
    println(s"Lower Rank Term-Document Matrix: \n $lsi")


  }

  override def close() = {
    searcher.close()
  }


}
