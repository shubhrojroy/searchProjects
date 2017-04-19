package com.search.core

import java.io.{Closeable, File, FileReader}
import java.nio.file.Paths

import SearchConstants._
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Field, FieldType, StoredField}
import org.apache.lucene.index.{IndexOptions, IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class Indexer(indexPath: String) extends Closeable {

  private val logger = LoggerFactory.getLogger(classOf[Indexer])
  private lazy val indexWriter = getInstance

  private def getInstance: IndexWriter = {
    logger.info("Initializing Index Writer ...")
    val indexDir = FSDirectory.open(Paths.get(indexPath))
    // Standard Analyzer will do stopword elimination as well
    val conf = new IndexWriterConfig(new StandardAnalyzer(StandardAnalyzer.ENGLISH_STOP_WORDS_SET))
    conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
    new IndexWriter(indexDir, conf)
  }

  def close(): Unit = {
    indexWriter.close()
  }

  def createIndex(dataDir: String): Unit = {
    logger.info(s"Indexing Files in directory $dataDir")
    val dir = new File(dataDir)
    dir.listFiles().foreach { file =>
      if (!file.isDirectory && file.exists() && file.canRead && !file.isHidden)
        indexFile(file)
      else
        logger.error(s"Cannot index file ${file.getCanonicalPath}")
    }
    indexWriter.commit()
  }

  def dropIndex(): Unit = {
    indexWriter.deleteAll()
    indexWriter.commit()
  }

  def addFile(filePath: String): Unit = {
    logger.info(s"Adding file $filePath to the Index")
    val file = new File(filePath)
    if (!file.isDirectory && file.exists() && file.canRead && !file.isHidden)
      indexFile(file)
    else
      logger.error(s"Cannot index file $filePath")
    indexWriter.commit()
  }

  private def indexFile(file: File): Unit = {
    logger.debug(s"Indexing file ${file.getCanonicalPath}")
    val indexedFieldType = new FieldType()
    indexedFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS)
    indexedFieldType.setStoreTermVectors(true)
    indexedFieldType.setTokenized(true)
    val contentField = new Field(Content, new FileReader(file), indexedFieldType)
    val filenameField = new StoredField(Filename, file.getName)
    val filepathField = new StoredField(Filepath, file.getCanonicalPath)

    // Index Document
    try {
      indexWriter.addDocument(List(contentField, filenameField, filepathField).asJava)
    }
    catch {
      case exception: Exception =>
        logger.error(s"Error occurred during indexing file ${file.getCanonicalPath}", exception)
    }
  }

}

