package com.example.shalitbot.service;

import com.example.shalitbot.enums.BotState;
import com.example.shalitbot.model.User;
import com.example.shalitbot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<Long, BotState> getMapOfStates(){
       List<User> userList = userRepository.findAll();
       return userList.stream().collect(Collectors.toMap(User::getChatId, User -> BotState.valueOf(User.getTypeChoice())));
    }
    public User saveUser(User user){
        return userRepository.save(user);
    }

    public User updateUserStateByChatId(Long chatId, BotState state){
        User user = userRepository.findByChatId(chatId).orElseGet(() -> new User(chatId));
        user.setTypeChoice(state.name());
        return userRepository.save(user);
    }

    public void updateUserDbFromMap(Map<Long, BotState> map) {
        List<User> userList= userRepository.findAll();
        for (User user : userList) {
            BotState state = map.get(user.getChatId());
            if(BotState.valueOf(user.getTypeChoice()) != state){
                this.updateUserStateByChatId(user.getChatId(), state);
            }
        }
    }
}
