package com.bvr;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.tools.json.JSONWriter;


public class Producer {

	/**
	 * @param args
	 *            [0] RabbitmqHost
	 */
	public static void main(String[] args) {
		System.out.println(Constants.HEADER);
		String RabbitmqHost = "localhost";
		if (args.length > 0)
			RabbitmqHost = args[0];

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(RabbitmqHost);
	
		try 
		{
			Connection connection = factory.newConnection();
			System.out.println("Connected: " + RabbitmqHost);
			Channel channel = connection.createChannel();		
			channel.exchangeDeclare(Constants.exchange, "direct", false);
			channel.queueDeclare(Constants.queue, false, false, false, null);
			channel.queueBind(Constants.queue, Constants.exchange, Constants.routingKey);
			Stats stats = new Stats();
			JSONWriter rabbitmqJson = new JSONWriter();
			// set message expiration time to 20 seconds
			BasicProperties msgProperties = new BasicProperties.Builder().expiration("20000").build();
			int msgCount=0;
			for(;;) {
				stats.Update();
				String statMsg = rabbitmqJson.write(stats);
				System.out.println(stats.toString());				
				channel.basicPublish(Constants.exchange, Constants.routingKey, msgProperties, statMsg.getBytes());
				++msgCount;
				if (System.in.available() > 0) break;
				Thread.sleep(1000);
			}
			channel.close();
			System.out.println("Done: " + msgCount + " messages sent");
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


