# spark-kafka

**scala version 2.10** <br/>
**spark version 1.6.0** <br/>
**kafka version 0.10** <br/>

  * 添加了spark 1.6 + kafka 0.10的集成jar<br/>
  * spark-streaming-kafka-0-10_2.10-1.6.0.jar<br/>
  * 支持了kafka 0.10的 SSL 验证。<br/>
  * kafka 0.10 默认是将offset存储在自身的topic里而不是之前版本的zookeeper里面<br/>
  * 所以，想自己实现存储zookeeper的可以使用kafka-util工具来自行封装<br/>
