package com.search.core

import java.io.Closeable
import java.nio.file.Paths
import java.util.concurrent.Executors

import com.search.core.SearchConstants._
import org.apache.lucene.index._
import org.apache.lucene.search.{IndexSearcher, TermQuery}
import org.apache.lucene.store.FSDirectory
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Created by shubhrojyotiroy on 3/8/17.
  */
class Searcher(indexPath: String, mode: String = SearchMode) extends Closeable {

  private val logger = LoggerFactory.getLogger(classOf[Searcher])
  private val indexReader = getInstance
  private val indexSearcher = new IndexSearcher(indexReader)
  private val inMemoryTermIndex = getTermIndex
  private val docIdFilenameIndex = getDocIdFilenameIndex
  private var clusterer: Clusterer = _
  private val similarityExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  if (mode == SearchMode) {
    clusterer = new Clusterer(indexPath)
    clusterer.loadClusters()
  }


  private def getInstance: IndexReader = {
    DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)))
  }

  private def getTermIndex: Map[String, Int] = {
    Option(MultiFields.getFields(indexReader)) match {
      case Some(fields) =>
        Option(fields.terms(Content)) match {
          case Some(field) =>
            val terms = getTerms(field.iterator())
            terms.zipWithIndex.map { case (ref, index) => ref.token -> index }.toMap
          case None =>
            Map()
        }
      case None =>
        Map()
    }
  }

  private def getDocIdFilenameIndex: Map[String, Int] = {
    (0 until indexReader.maxDoc()).map { docid =>
      getFilename(docid) -> docid
    }.toMap
  }

  private def getFilename(docId: Int) = indexReader.document(docId, Set(Filename).asJava).get(Filename)

  @tailrec
  private def getTerms(termIterator: TermsEnum, termList: List[IndexedTerm] = List()): List[IndexedTerm] = {
    Option(termIterator.next()) match {
      case None =>
        termList
      case Some(term) =>
        val tf = termIterator.totalTermFreq()
        val idf = Math.log(indexReader.maxDoc() / termIterator.docFreq())
        getTerms(termIterator, termList :+ IndexedTerm(term.utf8ToString(), tf, idf))
    }
  }

  def close(): Unit = {
    indexReader.close()
  }

  def search(queryStr: String, hitCount: Int = MaxDocs): List[String] = {
    val query = new TermQuery(new Term(Content, queryStr.toLowerCase))
    val hits = indexSearcher.search(query, hitCount)
    logger.info(s"Found ${hits.totalHits} hits for query $queryStr")

    hits.scoreDocs.map(doc => indexSearcher.doc(doc.doc).get(Filename)).toList

  }

  def getFeatureVector(docId: Int): FeatureVector = {
    val termVector: Terms = indexReader.getTermVector(docId, Content)
    val docTerms = getTerms(termVector.iterator(), List())
    val featureVector = new FeatureVector(docId, inMemoryTermIndex.size)
    docTerms.foreach { term =>
      featureVector.addTerm(inMemoryTermIndex(term.token), term.tf * term.idf)
    }
    featureVector.normalize()
    featureVector
  }

  private def getCosineSimilarity(doc1: FeatureVector, doc2: FeatureVector): Double = {
    doc1.normalizedVector.dotProduct(doc2.normalizedVector) / (doc1.normalizedVector.getNorm * doc2.normalizedVector.getNorm)
  }

  def getSimilarDocs(filename: String, hitCount: Int = MaxDocs, useCluster: Boolean = false): List[(String, Double)] = {
    docIdFilenameIndex.get(filename) match {
      case Some(srcDocId) =>
        val srcDocVector = getFeatureVector(srcDocId)
        //logger.info(s"Feature vector dimensionality: ${srcDocVector.getPoint.length}")
        val evaluationSet = if (useCluster) {
          clusterer.getCluster(clusterer.getClusterId(srcDocId))
        }
        else {
          docIdFilenameIndex.toSeq.map(_._2)
        }

        // Launch job to compute similarity and fetch most similar documents
        val evalFuture = getSimilarDocsAsync(evaluationSet.filter(_ != srcDocId).toList, srcDocVector)(similarityExecutor)

        // Wait for computation to complete and return hitlist
        Await.result(evalFuture, Duration.Inf).sortWith(_._2 > _._2).take(hitCount)

      /*
      evaluationSet.filter(_ != srcDocId).map { docId =>
        val tgtDocVector = getFeatureVector(docId)
        val score = getCosineSimilarity(srcDocVector, tgtDocVector)
        getFilename(docId) -> score
      }.sortWith(_._2 > _._2).toList.filter(_._2 > 0).take(hitCount)
      */
      case None =>
        logger.error("Document not present in Index!")
        List()
    }
  }

  private def getSimilarDocsAsync(evaluationSet: List[Int], srcDocVector: FeatureVector)
                                 (implicit ec: ExecutionContext): Future[List[(String, Double)]] = {
    Future.traverse(evaluationSet) { docId =>
      Future({
        val tgtDocVector = getFeatureVector(docId)
        val score = getCosineSimilarity(srcDocVector, tgtDocVector)
        (getFilename(docId), score)
      })
    }
  }


  def logIndexStats(printTokens: Boolean): Unit = {
    logger.info(s"Number of Docs: ${indexReader.maxDoc()}")
    logger.info(s"Number of unique tokens: ${inMemoryTermIndex.size}")
    if (printTokens) {
      inMemoryTermIndex.keySet.foreach(println)
    }
  }

  def getNumDocs: Int = indexReader.maxDoc()

  def getNumTerms: Int = inMemoryTermIndex.keySet.size


}
