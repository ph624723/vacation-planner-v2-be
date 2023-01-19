package com.ph.rest.webservices.restfulwebservices.Service;

import com.ph.model.EmailFailedException;
import com.ph.persistence.model.EventEntity;
import com.ph.persistence.model.PersonEntity;
import com.ph.rest.webservices.restfulwebservices.model.Event;
import com.ph.service.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;

@Service
public class EventService {

    @Autowired
    EmailServiceImpl emailService;

    public void sendEmailNotifications(EventEntity event, String url, String senderName){
        for (PersonEntity person : event.getPersons()) {
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
                url;
    }
}
