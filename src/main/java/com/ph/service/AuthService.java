package com.ph.service;

import com.ph.model.ToManySessionsException;
import com.ph.rest.webservices.restfulwebservices.model.AuthToken;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AuthService {
    private static final int tokenKeyLength = 50;
    @Getter
    private static final int maxConcurrentSessions = 1000;
    @Getter
    private static final Duration tokenLifetime = Duration.ofMinutes(120);
    private static List<AuthToken> activeTokens = new ArrayList<>();

    public static AuthToken generateToken(String password) throws ToManySessionsException {
        removeInvalidTokens();
        if((activeTokens.size()+1)>maxConcurrentSessions)
            throw new ToManySessionsException();

        AuthToken token = new AuthToken();
        token.setTokenKey(generatePassword(password));
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(token.getCreatedAt().plus(tokenLifetime));
        token.setTokenType("Bearer");
        activeTokens.add(token);
        return token;
    }

    public static String generatePassword(String password){
        long hash = LocalDateTime.now().hashCode();
        for (char c : password.toCharArray()) {
            hash = 31L*hash + c;
        }
        Random rd = new Random(hash);
        String key = "";
        for (int i=0;i<tokenKeyLength;i++){
            char c = (char) (rd.nextInt(92)+33);
            key += c;
        }
        key = key.replace("\\","").replaceAll("[\\'\\\"]","");
        //key = "Bearer "+key;
        return key;
    }

    public static boolean isTokenValid(String key){
        removeInvalidTokens();
        return activeTokens.stream().anyMatch(x -> key.equals("Bearer "+x.getTokenKey()));
    }

    private static void removeInvalidTokens(){
        for (int i=activeTokens.size()-1;i>=0;i--){
            if (activeTokens.get(i).getCreatedAt().plus(tokenLifetime).isBefore(LocalDateTime.now())){
                activeTokens.remove(activeTokens.get(i));
                System.out.println("Removed token");
            }
        }
    }
}
