package com.accenture.PocVivoReturnAndStatusOfBank.helper;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collections;

import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Updates.*;
import static java.util.Arrays.asList;

public final class ApiHelper {
    public static Bson createProjectStageFromFieldList(String fieldList) {

        if (fieldList == null || fieldList.isBlank()) {
            return null;
        }

        var bsonFields = new ArrayList<Bson>();
        var fields = new ArrayList<String>();
        Collections.addAll(fields, fieldList.split(","));


        if (fields.contains("id")) {
            fields.remove("id");
        } else {
            bsonFields.add(excludeId());
        }

        bsonFields.add(include(fields));

        return project(fields(bsonFields));

    }


    public static ArrayList<Bson> convertUpdateObjectToUpdateExpr(Object updateObject) {
        var updates = new ArrayList<Bson>();

        var fields = asList(updateObject.getClass().getDeclaredFields());
        for (var field : fields) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(JsonProperty.class)) {
                continue;
            }

            try {
                var value = field.get(updateObject);
                if (value == null) {
                    continue;
                }

                String jsonFieldName = field.getAnnotation(JsonProperty.class).value();
                updates.add(set(jsonFieldName, value));

            } catch(Exception e) {
                return new ArrayList<Bson>();
            }

        }
        return updates;
    }
}
