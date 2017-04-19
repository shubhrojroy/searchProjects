package com.search.core

import java.io.{BufferedWriter, File, FileWriter}

import org.apache.commons.math3.ml.clustering.{CentroidCluster, KMeansPlusPlusClusterer}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import SearchConstants._

import scala.io.Source

/**
  * Created by shubhrojyotiroy on 4/5/17.
  */
class Clusterer(indexDir: String) {

  private val logger = LoggerFactory.getLogger(classOf[Searcher])
  private var docToClusterMapping: Map[Int , Int] = _
  private var clusterToDocMapping: Map[Int, List[Int]] = _

  def cluster(numClusters: Int = DefaultNumClusters): Unit = {

    logger.info(s"Clustering Indexed documents from $indexDir ...")
    val kmeans = new KMeansPlusPlusClusterer[FeatureVector](numClusters, NumIterations)

    // Generate Document Vectors for clustering
    val searcher = new Searcher(indexDir, ClusterMode)
    val docVectors = (0 until searcher.getNumDocs).map{ docId =>
      searcher.getFeatureVector(docId)
    }

    // Cluster the documents
    logger.info(s"Generating upto $numClusters clusters for ${docVectors.size} documents ...")
    val clusters = kmeans.cluster(docVectors.asJava).asScala.toList
    logger.info(s"Generated ${clusters.size} clusters.")
    writeClusters(clusters)


  }

  private def writeClusters(clusters: List[CentroidCluster[FeatureVector]]) = {
    // Write clusters to output file
    logger.info(s"Writing clusters to cluster file $indexDir/$ClusterFilename")
    val fileWriter = new BufferedWriter(new FileWriter(new File(s"$indexDir/$ClusterFilename"), true))
    try {
      var clusterId = 0

      clusters.foreach { cluster =>
        val docIds = cluster.getPoints.asScala.map(_.docId).toList.mkString(",")
        fileWriter.write(s"$clusterId,$docIds")
        fileWriter.newLine()
        clusterId += 1
      }
    }
    catch{
      case e: Exception =>
        logger.error("Exception during writing clusters to file!",e)
        throw e
    }
    finally {
      fileWriter.close()
    }
  }

  def loadClusters(): Unit = {
    logger.info(s"Loading clusters from $indexDir/$ClusterFilename")
    try {
      clusterToDocMapping = (for{
        line <- Source.fromFile(s"$indexDir/$ClusterFilename").getLines()
      } yield {
        val values = line.split(",").map(_.toInt).toList
        values.head -> values.tail
      }).toMap
      docToClusterMapping = clusterToDocMapping.toSeq.flatMap{ case (clusterId, docIds) =>
          docIds.map(docId => docId -> clusterId)
      }.toMap
      logger.info(s"Loaded ${clusterToDocMapping.keySet.size} clusters.")
    }
    catch {
      case e: Exception =>
        logger.error(s"Exception during reading cluster file!", e)
        throw e
    }
  }

  def getClusterId(docId: Int): Int = {
    docToClusterMapping(docId)
  }

  def getCluster(clusterId: Int): List[Int] = {
    clusterToDocMapping(clusterId)
  }


}
