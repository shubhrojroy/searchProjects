import com.shubhro.search._
import org.scalatra._
import javax.servlet.ServletContext

import com.search.core.Searcher

class ScalatraBootstrap extends LifeCycle {
  var searcher : Searcher = _
  override def init(context: ServletContext) {
    searcher = new Searcher("/Users/shubhrojyotiroy/indexDir")
    context.mount(new SearchServlet(searcher), "/*")
  }

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    searcher.close()
  }
}
