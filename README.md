# Kafka usecase
A simple kafka publish-subcribe example. This project builds on the LinkedIn courses :
* Apache Kafka Essentials Training: Getting Started
* Apache Kafka Essentials Training: Building Scalable Applications

This projects contains 2 docker-compose files:
* The single-broker.yml builds a single Kafka broker.
* The kafka-cluster.yml builds a Kafka cluster with 3 kafka brokers.

# Building and using a single Kafka broker
### create the Zookeeper and Kafka containers
`docker-compose -f single-broker.yml up -d`

### open a bash terminal inside the Kafka container
`docker exec -it kafka-broker bash`

### go to the kafka scripts directory
`cd /opt/bitnami/kafka/bin`  
`ls`

### create a Kafka topic
`kafka-topics.sh --create --topic usecase.chat.messages --bootstrap-server localhost:2909`

2909 is the kafka's port (kafka container's interal port in this case)

### create a topic with 2 patitions
`kafka-topics.sh --create --topic usecase.chat.messages --partitions 2 --bootstrap-server localhost:29092`  

Partitions allow for scalability depending on the number of partitions and consumers.  
With multiple partitions, multiple consumer can consumer messages from a single topic in parallel  
Partitions are also useful to define the destination of messages based on their message keys.  
The partition where the message is sent to is chosen by the producer according to the message key hash if a key is 
defined in the message otherwise it will be chosen using a round-robin method. Custom partition selectors can be implemented.
Messages with the same key will therefore be sent to same partition, this will guarentee the messages are ordered in the same partition.  
If the messages have no keys, they will be distributed round-robin and evenly across all the topics's partitions, 
this will not however preserve the ordering of the input messages.   
Each consumer will receive messages from one topic partition, however there should not be more consumers than partitions, 
 otherwise some consumers will be idle and not receive any messages.

### change the number of partitions
It is possible to change the number of partitions after a topic is created, but this is not recommended

`kafka-topics.sh --topic usecase.chat.messages --alter --partitions 3 --bootstrap-server localhost:29092`

### list created Kafka topics
`kafka-topics.sh --list --bootstrap-server localhost:29092`

### view a topic's configuration
`kafka-topics.sh --topic usecase.chat.messages --describe --bootstrap-server localhost:29092`

### delete a topic
`kafka-topics.sh --delete --topic usercase.chat.messages --bootstrap-server localhost:29092`

### publish a record  into a topic (messages are called records in Kafka)
`kafka-console-producer.sh --topic usercase.chat.messages --bootstrap-server localhost:29092`

### publish a key-value record into a topic
`kafka-console-producer.sh --topic usercase.chat.messages --property "parse.key=true" --property "key.separator=:" --bootstrap-server localhost:29092`

### consume the latest (non read) records from a topic
`kafka-console-consumer.sh --topic usercase.chat.messages --bootstrap-server localhost:29092`

### consume all the records from a topic
`kafka-console-consumer.sh --topic usercase.chat.messages --from-beginning --bootstrap-server localhost:29092`

### consume a key-value record
`kafka-console-consumer.sh --topic usercase.chat.messages --from-beginning --property "print.key=true" --property "key.separator=:" --bootstrap-server localhost:29092
`
### create a consumer group
The creation of a consumer group happens during the creation of a consumer  
If the group does not exist it will be created, if it exists the consumer will be attached to that group

`kafka-console-consumer.sh --bootstrap-server localhost:29092 --group ClientAppGroup --topic usecase.chat.messages`

All messages from a topic will be sent to the consumer-group   
The consumer-group distributes the messages over all consumers inside it, which allows for scaling  
Messages will be consumed by only one consumer inside the consumer-group   
The number of consumers inside a consumer group should not exceed the number of partitions in a topic
otherwise some consumers would be idle and not receive any messages  

### view the list of consumer groups created
`kafka-consumer-groups.sh -bootstrap-server localhost:9092 -list`

### view the details of a consumer group
`kafka-consumer-groups.sh -bootstrap-server localhost:9092 --describe --group ClientAppGroup`

**output >>**

| GROUP           | TOPIC                 | PARTITION  | CURRENT-OFFSET | LOG-END-OFFSET | LAG | CONSUMER-ID                                            | HOST         | CLIENT-ID|
|-----------------| --------------------- | ---------- |----------------|----------------|-----|--------------------------------------------------------|--------------| ----------------|
| ClientAppGroup  | usecase.chat.messages | 0          | 20             | 20             | 0   | console-consumer-7cb8abdd-7185-4383-a49e-d126970bc4f5  | /172.19.0.3  | console-consumer| 
| ClientAppGroup  | usecase.chat.messages | 1          | 0              | 0              | 0   | console-consumer-7cb8abdd-7185-4383-a49e-d126970bc4f5  | /172.19.0.3  | console-consumer| 
| ClientAppGroup  | usecase.chat.messages | 2          | 0              | 0              | 0   | console-consumer-7cb8abdd-7185-4383-a49e-d126970bc4f5  | /172.19.0.3  | console-consumer|   

