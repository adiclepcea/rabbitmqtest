package org.clepcea.rabbitmqtest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * Hello world!
 *
 */
public class App 
{
	final Connection connection;
	final String exchangeName = "dev-exchange";
	final String vhostName = "dev-vhost";
	final String queueName = "TestQueue";
	
	public App() throws IOException, TimeoutException{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		factory.setUsername("admin");
		factory.setPassword("Cucubau89");
		factory.setVirtualHost(vhostName);
		
		connection = factory.newConnection();
	}
	
	public void write()  throws IOException, TimeoutException{
		
		Channel channel = connection.createChannel();
		
		channel.exchangeDeclare(exchangeName, "direct",true);	
		
		channel.queueDeclare(queueName, true, false, false, null);
		
		for(int i=0;i<10;i++){
			String message = "Hello there, this is message number "+(i+1);
			channel.basicPublish("", queueName, null, message.getBytes());
			System.out.println(String.format("Wrote: %s", message));
		}
		
		channel.close();
	}
	
	public void close() throws IOException{
		if(connection!=null){
			connection.close();
		}
	}
	public void read() throws IOException, TimeoutException, InterruptedException{
		final Channel channel = connection.createChannel();
		
		//channel.exchangeDeclare(exchangeName, "direct");
		
		channel.queueDeclare(queueName,true,false,false,null);
		
		channel.queueBind(queueName, exchangeName, queueName);		
		
		channel.basicConsume(queueName, new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag,
						Envelope envelope,AMQP.BasicProperties properties,
						byte[] body) throws IOException{
				String routingKey = envelope.getRoutingKey();
				String contentType = properties.getContentType();
				System.out.println("Received: "+new String(body));
				long deliveryTag = envelope.getDeliveryTag();
				channel.basicAck(deliveryTag, false);
			}
		});
		
		Thread.sleep(1000);
		
		channel.close();
	}
	
    public static void main( String[] args ) throws IOException, TimeoutException, InterruptedException
    {
    	App app = new App();
    	//app.write();
    	app.read();
    }
}
