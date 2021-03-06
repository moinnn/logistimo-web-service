/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.proto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Type;
import java.util.Hashtable;

/**
 * Created by chitrachar on 23/06/16.
 */
public class SetupDataInputDeserializer implements JsonDeserializer<SetupDataInput> {


  @Override
  public SetupDataInput deserialize(final JsonElement json, final Type typeOfT,
                                    final JsonDeserializationContext context)
      throws JsonParseException {
    final JsonObject jsonObject = json.getAsJsonObject();
    final SetupDataInput setupDataInput = new SetupDataInput();
    if (jsonObject.get(JsonTagsZ.TYPE) != null) {
      setupDataInput.setType(jsonObject.get(JsonTagsZ.TYPE).getAsString());
    }

    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();

    if (jsonObject.get(JsonTagsZ.USER) != null) {
      String userString = gson.toJson(jsonObject.get(JsonTagsZ.USER));
      if (userString != null) {
        Hashtable user = gson.fromJson(userString, new TypeToken<Hashtable<String, String>>() {
        }.getType());
        setupDataInput.setUser(user);
      }
    }

    if (jsonObject.get(JsonTagsZ.KIOSK) != null) {
      final JsonObject jsonObjectKiosk = jsonObject.get(JsonTagsZ.KIOSK).getAsJsonObject();
      //Html5 app is sending the users, customers and vendors as an array. Converting to ; delimited string.
      if (jsonObjectKiosk.has(JsonTagsZ.USERS) && jsonObjectKiosk.get(JsonTagsZ.USERS) != null) {
        String usersString = jsonObjectKiosk.getAsJsonArray(JsonTagsZ.USERS).toString();
        if (usersString != null && !usersString.isEmpty()) {
          String str1 = usersString.replace("\"", ""); //REMOVE QUOTES
          String str2 = str1.replaceAll(CharacterConstants.COMMA, CharacterConstants.SEMICOLON);
          String str3 = str2.replace(CharacterConstants.O_SBRACKET, "");
          String str4 = str3.replace(CharacterConstants.C_SBRACKET, "");
          jsonObjectKiosk.addProperty(JsonTagsZ.USERS, str4);
        }
      }
      if (jsonObjectKiosk.has(JsonTagsZ.CUSTOMERS)
          && jsonObjectKiosk.get(JsonTagsZ.CUSTOMERS) != null) {
        String customerString = jsonObjectKiosk.getAsJsonArray(JsonTagsZ.CUSTOMERS).toString();
        if (customerString != null && !customerString.isEmpty()) {
          String str1 = customerString.replace("\"", "");
          String str2 = str1.replaceAll(CharacterConstants.COMMA, CharacterConstants.SEMICOLON);
          String str3 = str2.replace(CharacterConstants.O_SBRACKET, "");
          String str4 = str3.replace(CharacterConstants.C_SBRACKET, "");
          jsonObjectKiosk.addProperty(JsonTagsZ.CUSTOMERS, str4);
        }
      }
      if (jsonObjectKiosk.has(JsonTagsZ.VENDORS)
          && jsonObjectKiosk.get(JsonTagsZ.VENDORS) != null) {
        String vendorString = jsonObjectKiosk.getAsJsonArray(JsonTagsZ.VENDORS).toString();
        if (vendorString != null && !vendorString.isEmpty()) {
          String str1 = vendorString.replace("\"", "");
          String str2 = str1.replaceAll(CharacterConstants.COMMA, CharacterConstants.SEMICOLON);
          String str3 = str2.replace(CharacterConstants.O_SBRACKET, "");
          String str4 = str3.replace(CharacterConstants.C_SBRACKET, "");
          jsonObjectKiosk.addProperty(JsonTagsZ.VENDORS, str4);
        }
      }
      if (jsonObjectKiosk.has(JsonTagsZ.ENTITY_TAG)
          && jsonObjectKiosk.get(JsonTagsZ.ENTITY_TAG) != null) {
        String eTagsString = jsonObjectKiosk.getAsJsonArray(JsonTagsZ.ENTITY_TAG).toString();
        if (StringUtils.isNotEmpty(eTagsString)) {
          jsonObjectKiosk
              .addProperty(JsonTagsZ.ENTITY_TAG, eTagsString.replace("\"", CharacterConstants.EMPTY)
                  .replace(CharacterConstants.COMMA, CharacterConstants.SEMICOLON)
                  .replace(CharacterConstants.O_SBRACKET, CharacterConstants.EMPTY)
                  .replace(CharacterConstants.C_SBRACKET, CharacterConstants.EMPTY));
        }
      }
      String kioskString = gson.toJson(jsonObjectKiosk);
      if (kioskString != null) {
        Hashtable kiosk = gson.fromJson(kioskString, new TypeToken<Hashtable<String, String>>() {
        }.getType());
        setupDataInput.setKiosk(kiosk);
      }
    }
    return setupDataInput;
  }
}


