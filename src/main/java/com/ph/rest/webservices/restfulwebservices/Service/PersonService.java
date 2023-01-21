package com.ph.rest.webservices.restfulwebservices.Service;

import com.ph.persistence.model.EventEntity;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.model.UserEntity;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.service.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PersonService {

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    UserService userService;

    @Autowired
    PersonJpaRepository personJpaRepository;

    public List<PersonEntity> getAvailablePersons(){
        UserEntity authUser = userService.getCurrentlyAuthenticatedUser();
        Set<PersonEntity> authPersons = personJpaRepository.findByRolesIsIn(authUser.getPersonData().getRoles());
        authPersons.add(authUser.getPersonData());
        return authPersons.stream().collect(Collectors.toList());
    }

    public void sendEmailNotification(PersonEntity person){
            try {
                System.out.println("try to send mail to "+person.getName());
                emailService.sendSimpleMail(person.getContact(),
                        "New contact mail address",
                        generateNotificationText(
                                person.getName()
                        ));
                System.out.println("success to send mail to "+person.getName());
            }catch (Exception e){
                System.out.println("Notification mail failed for "+person.getName());
            }

    }

    private String generateNotificationText(String name){

        return "Hello "+name+",\n\n"+
                "Your contact mail address has been changed.\n" +
                "If you received this message your new address is working.";
    }
}
