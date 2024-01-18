import org.apache.spark.sql.SparkSession

object PageRank {

  // Do not modify
  val PageRankIterations = 10
  val DampFactor = 0.85 

  /**
    * Input graph is a plain text file of the following format:
    *
    *   follower  followee
    *   follower  followee
    *   follower  followee
    *   ...
    *
    * where the follower and followee are separated by `\t`.
    *
    * After calculating the page ranks of all the nodes in the graph,
    * the output should be written to `outputPath` in the following format:
    *
    *   node  rank
    *   node  rank
    *   node  rank
    *
    * where node and rank are separated by `\t`.
    *
    * @param inputGraphPath path of the input graph.
    * @param outputPath path of the output of page rank.
    * @param iterations number of iterations to run on the PageRank.
    * @param spark the SparkSession.
    */
  def calculatePageRank(
      inputGraphPath: String,
      outputPath: String,
      iterations: Int,
      spark: SparkSession): Unit = {

    val sc = spark.sparkContext

    // Referenced code uses DampingFactor inside the calculatePageRank
    val damping = DampFactor
    val iterCount = iterations 
    val edgesFile = sc.textFile(inputGraphPath) 

    // Constructing (follower, followee) pairs and group followees by follower
    val links = edgesFile
      .map(_.split("\t"))
      .map(parts => (parts(0), parts(1)))
      .distinct() 
      .groupByKey() 
      .cache()

    // Compute the list of all unique pages (followers and followees)
    val allPages = links
      .flatMap { case (follower, followees) => Seq(follower) ++ followees }
      .distinct() 
      .map(page => (page, None)) // Referenced code uses None

    // Identify pages with no outgoing links and assign "empty"
    val danglingPages = allPages.subtractByKey(links).mapValues(_ => Iterable(""))

    val followeeNodes = links
      .flatMap(_._2)
      .distinct()
      .map(followee => (followee, None))

    // Combine all links, including dangling pages
    val completeLinks = links.union(danglingPages).cache()

    // Calculate the total number of pages
    val pageCount = completeLinks.count().toDouble

    // Initialize page ranks with equal values, referenced code does this operation
    var ranks = completeLinks.mapValues(_ => 1.0 / pageCount)

    // Iteration of PageRank algorithm for the specified number of iterations
    for (iteration <- 1 to iterCount) {
      val accDangling = sc.doubleAccumulator("Dangling Nodes Accumulator")

      val contribs = completeLinks.join(ranks).flatMap { case (page, (urls, rank)) =>
        if (urls.exists(_ == "")) { 
          accDangling.add(rank) 
          Seq() 
        } else {
          urls.map(url => (url, rank / urls.size)) 
        }
      }

      // Sum up contributions by followee
      val summedContribs = contribs.reduceByKey(_ + _)
      summedContribs.take(1)
      // Retrieve the accumulated rank from dangling nodes.
      val danglingRankTotal = accDangling.value

      ranks.unpersist()

      // Calculate new ranks using the PageRank formula
      ranks = summedContribs
          .union(allPages.subtract(followeeNodes).mapValues(_ => 0.0).cache()) // Set initial rank for pages with no incoming links
          .mapValues(summedRank => (1 - damping) / pageCount + damping * (danglingRankTotal / pageCount + summedRank))
          .cache()
    }

    // Format the output as tab-separated values: node_id rank_value
    val result = ranks.map { case (nodeId, rankValue) => s"$nodeId\t$rankValue" }

    // Save the output to the specified path
    result.saveAsTextFile(outputPath)
  }
  /**
    * @param args it should be called with two arguments, the input path, and the output path.
    */
  def main(args: Array[String]): Unit = {
    val spark = SparkUtils.sparkSession()

    val inputGraph = args(0)
    val pageRankOutputPath = args(1)

    calculatePageRank(inputGraph, pageRankOutputPath, PageRankIterations, spark)
  }
}
