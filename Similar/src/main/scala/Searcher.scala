import java.io.Closeable
import java.nio.file.Paths

import SearchConstants._
import org.apache.lucene.index._
import org.apache.lucene.search.{IndexSearcher, TermQuery, TopDocs}
import org.apache.lucene.store.FSDirectory
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.collection.JavaConverters._

/**
  * Created by shubhrojyotiroy on 3/8/17.
  */
class Searcher(indexPath: String) extends Closeable {

  private val logger = LoggerFactory.getLogger(classOf[Searcher])
  private val indexReader = getInstance
  private val indexSearcher = new IndexSearcher(indexReader)
  private val inMemoryTermIndex = getTermIndex
  private val docIdFilenameIndex = getDocIdFilenameIndex

  private def getInstance: IndexReader = {
    DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)))
  }

  private def getTermIndex: Map[String, Int] = {
    val fields = MultiFields.getFields(indexReader)
    val terms = getTerms(fields.terms(Content).iterator())
    terms.zipWithIndex.map { case (ref, index) => ref.token -> index }.toMap
  }

  private def getDocIdFilenameIndex: Map[String, Int] = {
    (0 until indexReader.maxDoc()).map { docid =>
      indexReader.document(docid, Set(Filename).asJava).get(Filename) -> docid
    }.toMap
  }

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

  def search(queryStr: String): List[String] = {
    val query = new TermQuery(new Term(Content, queryStr))
    val hits: TopDocs = indexSearcher.search(query, MaxDocs)
    logger.info(s"Found ${hits.totalHits} for query $queryStr")

    hits.scoreDocs.map(doc => indexSearcher.doc(doc.doc).get(Filename)).toList

  }

  def getTermVector(docId: Int): FeatureVector = {
    val termVector: Terms = indexReader.getTermVector(docId, Content)
    val docTerms = getTerms(termVector.iterator(), List())
    val featureVector = new FeatureVector(inMemoryTermIndex.size)
    docTerms.foreach { term =>
      featureVector.addTerm(inMemoryTermIndex(term.token), term.tf * term.idf)
    }
    featureVector.normalize()
    featureVector
  }

  def getCosineSimilarity(doc1: FeatureVector, doc2: FeatureVector): Double = {
    doc1.normalizedVector.dotProduct(doc2.normalizedVector) / (doc1.normalizedVector.getNorm * doc2.normalizedVector.getNorm)
  }

  def getSimilarDocs(filename: String): List[(String, Double)] = {
    docIdFilenameIndex.get(filename) match {
      case Some(srcDocId) =>
        val srcDocVector = getTermVector(srcDocId)
        docIdFilenameIndex.toSeq.filter(_._2 != srcDocId).map { case (name, docId) =>
          val tgtDocVector = getTermVector(docId)
          val score = getCosineSimilarity(srcDocVector, tgtDocVector)
          name -> score
        }.sortWith(_._2 > _._2).toList.take(MaxDocs)
      case None =>
        logger.error("Document not present in Index!")
        List()
    }
  }


}
