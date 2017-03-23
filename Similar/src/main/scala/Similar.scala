import org.slf4j.LoggerFactory

/**
  * Created by shubhrojyotiroy on 3/9/17.
  */
object Similar {
  def main(args: Array[String]) = {
    args(0) match {
      case "index" =>
        if(args.size < 3) throw new IllegalArgumentException("Syntax: index <path_to_index> <files_to_index_path>")
        else CreateIndexCmd(args)
      case "search" =>
        if(args.size < 3) throw new IllegalArgumentException("Syntax: search <path_to_index> <query>")
        else SearchCmd(args)
      case "similar" =>
        if(args.size < 3) throw new IllegalArgumentException("Syntax: similar <path_to_index> <filename>")
        else SimilarCmd(args)
      case _ =>
        throw new IllegalArgumentException("Unrecognised command! Valid commands are : index, search, similar")
    }
  }

}

object CreateIndexCmd {
  def apply(args: Array[String]) = {
    val indexer = new Indexer(args(1))
    indexer.createIndex(args(2))
  }
}

object SearchCmd {
  def apply(args: Array[String]) = {
    val searcher = new Searcher(args(1))
    println("Top Hits:")
    println(searcher.search(args(2)).mkString("\n"))
  }
}

object SimilarCmd {
  def apply(args: Array[String]) = {
    val searcher = new Searcher(args(1))
    println("Similar Docs:")
    println(searcher.getSimilarDocs(args(2)).mkString("\n"))
  }
}