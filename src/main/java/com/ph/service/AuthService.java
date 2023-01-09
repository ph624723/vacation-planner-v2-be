package com.ph.service;

import com.ph.rest.webservices.restfulwebservices.model.AuthToken;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AuthService {
    private static final int tokenKeyLength = 50;
    private static final Duration tokenLifetime = Duration.ofMinutes(30);
    private static List<AuthToken> activeTokens = new ArrayList<>();

    public static AuthToken generateToken(String password){
        removeInvalidTokens();
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
        AuthToken token = new AuthToken();
        token.setTokenKey(key);
        token.setCreatedAt(LocalDateTime.now());
        token.setLifetime(tokenLifetime.toMinutes()+" minutes");
        activeTokens.add(token);
        return token;
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
