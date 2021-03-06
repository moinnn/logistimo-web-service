/*
 * Copyright © 2018 Logistimo.
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

package com.logistimo.api.controllers;

import com.logistimo.api.action.AssetConfigurationAction;
import com.logistimo.api.models.configuration.AssetSystemConfigModel;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.exception.BadRequestException;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;




/**
 * Created by naveensnair on 23/02/18.
 */
@Controller
@RequestMapping("/configuration")
public class ConfigControllerV1 {


  private AssetConfigurationAction assetConfigurationAction;

  @Autowired
  public void setAssetConfigurationAction(AssetConfigurationAction assetConfigurationAction) {
    this.assetConfigurationAction = assetConfigurationAction;
  }

  @RequestMapping(method = RequestMethod.GET)
  public
  @ResponseBody
  AssetSystemConfigModel
  getAssetConfiguration(@RequestParam(required = true) String src)
      throws ServiceException {
    AssetSystemConfigModel model;
    if (StringUtils.isEmpty(src)) {
      throw new BadRequestException("Source is required");
    }
    SecureUserDetails userDetails = SecurityUtils.getUserDetails();
    model =
        assetConfigurationAction.invoke(src, userDetails.getDomainId(), userDetails.getLocale(),
            userDetails.getTimezone());
    return model;
  }
}
