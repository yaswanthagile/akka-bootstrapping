package com.bootstrap.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;

/**
 * Class that illustrates, all the messages to an actor runs sequentially
 * 
 * @author yaswanth
 */
public class CounterActor extends AbstractActor {

	private Integer counter = new Integer(0);

	private static class PrintResult {

	}

	public static final PrintResult result = new PrintResult();

	@Override
	public Receive createReceive() {
		// TODO Auto-generated method stub
		return receiveBuilder().match(Integer.class, i -> {
			counter += i;
			System.out.println(counter);
		}).matchEquals(result, r -> {
			System.out.println(counter);
		}).build();
	}

	private CounterActor() {

	}

	public static Props props() {
		return Props.create(CounterActor.class);
	}

	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create("COUNTER_SYSTEM");
		final ActorRef ref = system.actorOf(CounterActor.props());

		for (int i = 0; i < 10; i++) {
			Thread t = new Thread() {
				public void run() {
					for (int k = 0; k < 20; k++)
						// TODO Auto-generated method stub
						ref.tell(new Integer(1), ActorRef.noSender());
				}
			};
			t.start();
		}

		// ref.tell(CounterActor.result, ActorRef.noSender());
		ref.tell(PoisonPill.getInstance(), ActorRef.noSender());
		// ref.tell(PoisonPill.getInstance(), ActorRef.noSender());
	}
}
