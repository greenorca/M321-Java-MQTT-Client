package ch.wiss.m321mqttdemo;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Simple Java MQTT-Demo to publish SIN wave data onto PUBLISH_TOPIC "sensors/java/",
 * subscribes at a specific SUBSCRIPTION_TOPIC "feedback/java/" and stops sending data if a stop message is received
 * 
 * appends environment variable SENSOR_ID (if set) after the topic, otherwise assumes 0
 * 
 * so it can be run like that:
 * ```
 * EXPORT SENSOR_ID=42
 * java -cp target/MQTT-Client2-0.0.1-SNAPSHOT.jar ch.wiss.m321mqttdemo.MySensor 
 * ```
 * code adopted from https://www.emqx.com/en/blog/how-to-use-mqtt-in-java
 * 
 * @author sven
 *
 */

public class MySensor {
	
	private static String PUBLISH_TOPIC = "sensors/java/";
	private static String SUBSCRIPTION_TOPIC = "feedback/java/";

	private static final String SERVER_URI = "tcp://127.0.0.1:1883";

	private static double x = 0;

	public static IMqttClient publisher;
	public static IMqttClient subscriber;
	public static String publisherId = UUID.randomUUID().toString();
	public static String subscriberId = UUID.randomUUID().toString();
	public static boolean active = true;

	static IMqttMessageListener subscriptionListener = new IMqttMessageListener() {
				
		//event handler for MQTT subscriber, implements the IMqttMessageListener interface 
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			System.out.println("Message received for topic: " + topic+": "+message);
			if (message.toString().equals("stop")) {
				System.out.println("Stopping sensor");
				active = false;
			} 
		}
	};

	public static void sendData() throws Exception{
		MqttConnectOptions options = new MqttConnectOptions();
		options.setAutomaticReconnect(true);
		options.setCleanSession(true);
		options.setConnectionTimeout(10);
		publisher.connect(options);
		
		double y = 10 * Math.sin(x) + 10;
		x += 0.1;
		x = x % (20 * 3.14);
		
		MqttMessage msg = new MqttMessage(String.valueOf(y).getBytes());
		msg.setQos(0);
		msg.setRetained(true);
		publisher.publish(PUBLISH_TOPIC, msg);
		
		publisher.disconnect();
		Thread.sleep(1000);
		System.out.println("Value sent: "+String.valueOf(y));
	}

	public static void main(String[] args) {	

		String id = System.getenv("SENSOR_ID");
		if (id == null){
			id = "0";
		};

		PUBLISH_TOPIC += id;
		SUBSCRIPTION_TOPIC += id;
		
		try {
			
			publisher = new MqttClient(SERVER_URI, publisherId);
			
			subscriber = new MqttClient(SERVER_URI, subscriberId);
			subscriber.connect();
			
			//subscribe to specific topic
			subscriber.subscribe(SUBSCRIPTION_TOPIC, subscriptionListener);
			
			// calculates and publishes sin-wave data 
			while (active) {
				sendData();				
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		
		try {
			System.out.println("subscriber disconnecting");
			subscriber.disconnect();
		}
		catch(Exception ex) {}
		
		try {
			if (publisher.isConnected()) {
				System.out.println("publisher disconnecting");
				publisher.disconnect();
			}
		}
		catch(Exception ex) {}
		
		System.out.println("Fin de Semana");
		System.exit(0);
		
	}

}
