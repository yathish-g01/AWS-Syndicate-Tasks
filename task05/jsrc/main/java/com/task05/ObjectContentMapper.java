package com.task05;

import com.google.gson.Gson;
import java.util.Map;

public class ObjectContentMapper {
    private final Gson gson;

    public ObjectContentMapper(Gson gson) {
        this.gson = gson;
    }

    public Map<String, String> objectToContent(Object obj) throws ClassCastException{
        Map<String,String> map = (Map<String,String>) gson.fromJson(gson.toJson(obj), Map.class);
        return map;
    }


}

