package com.ph.rest.webservices.restfulwebservices.Service;

import com.ph.model.EmailFailedException;
import com.ph.model.UserNameInUseException;
import com.ph.persistence.model.UserEntity;
import com.ph.persistence.repository.UserJpaRepository;
import com.ph.rest.webservices.restfulwebservices.model.*;
import com.ph.service.AuthService;
import com.ph.service.EmailServiceImpl;
import com.ph.service.HashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    private UserJpaRepository repository;

    public ResourceIdResponse<String> registerUser(RegisterCredentials credentials) throws EmailFailedException, UserNameInUseException {
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
        user.setPassword(HashService.MD5(passwordPlain));
        user.setPerson(person);
        ResourceIdResponse<String> response = saveUser(user,null);
        System.out.println(emailService.sendSimpleMail(credentials.getContact(), "Vacation Planner - Initial Credentials",
                "Thanks for signing up to the Vacation Planner.\n\n" +
                        "You can login using the following credentials:\n" +
                        "Username: " + user.getName() + "\n" +
                        "Password: " + passwordPlain + "\n\n" +
                        "Please change the initial password after your first login."));
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
