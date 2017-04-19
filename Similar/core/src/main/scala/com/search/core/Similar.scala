package com.search.core

import org.slf4j.LoggerFactory
import com.search.core.SearchConstants._

/**
  * Created by shubhrojyotiroy on 3/9/17.
  */
object Similar {
  def main(args: Array[String]): Unit = {
    args(0) match {
      case "index" =>
        if(args.length < 3) throw new IllegalArgumentException("Syntax: index <path_to_index> <files_to_index_path>")
        else CreateIndexCmd(args)
      case "cluster" =>
        if(args.length < 2) throw new IllegalArgumentException("Syntax: cluster <path_to_index> [<numClusters>]")
        else ClusterCmd(args)
      case "add" =>
        if(args.length < 3) throw new IllegalArgumentException("Syntax: add <path_to_index> <filePath>")
        else AddToIndexCmd(args)
      case "search" =>
        if(args.length < 3) throw new IllegalArgumentException("Syntax: search <path_to_index> <query> [<hitcount>]")
        else SearchCmd(args)
      case "similar" =>
        if(args.length < 3) throw new IllegalArgumentException("Syntax: similar <path_to_index> <filename> [<hitcount>]")
        else SimilarCmd(args)
      case "drop" =>
        if(args.length < 2) throw new IllegalArgumentException("Syntax: drop <path_to_index>")
        else DropCmd(args)
      case "stats" =>
        if(args.length < 2) throw new IllegalArgumentException("Syntax: stats <path_to_index> [--verbose]")
        else GetStatsCmd(args)
      case "lsi" =>
        if(args.length < 2) throw new IllegalArgumentException(s"Syntax lsi <path_to_index>")
        else LSICmd(args)
      case _ =>
        throw new IllegalArgumentException("Unrecognised command! Valid commands are : index, add, search, similar, drop, stats")
    }
  }

}

object CreateIndexCmd {
  def apply(args: Array[String]): Unit = {
    val indexer = new Indexer(args(1))
    try{
      indexer.createIndex(args(2))
    }
    finally {
      indexer.close()
    }
  }
}

object LSICmd {
  def apply(args: Array[String]): Unit = {
    val latentSemanticIndexer = new LatentSemanticIndexer(args(1))
    try{
      if(args.length>2) latentSemanticIndexer.createIndex(args(2).toInt) else latentSemanticIndexer.createIndex()
      //latentSemanticIndexer.testLSI(args(2).toInt)
    }
    finally {
      latentSemanticIndexer.close()
    }
  }
}

object ClusterCmd {
  def apply(args: Array[String]): Unit = {
    val clusterer = new Clusterer(args(1))
    if(args.length > 2) clusterer.cluster(args(2).toInt) else clusterer.cluster()
  }
}

object SearchCmd {
  def apply(args: Array[String]): Unit = {
    val searcher = new Searcher(args(1))
    println("Top Hits:")
    if(args.length>3)
      println(searcher.search(args(2), args(3).toInt).mkString("\n"))
    else
      println(searcher.search(args(2)).mkString("\n"))
    searcher.close()
  }
}

object SimilarCmd {
  def apply(args: Array[String]): Unit = {
    val searcher = new Searcher(args(1))
    println("Similar Docs:")
    if(args.length>3)
      println(searcher.getSimilarDocs(args(2), args(3).toInt).mkString("\n"))
    else
      println(searcher.getSimilarDocs(args(2)).mkString("\n"))
    searcher.close()
  }
}

object DropCmd {
  def apply(args: Array[String]): Unit = {
    val continue = Console.readLine("This command will drop the Index. Do you wish to continue (Y/N)?")
    if(continue == "Y") {
      val indexer = new Indexer(args(1))
      indexer.dropIndex()
      indexer.close()
    }
  }
}

object GetStatsCmd {
  def apply(args: Array[String]): Unit = {
    val searcher = new Searcher(args(1), IndexReaderMode)
    if(args.length>2 && args(2)=="--verbose") searcher.logIndexStats(true) else searcher.logIndexStats(false)
    searcher.close()
  }
}

object AddToIndexCmd {
  def apply(args: Array[String]): Unit = {
    val indexer = new Indexer(args(1))
    indexer.addFile(args(2))
    indexer.close()
  }
}