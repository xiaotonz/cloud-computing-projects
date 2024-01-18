import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.SaveMode

object FollowerDF {

  /**
    * This function should first read the graph located at the input path, it should compute the
    * follower count, and save the top 100 users to the output path in parquet format.
    *
    * It must be done using the DataFrame/Dataset API.
    *
    * It is NOT valid to do it with the RDD API, and convert the result to a DataFrame, nor to read
    * the graph as an RDD and convert it to a DataFrame.
    *
    * @param inputPath the path to the graph.
    * @param outputPath the output path.
    * @param spark the spark session.
    */
  def computeFollowerCountDF(inputPath: String, outputPath: String, spark: SparkSession): Unit = {
    // TODO: Calculate the follower count for each user
    // TODO: Write the top 100 users to the above outputPath in parquet format
    import spark.implicits._
    val df = spark.read.textFile(inputPath).as[String]
    val countsDF = df.flatMap { line =>
      val parts = line.split("\t")
      if (parts.length == 2) Some((parts(1), 1)) else None
    }.toDF("user_id", "follower_count")
    .groupBy("user_id")
    .sum("follower_count")
    .withColumnRenamed("sum(follower_count)", "follower_count")
    .orderBy($"follower_count".desc)
    .limit(100)
    countsDF.write.mode(SaveMode.Overwrite).parquet(outputPath)

  }

  /**
    * @param args it should be called with two arguments, the input path, and the output path.
    */
  def main(args: Array[String]): Unit = {
    val spark = SparkUtils.sparkSession()

    val inputGraph = args(0)
    val followerDFOutputPath = args(1)

    computeFollowerCountDF(inputGraph, followerDFOutputPath, spark)
  }

}
