package com.shubhro.search

import com.search.core.Searcher
import org.scalatra._

class SearchServlet(searcher: Searcher) extends SearchserviceStack {

  get("/") {
    <html>
      <body>
        <h1>Similar!</h1>
      </body>
    </html>
  }

  get("/search/:keyword") {
    params.get("maxhits") match {
      case Some(maxhits) =>
        searcher.search(params("keyword"), maxhits.toInt)
      case None =>
        searcher.search(params("keyword"))
    }

  }

  get("/similar/:doc") {
    params.get("maxhits") match {
      case Some(maxhits) =>
        searcher.getSimilarDocs(params("doc"), maxhits.toInt)
      case None =>
        searcher.getSimilarDocs(params("doc"))
    }
  }

  get("/similar2/:doc") {
    params.get("maxhits") match {
      case Some(maxhits) =>
        searcher.getSimilarDocs(params("doc"), maxhits.toInt, true)
      case None =>
        searcher.getSimilarDocs(params("doc"), useCluster = true)
    }
  }


}
