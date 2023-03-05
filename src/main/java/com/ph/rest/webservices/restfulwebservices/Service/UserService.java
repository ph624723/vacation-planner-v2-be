package com.ph.rest.webservices.restfulwebservices.Service;

import com.ph.model.EmailFailedException;
import com.ph.model.PersonNotFoundException;
import com.ph.model.UserNameInUseException;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.model.UserEntity;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.persistence.repository.UserJpaRepository;
import com.ph.rest.webservices.restfulwebservices.model.*;
import com.ph.service.AuthService;
import com.ph.service.EmailServiceImpl;
import com.ph.service.HashService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Getter
    private final String rootUserName = "root";

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    private UserJpaRepository repository;

    @Autowired
    private PersonJpaRepository personJpaRepository;

    public UserEntity getCurrentlyAuthenticatedUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            String username = ((UserDetails)auth.getPrincipal()).getUsername();
            if(repository.existsById(username)){
                return repository.findById(username).get();
            }else {
                return null;
            }
        }catch (Exception e){
            return null;
        }
    }

    public ResourceIdResponse<String> generateNewPassword(String mail) throws EmailFailedException, PersonNotFoundException {
        List<PersonEntity> matchingPersons = personJpaRepository.findByContact(mail);
        if(matchingPersons.isEmpty()){
            throw new PersonNotFoundException(mail);
        }

        UserEntity userEntity = matchingPersons.get(0).getUser();
        String passwordPlain = AuthService.generatePassword(userEntity.getName());
        userEntity.setPassword(HashService.MD5(passwordPlain));
        UserEntity userUpdated = repository.save(userEntity);

        try {
            System.out.println(emailService.sendSimpleMail(mail, "Vacation Planner - New Initial Credentials",
                    "Someone requested new login credentials using this email address.\n\n" +
                            "You can login using the following credentials:\n" +
                            "Username: " + userUpdated.getName() + "\n" +
                            "Password: " + passwordPlain + "\n\n" +
                            "Please change the password after your next login."));
        }catch (Exception e){
            throw new EmailFailedException(mail);
        }

        ResourceIdResponse<String> response = new ResourceIdResponse<>();
        response.setResourceId(userUpdated.getName());
        response.setRespondeCode(RepsonseCode.SAVE_SUCCESSFULL);
        response.setMessage("Updated user with name: "+userUpdated.getName());
        return response;
    }

    public ResourceIdResponse<String> registerUser(RegisterCredentials credentials) throws EmailFailedException, UserNameInUseException {
        credentials.setUsername(credentials.getUsername().trim());
        if(repository.existsById(credentials.getUsername())){
            throw new UserNameInUseException(credentials.getUsername());
        }
        try {
            System.out.println(emailService.sendSimpleMail(credentials.getContact(), "Vacation Planner - User Registration",
                    "Thanks for signing up to the Vacation Planner.\n\n" +
                            "Your initial login credentials will be sent in a seperate e-mail shortly."));
        }catch (Exception e){
            throw new EmailFailedException(credentials.getContact());
        }
        Person person = new Person();
        person.setContact(credentials.getContact());
        person.setName(credentials.getPersonName());
        User user = new User();
        user.setName(credentials.getUsername());
        String passwordPlain = AuthService.generatePassword(credentials.getUsername());
        user.setPassword(passwordPlain);
        user.setPerson(person);
        ResourceIdResponse<String> response = saveUser(user,null);
        System.out.println(emailService.sendSimpleMail(credentials.getContact(), "Vacation Planner - Initial Credentials",
                "Thanks for signing up to the Vacation Planner.\n\n" +
                        "You can login using the following credentials:\n" +
                        "Username: " + user.getName() + "\n" +
                        "Password: " + passwordPlain + "\n\n" +
                        "Please change the initial password after your first login."));
        try {
            System.out.println(emailService.sendSimpleMail(repository.findById(rootUserName).get().getPersonData().getContact(), "New User Registration",
                    "A new user has registered with name: "+credentials.getUsername()));
        }catch (Exception e){

        }
        return response;
    }

    public ResourceIdResponse<String> saveUser(User user, UserEntity oldUser){
        user.setPassword(HashService.MD5(user.getPassword()));
        UserEntity userEntity = user.toEntity(oldUser);

        UserEntity userUpdated = repository.save(userEntity);

        ResourceIdResponse<String> response = new ResourceIdResponse<>();
        response.setResourceId(userUpdated.getName());
        response.setRespondeCode(RepsonseCode.SAVE_SUCCESSFULL);
        response.setMessage("Saved user with name: "+userUpdated.getName());
        return response;
    }

}