Kafka ensure **at least once delivery** of messages through consumer offsets  
**CURRENT-OFFSET** is the latest offset of the message sent to the consumer  
**LOG-END-OFFSET** is the offset of the last message received in the partition     
**LAG** is the difference between CURRENT-OFFSET and LOG-END-OFFSET

### Create a Kafka Consumer and Producer for the simple Kafka broker
The class files are located under src/main/java: The Main.java class creates a producer instance to send a message (Record)
to the Kafka broker and a consumer to consume the message.

# Building a Kafka Cluster
To build the kafka cluster star up the kafka-cluster.yml file
`docker-compose -f kafka-cluster.yml up -d`

The docker file also builds a container for Kafdrop, a simple UI to monitor Kafka brokers
To access Kafkadrop open http://localhost:9000 in the browser

[Kafka Tool](https://www.kafkatool.com/) (renamed to Offset Explorer) is another free software that can be used 
to view and manage the Kafka Topics, Partition, etc

Having created a cluster with 3 brokers, we can then create topic having a replication factor of 2 which means that each 
Topic's partition will be replicated in 2 Brokers, a 'Partition-Leader' Broker (the one that owns the partition) and a 'Partition-Follower' Broker
that will hold a replica of that partition.   

In a Kafka Cluster there is always one partition leader only per partition at time and one or many partition followers for that partition.  
If the Partition Leader goes down, Kafka will assign the partition to another Broker (if exists) which will become the new Partition Leader.  
If the there are more partitions brokers, some brokers will become the Partition Leaders for multiple partitions.  
When the partition leader goes down kafka will select a new partition leader from the still available brokers.  

Messages are sent to a producer using the Client API send(ProducerRecord) method.
When the producer receives a message it serializes it, send it the Leading Partition which in turn
sends back an Acknowledgement

a local Batch is maintained on the producer side on a per partition basis to contain messages to be dispatched to the partition leader.  
The batch of messages will be dispatched to the Leader Partition once it attains its maximum size.
The time a producer will wait for its local batch to attain its batch size before it's dispatched to the partition leader 
can be configured through the config parameter `linger.ms`


## Producer Pulishing modes

* Synchronous:  
  the client is blocked until an acknowledgement is received from the Partition Leader
  before passing the next message.
  This method return a RecordMetaData Object containing the partition where the message was published to
  and the Offset for the message in that partition
  This method guaranties message delivery and allows for the message errors to be treated in the same client thread
  to resend the message or ignore it.
  Drawback of the method is that it is slow since it sends one message at a time until 
  acknowledgement of the message's reception 
  Synchronous mose is blocking and does not exploit the batch publishing capability of the producer


* Asynchronous with No-Check:  
  The client sends the message to the Producer through a separate thread and does not wait for the acknowledgement
  The advantage of this method is Low Latency and the Scaling capability, but it has a drawback
  that sent messages are not tracked and therefore messages can be missed or not replicated without
  the ability to do a retry.


* Asynchronous with Callback:  
  This is similar to Asynchronous with No-Check method in the way that it does not wait for Acknowledgment for the successful
  receipt of the message, however it provides a callback function with a RecordMetadData object 
  and the occurring exceptions to process the results.
  This method offers Low Latency, but error handling can be complex especially if message ordering is a strict requirement

The publishing mode can be chosen based on what method is called on the producer:  
* producer.send(producerRecord) : void // asynchronous without any check  

* producer.send(producerRecord).get() : RecordMetadata, throws Exception // synchronous, block and wait for confirmation  

* producer.send(producerRecord, asyncCallback) : void // asynchronous with callback  
  the callback parameter is an instance of a class that implements the org.apache.kafka.clients.producer.Callback interface

## Acknowledgements ##
There are 3 acknowledgement types which can be configured during the creation of a producer using the parameter 'acks'  
* 0 : means no acknowledgment is needed, as soon as the message is put onto the socket buffer it is considered sent
* 1 : guarentees the Leader Replica will send back an acknowledgement as soon it receives the message and saves it into it's local log
* all : This is equivalent to the acks=**-1** setting. the Leader Replica must wait for all In-Sync follower replicas to acknowledge the reception of the message before sending back an acknowledgement itself

```
// example
properties config = new Properties();  
config.put("acks", "all");  
KafkaProducer<String, String> producer = new KafkaProducer<>(config);  
```

All Kafka configuration parameters can be found at this [Link](https://kafka.apache.org/documentation.html#producerconfigs)




