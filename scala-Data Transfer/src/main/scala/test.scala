import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, TaskContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.{Durations, StreamingContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import java.lang.Exception
import java.sql.{Connection, DriverManager, PreparedStatement}
object SparkStremaingReadKafka {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local")
    conf.setAppName("SparkStreamingReadKafka")
    val ssc = new StreamingContext(conf,Durations.seconds(3))
    //    ssc.sparkContext.setLogLevel("Error")

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "suningvm:9092", // kafka集群
      "key.deserializer" -> classOf[StringDeserializer],  //指定读取kafka数据 key的序列化格式
      "value.deserializer" -> classOf[StringDeserializer], //指定读取Kafka 数据value的序列化格式
      "group.id" -> "test1", // 指定消费者组，利用kafka管理消费者offset时，需要以组为单位存储。
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)//是否开启自动向Kafka 提交消费者offset,周期5s
    )
    val topics = Array[String]("test1")
    val ds: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent, //接收Kafka数据的策略，这种策略是均匀将Kafka中的数据接收到Executor中处理。
      Subscribe[String, String](topics, kafkaParams)
    )
    val lines: DStream[String] = ds.map(cr => {
      println(s"message key = ${cr.key()}")
      println(s"message value = ${cr.value()}")
      //连接mysql
      val host="sun"
      val port=3306
      val database="web"
      val jdbcUrl=s"jdbc:mysql://$host:$port/$database?useUnicode=true&characterEncoding=utf-8"
      val mysqlConn: Connection = DriverManager.getConnection(jdbcUrl, "root", "123456")
      //插入数据
      val sql="insert into tests(tae) values(?)"
      val instertPreparedStatement: PreparedStatement =mysqlConn.prepareStatement(sql)
      try{
        instertPreparedStatement.setString(1, ""+cr.value())
        instertPreparedStatement.executeUpdate()
      }catch {
        case ex: Exception => {
        }
      }

      println("*********")

      cr.value()
    })
    val words : DStream[String]= lines.flatMap(line=>{line.split("\t")})
    val pairWords: DStream[(String,Int)] = words.map(word=>{(word,1)})
    val result = pairWords.reduceByKey((v1,v2)=>{v1+v2})
    result.print()

    //保证业务逻辑处理完成的情况下，异步将当前批次offset提交给kafka,异步提交DStream需要使用源头的DStream
    ds.foreachRDD { rdd =>
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      // some time later, after outputs have completed
      ds.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges) // 异步向Kafka中提交消费者offset
    }


    ssc.start()
    ssc.awaitTermination()

  }



}