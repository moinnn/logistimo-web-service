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

package com.logistimo.returns.service;

import com.logistimo.returns.Status;
import com.logistimo.returns.models.ReturnFilters;
import com.logistimo.returns.models.UpdateStatusModel;
import com.logistimo.returns.validators.ReturnsValidator;
import com.logistimo.returns.vo.ReturnsItemBatchVO;
import com.logistimo.returns.vo.ReturnsItemVO;
import com.logistimo.returns.vo.ReturnsStatusVO;
import com.logistimo.returns.vo.ReturnsVO;
import com.logistimo.services.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created by pratheeka on 13/03/18.
 */
@Service
@EnableTransactionManagement

public class ReturnsService {


  @Autowired
  private ReturnsValidator returnsValidator;

  @Autowired
  private ReturnsDao returnsDao;

  @Transactional(transactionManager = "transactionManager")
  public ReturnsVO createReturns(ReturnsVO returnsVO) {
    returnsValidator.isQuantityValid(returnsVO.getItems(), returnsVO.getOrderId());
    List<ReturnsItemVO> returnsItemVOList = returnsVO.getItems();
    ReturnsVO updatedReturnsVo = returnsDao.saveReturns(returnsVO);
    Long returnId = updatedReturnsVo.getId();
    returnsItemVOList.forEach(returnsItemVO -> {
      returnsItemVO.setReturnsId(returnId);
      List<ReturnsItemBatchVO>
          returnsItemBatchVOList = returnsItemVO.getReturnItemBatches();
      returnsItemVO = returnsDao.saveReturnsItems(returnsItemVO);
      Long itemId = returnsItemVO.getId();
      returnsItemBatchVOList.forEach(returnsItemBatchVO -> {
        returnsItemBatchVO.setItemId(itemId);
        returnsItemBatchVO = returnsDao.saveReturnBatchItems(returnsItemBatchVO);
      });
      returnsItemVO.setReturnItemBatches(returnsItemBatchVOList);
    });
    returnsVO.setItems(returnsItemVOList);
    return returnsVO;
  }

  public List<ReturnsItemVO> getReturnsItem(Long returnId) {
    return returnsDao.getReturnedItems(returnId);
  }

  @Transactional
  public ReturnsVO updateReturnsStatus(UpdateStatusModel statusModel) throws ServiceException {
    ReturnsVO returnsVO = returnsDao.getReturnsById(statusModel.getReturnId());
    Status newStatus = statusModel.getStatus();
    Status oldStatus = returnsVO.getStatus().getStatus();
    returnsValidator.validateStatusChange(newStatus, oldStatus);
    if ((statusModel.getStatus() == Status.SHIPPED && returnsValidator
        .hasAccessToEntity(returnsVO.getCustomerId())) ||
        statusModel.getStatus() == Status.RECEIVED && returnsValidator
            .hasAccessToEntity(returnsVO.getVendorId())) {

      ReturnsStatusVO statusVO = new ReturnsStatusVO();
      statusVO.setStatus(newStatus);
      Timestamp updatedAt = new Timestamp(new Date().getTime());
      statusVO.setUpdatedAt(updatedAt);
      statusVO.setUpdatedBy(statusModel.getUserId());
      returnsVO.setStatus(statusVO);
      returnsVO.setUpdatedBy(statusModel.getUserId());
      returnsVO.setUpdatedAt(updatedAt);
      returnsVO = returnsDao.saveReturns(returnsVO);
    }
    returnsVO.setItems(getReturnsItem(returnsVO.getId()));
    return returnsVO;
  }

  public ReturnsVO getReturnsById(Long returnId) {
    return returnsDao.getReturnsById(returnId);
  }

  public List<ReturnsVO> getReturns(ReturnFilters filters) {
    return returnsDao.getReturns(filters);
  }

}
