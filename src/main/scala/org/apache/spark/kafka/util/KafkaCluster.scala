package org.apache.spark.kafka.util

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.kafka.clients.consumer.ConsumerConfig
class KafkaCluster[K, V](kp: Map[String, String]) {

  lazy val fixKp = fixKafkaParams(kp)
  @transient private var kc: Consumer[K, V] = null

  /**
   * @author LMQ
   * @time 2018-10-31
   * @desc 获取consumer
   */
  def c(): Consumer[K, V] = this.synchronized {
    if (null == kc) {
      kc = new KafkaConsumer[K, V](fixKp)
    }
    kc
  }
  /**
   * @author LMQ
   * @time 2018-10-31
   * @desc 修正kp的配置
   */
  def fixKafkaParams(kafkaParams: Map[String, String]) = {
    val fixKp = new java.util.HashMap[String, Object]()
    kafkaParams.foreach { case (x, y) => fixKp.put(x, y) }
    fixKp.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false: java.lang.Boolean)
    fixKp.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "none")
    fixKp.put(ConsumerConfig.RECEIVE_BUFFER_CONFIG, 65536: java.lang.Integer)
    fixKp
  }
  /**
   * @author LMQ
   * @time 2018-10-31
   * @desc 获取上次消费的offset
   */
  def getConsumerOffet() = {
    c.poll(0)
    val parts = c.assignment()
    parts.map { tp => tp -> c.position(tp) }.toMap
  }
  /**
   * @author LMQ
   * @time 2018-10-31
   * @desc 更新偏移量
   */
  def updateOffset(offset: Map[TopicPartition, Long]) {
    offset.foreach { case (tp, l) => c.seek(tp, l) }
    c.commitSync(offset
      .map { case (tp, l) => tp -> new OffsetAndMetadata(l) }
      .asJava)
  }
  /**
   * @author LMQ
   * @time 2018-10-31
   * @desc 获取最新偏移量
   */
  def getLastestOffset() = {
    c.poll(0)
    val parts = c.assignment()
    val currentOffset = parts.map { tp => tp -> c.position(tp) }.toMap
    c.pause(parts)
    c.seekToEnd(parts)
    val re = parts.map { ps => ps -> c.position(ps) }
    currentOffset.foreach { case (tp, l) => c.seek(tp, l) }
    re
  }
  /**
   * @author LMQ
   * @time 2018-10-31
   * @desc 获取最开始偏移量
   */
  def getEarleastOffset(c: KafkaConsumer[String, String]) = {
    c.poll(0)
    val parts = c.assignment()
    val currentOffset = parts.map { tp => tp -> c.position(tp) }.toMap
    c.pause(parts)
    c.seekToBeginning(parts)
    val re = parts.map { ps => ps -> c.position(ps) }
    currentOffset.foreach { case (tp, l) => c.seek(tp, l) }
    re
  }
}