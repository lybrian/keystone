package nodes.nlp

import pipelines.{LocalSparkContext, Transformer}

import org.apache.spark.SparkContext

import org.scalatest.FunSuite

class NGramSuite extends FunSuite with LocalSparkContext {

  val tokenizer = Transformer { x: String => x.split(" ").toSeq }

  test("NGramsFeaturizer") {
    sc = new SparkContext("local[2]", "NGramSuite")
    val rdd = sc.parallelize(Seq("Pipelines are awesome", "NLP is awesome"), 2)

    def run(orders: Seq[Int]) = {
      val pipeline = tokenizer then
        new NGramsFeaturizer(orders)

      pipeline(rdd)
        .mapPartitions(_.flatten) // for comparison
        .collect()
        .toSeq.map(_.toSeq) // for comparison
    }

    val unigrams = Seq(
      Seq("Pipelines"), Seq("are"), Seq("awesome"),
      Seq("NLP"), Seq("is"), Seq("awesome")
    )
    assert(run(Seq(1)) === unigrams)

    val bigramTrigrams = Seq(
      Seq("Pipelines", "are"), Seq("Pipelines", "are", "awesome"), Seq("are", "awesome"),
      Seq("NLP", "is"), Seq("NLP", "is", "awesome"), Seq("is", "awesome")
    )
    assert(run(2 to 3) === bigramTrigrams)

    assert(run(Seq(6)) === Seq.empty, "returns 6-grams when there aren't any")
  }

}
