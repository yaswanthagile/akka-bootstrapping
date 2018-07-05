package com.bootstrap.akka;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.bootstrap.akka.EmailManager.EmailTask;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

public class CounterWithRouters {
	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create("EMAIL_SENDER_SYSTEM");
		Props prop = EmailManager.props();
		final ActorRef actorRef = system.actorOf(prop);

		for (int i = 0; i < 10; i++) {
			Thread t = new Thread() {
				public void run() {
					for (int k = 0; k < 20; k++) {
						EmailTask task = new EmailTask("To_" + k, "From_" + k, "Content_" + k);
						actorRef.tell(task, ActorRef.noSender());
					}
				}
			};
			t.start();
		}
		

		Scanner sc=new Scanner(System.in);  
		while(true) {
			String input = sc.nextLine();
			if("r".equalsIgnoreCase(input)) {
				actorRef.tell(EmailManager.STATUS, ActorRef.noSender());
			}
		}
	}
	
}

/**
 * Worker actor to send out emails
 * @author yaswanth
 *
 */
class EmailSender extends AbstractActor {
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(EmailTask.class, task -> {
			System.out.println("Email Sent to : " + task.getToEmail());
			getSender().tell(EmailManager.EMAIL_SENT, getSelf());
		}).build();
	}

	public static Props props() {
		return Props.create(EmailSender.class);
	}
}

/**
 * Email sender manager
 * @author yaswanth
 *
 */
class EmailManager extends AbstractActor {

	private int emailsSent = 0;
	public static final Object EMAIL_SENT = new Object();
	public static final Object STATUS = new Object();

	private final Router router;
	{
		List<Routee> routees = new ArrayList<Routee>();
		for (int i = 0; i < 5; i++) {
			ActorRef r = getContext().actorOf(EmailSender.props());
			getContext().watch(r);
			routees.add(new ActorRefRoutee(r));
		}
		router = new Router(new RoundRobinRoutingLogic(), routees);
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().matchEquals(EMAIL_SENT, arg1 -> {
			Thread.sleep(30);
			emailsSent++;
		}).matchEquals(STATUS, arg1 -> {
			System.out.println("Emails sent : " + emailsSent);
		}).match(EmailTask.class, item -> {
			router.route(item, getSelf());
		}).build();
	}

	public static Props props() {
		return Props.create(EmailManager.class);
	}

	public static final class EmailTask {
		private final String toEmail;
		private final String fromEmail;
		private final String contentOfEmail;

		public EmailTask(String toEmail, String fromEmail, String contentOfEmail) {
			this.toEmail = toEmail;
			this.fromEmail = fromEmail;
			this.contentOfEmail = contentOfEmail;
		}

		public String getToEmail() {
			return toEmail;
		}

		public String getFromEmail() {
			return fromEmail;
		}

		public String getContentOfEmail() {
			return contentOfEmail;
		}
	}
}