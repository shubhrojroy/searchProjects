package com.search.core
/**
  * Created by shubhrojyotiroy on 3/9/17.
  */
object SearchConstants {

  // Indexing
  val Content = "contents"
  val Filename = "filename"
  val Filepath = "filepath"

  // Search
  val MaxDocs = 5
  val SearchMode = "search"
  val ClusterMode = "cluster"
  val IndexReaderMode = "read"

  // Clustering
  val ClusterFilename = "clusters.txt"
  val DefaultNumClusters = 10
  val NumIterations = 10000
  val DefaultLSIRank = 300
}
