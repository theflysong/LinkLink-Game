package io.github.theflysong.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;


/**
 * 用户数据文件的容器 — 集中存储所有用户和 UID 计数器
 *
 * @author norbe
 * @date 2026年5月11日
 */
public class UserDataFile {

    private Map<String, User> users = new HashMap<>();
    private int uid;

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("uid", uid);
        JsonArray usersArray = new JsonArray();
        for (User user : users.values()) {
            usersArray.add(user.toJson());
        }
        obj.add("users", usersArray);
        return obj;
    }

    public static UserDataFile fromJson(JsonObject obj) {
        UserDataFile dataFile = new UserDataFile();
        dataFile.setUid(obj.get("uid").getAsInt());
        JsonArray usersArray = obj.getAsJsonArray("users");
        Map<String, User> users = new HashMap<>();
        for (int i = 0; i < usersArray.size(); i++) {
            User user = User.fromJson(usersArray.get(i).getAsJsonObject());
            users.put(user.getUsername(), user);
        }
        dataFile.setUsers(users);
        return dataFile;
    }
}
