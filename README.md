# Simple Java MQTT Demo

Simple Java MQTT-Demo to publish SIN wave data to MQTT broker.
Default MQTT broker is tcp://localhost:1883, but can be configured with environment variable MQTT_BROKER
PUBLISH_TOPIC is "sensors/java/", environment variable SENSOR_ID is appended after the topic, if it is not set it defaults to 0
Programm subscribes at a specific SUBSCRIPTION_TOPIC "feedback/java/{SENSOR_ID}" and stops sending data if "stop" message is received

Run programm like that:
 
```
 EXPORT SENSOR_ID=42
 java -cp target/MQTT-Client2-0.0.1-SNAPSHOT.jar ch.wiss.m321mqttdemo.MySensor 
 
```

Code adopted from https://www.emqx.com/en/blog/how-to-use-mqtt-in-java