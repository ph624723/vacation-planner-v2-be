package com.ph.rest.webservices.restfulwebservices.Service;

import com.ph.model.EmailFailedException;
import com.ph.persistence.model.CommentEntity;
import com.ph.persistence.model.EventEntity;
import com.ph.persistence.model.PersonEntity;
import com.ph.rest.webservices.restfulwebservices.model.Event;
import com.ph.service.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

@Service
public class EventService {

    @Autowired
    EmailServiceImpl emailService;

    public void sendEmailNotifications(EventEntity event, Set<PersonEntity> toPersons, String url, String senderName){
        for (PersonEntity person : toPersons) {
            try {
                System.out.println("try to send mail to "+person.getName());
                emailService.sendSimpleMail(person.getContact(),
                        person.getName().equals(senderName) ? "Your planned event" : senderName+" planned an event with you",
                        generateNotificationText(
                                person.getName(),
                                event.getDescription(),
                                event.getStartDate(),
                                event.getEndDate(),
                                url,
                                senderName
                        ));
                System.out.println("success to send mail to "+person.getName());
            }catch (Exception e){
                System.out.println("Notification mail failed for "+person.getName());
            }
        }
    }

    private String generateNotificationText(String name, String description, Date start, Date end, String url, String senderName){

        return "Hello "+name+",\n\n"+
                (name.equals(senderName) ? "Your event has been stored.\n" : senderName+" has proposed an event with your participation.\n") +
                "From: " + start +"\n"+
                "To: " + end +"\n"+
                "Description: " + description +"\n\n"+
                "Details can be found on your page inside the Vacation Planner:\n" +
                url +"\n" +
                "Please check everything and accept the event if you want to participate." + "\n" +
                "This way planning can progress to the next steps as soon as possible.";
    }

    public void sendAllAcceptedMail(EventEntity event, String url){
        for (PersonEntity person : event.getPersons()) {
            try {
                emailService.sendSimpleMail(person.getContact(),
                        "All participants have accepted your event!",
                        "Hello "+person.getName()+",\n\n"+
                        "Good news! All participants have accepted your event with the following details:\n" +
                        "From: " + event.getStartDate() +"\n"+
                        "To: " + event.getEndDate() +"\n"+
                        "Description: " + event.getDescription() +"\n\n"+
                        "If you are content with your planning, you can now finalize the event and create a fixed absence for everyone.\n" +
                        url);
                System.out.println("success to send mail to "+person.getName());
            }catch (Exception e){
                System.out.println("Notification mail failed for "+person.getName());
            }
        }
    }

    public void sendNewCommentMail(EventEntity event, String url, CommentEntity comment){
        for (PersonEntity person : event.getPersons()) {
            if(person.equals(comment.getPerson())) continue;
            try {
                emailService.sendSimpleMail(person.getContact(),
                        "Vacation Planner - New Comment on Event",
                        "Hello "+person.getName()+",\n\n"+
                                comment.getPerson().getName() + (comment.getReplyTo() != null && person.equals(comment.getReplyTo().getPerson()) ?
                                " has replied to your comment:\n" +
                                "> "+ person.getName() + ", " + comment.getReplyTo().getCreated() + "\n" +
                                "\t"+ comment.getReplyTo().getContent()
                                : " has commented on your event:") + "\n" +
                                "> "+ comment.getPerson().getName() + ", " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(comment.getCreated()) + "\n" +
                                "\t"+ comment.getContent() + "\n\n" +
                                "Event:\n" +
                                "From: " + event.getStartDate() +"\n"+
                                "To: " + event.getEndDate() +"\n"+
                                "Description: " + event.getDescription() +"\n\n"+
                                "Details can be found on your page inside the Vacation Planner:\n" +
                                url +"\n" +
                                "Please check everything and accept the event if you want to participate." + "\n" +
                                "This way planning can progress to the next steps as soon as possible.");
                System.out.println("success to send mail to "+person.getName());
            }catch (Exception e){
                System.out.println("Notification mail failed for "+person.getName());
            }
        }
    }
}
