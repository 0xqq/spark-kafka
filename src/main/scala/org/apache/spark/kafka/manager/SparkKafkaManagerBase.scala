package org.apache.spark.kafka.manager

import kafka.message.MessageAndMetadata
import org.apache.spark.rdd.RDD
import kafka.common.TopicAndPartition
import org.apache.spark.streaming.kafka.HasOffsetRanges
import org.apache.spark.kafka.util.KafkaSparkTool

trait SparkKafkaManagerBase extends KafkaSparkTool{
  val LAST="LAST"
  val CONSUM="CONSUM"
  val EARLIEST="EARLIEST"
  val CUSTOM="CUSTOM"
  val KAFKA_OFFSET="kafka.offset"
  override var kp:Map[String, String]
   /**
   * @author LMQ
   * @description  默认的一个handle (key,value)=>(topic,msg)
   */
  def msgHandle = (mmd: MessageAndMetadata[String, String]) => (mmd.topic, mmd.message)
    /**
   * @author LMQ
   * @description 获取RDD的offset。但这个rdd必须继承HasOffsetRanges 的
   */
  def getRDDConsumerOffsets[T](rdd: RDD[T]) = {
    var consumoffsets = Map[TopicAndPartition, Long]()
    val offsetsList = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
    for (offsets <- offsetsList) {
      val topicAndPartition = TopicAndPartition(offsets.topic, offsets.partition)
      consumoffsets += ((topicAndPartition, offsets.untilOffset))
    }
    consumoffsets
  }
  /**
   * @author LMQ
   * @description 将rdd的offset更新至zookeeper
   */
  def updateRDDOffset[T]( groupId: String, rdd: RDD[T]) {
    val offsets = getRDDConsumerOffsets(rdd)
    updateConsumerOffsets(groupId, offsets)
  }

}