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

package com.logistimo.returns.entity.values;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Mohan Raja
 */
@Embeddable
public class ReturnsReceived {

  @Column(name = "received_quantity")
  private BigDecimal quantity = BigDecimal.ZERO;

  @Column(name = "received_material_status")
  private String materialStatus;

  @Column(name = "discrepancy_reason")
  private String reason;

  @SuppressWarnings("unused")
  private ReturnsReceived() {
    //Added to support JPA
  }

  public ReturnsReceived(BigDecimal quantity, String materialStatus, String reason) {
    this.quantity = quantity == null ? BigDecimal.ZERO : quantity;
    this.materialStatus = materialStatus;
    this.reason = reason;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public String getMaterialStatus() {
    return materialStatus;
  }

  public String getReason() {
    return reason;
  }

}