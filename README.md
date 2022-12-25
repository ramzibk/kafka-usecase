# kafka-usecase  
A simple kafka publish-subcribe example in Java  

### create the Zookeeper and Kafka containers   
`docker-compose -f docker-compose.yml up -d`  

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
each consumer will receive messages from one topic partition
there should not be more consumers than partitions, because otherwise some consumers will be idle and not receive any messges

### change the number of partitions  
it is possible to change the number of partitions after a topic is created, but this is not recommended  

`kafka-topics.sh --topic usecase.chat.messages --alter --partitions 3 --bootstrap-server localhost:29092`  

### list created Kafka topics  
`kafka-topics.sh --list --bootstrap-server localhost:29092`  

### view a topic's configuration  
`kafka-topics.sh --topic usecase.chat.messages --describe --bootstrap-server localhost:29092`  

### delete a topic  
`kafka-topics.sh --delete --topic usercase.chat.messages --bootstrap-server localhost:29092`  

### publish a record  into a topic (messages are called records in Kafka)  
`kafka-console-producer.sh --topic usercase.chat.messages --bootstrap-server localhost:29092`  

### consume the latest (non read) records from a topic  
`kafka-console-consumer.sh --topic usercase.chat.messages --bootstrap-server localhost:29092`  

### consume all the records from a topic  
`kafka-console-consumer.sh --topic usercase.chat.messages --from-beginning --bootstrap-server localhost:29092`  

### create a consumer group  
the creation of a consumer group happens during the creation of a consumer  
if the group does not exist it will be created, if it exists the consumer will be attached to that group  

`kafka-console-consumer.sh --bootstrap-server localhost:29092 --group ClientAppGroup --topic usecase.chat.messages`  

all messages from a topic will be sent to the consumer-group   
the consumer-group distributes the messages over all consumers inside it, which allows for scaling  
messages will be consumed by only one consumer inside the consumer-group   
the number of consumers inside a consumer group should not exceed the number of partitions in a topic  

### view the list of consumer groups created  
`kafka-consumer-groups.sh -bootstrap-server localhost:9092 -list`  

### viea the details of a consumer group  
`kafka-consumer-groups.sh -bootstrap-server localhost:9092 --describe --group ClientAppGroup`  

**output >>**

GROUP          | TOPIC                 | PARTITION  | CURRENT-OFFSET | LOG-END-OFFSET | LAG | CONSUMER-ID                                            | HOST         | CLIENT-ID
-------------- | --------------------- | ---------- |----------------|----------------|-----|--------------------------------------------------------|--------------| ----------------
ClientAppGroup | usecase.chat.messages | 0          | 20             | 20             | 0   | console-consumer-7cb8abdd-7185-4383-a49e-d126970bc4f5  | /172.19.0.3  | console-consumer 
ClientAppGroup | usecase.chat.messages | 1          | 0              | 0              | 0   | console-consumer-7cb8abdd-7185-4383-a49e-d126970bc4f5  | /172.19.0.3  | console-consumer  
ClientAppGroup | usecase.chat.messages | 2          | 0              | 0              | 0   | console-consumer-7cb8abdd-7185-4383-a49e-d126970bc4f5  | /172.19.0.3  | console-consumer   

Kafka ensure **at least once delivery** of messages through consumer offsets



