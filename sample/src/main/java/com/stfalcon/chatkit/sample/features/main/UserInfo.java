package com.stfalcon.chatkit.sample.features.main;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {

    public List<String> messages = new ArrayList<>();
    public String login;
    public Long id;

    public UserInfo(List<String> messages, String login, Long id) {
        this.messages = messages;
        this.login = login;
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        UserInfo other = obj instanceof UserInfo ? ((UserInfo) obj) : null;
        if (other == null)
            return false;
        return this.id == other.id;
    }
}
