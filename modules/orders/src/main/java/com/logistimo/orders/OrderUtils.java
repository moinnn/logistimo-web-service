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

package com.logistimo.orders;

import com.logistimo.config.models.FieldsConfig;
import com.logistimo.exception.LogiException;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.orders.models.UpdatedOrder;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.models.shipments.ShipmentItemBatchModel;
import com.logistimo.models.shipments.ShipmentItemModel;
import com.logistimo.models.shipments.ShipmentMaterialsModel;
import com.logistimo.proto.FulfillmentBatchMaterialRequest;
import com.logistimo.proto.FulfillmentMaterialRequest;
import com.logistimo.proto.UpdateOrderStatusRequest;
import com.logistimo.shipments.ShipmentStatus;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.shipments.service.IShipmentService;
import com.logistimo.shipments.service.impl.ShipmentService;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.logger.XLog;
import com.logistimo.exception.InvalidServiceException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;


/**
 * Created by charan on 05/03/17.
 */
public class OrderUtils {

  private static final XLog xLogger = XLog.getLog(OrderUtils.class);

  // Update an order
  public static UpdatedOrder updateOrder(IOrder o, DomainConfig dc) throws LogiException {
    if (o == null) {
      throw new ServiceException("Order not specified");
    }
    // Get the OMS
    OrderManagementService
        oms =
        Services.getService(OrderManagementServiceImpl.class, dc.getLocale());
    return oms.updateOrder(o, SourceConstants.WEB);
  }

  // Update an order's status - used only in viewOrder.jsp
  public static UpdatedOrder updateOrderStatus(Long orderId, String newStatus,
                                               String updatingUserId, String message,
                                               List<String> recevingUserIds, DomainConfig dc,
                                               int source)
      throws ObjectNotFoundException, ServiceException {
    OrderManagementService
        oms =
        Services.getService(OrderManagementServiceImpl.class, dc.getLocale());
    IOrder o = oms.getOrder(orderId);
    o.setStatus(
        newStatus); // required, given isPostingInventoryTransRequired() requires an order with the current status
    return oms
        .updateOrderStatus(orderId, newStatus, updatingUserId, message, recevingUserIds, source);
  }

  // Update an order's status - to support old apk
  public static UpdatedOrder updateOrderStatus(Long orderId, String newStatus,
                                               String updatingUserId, String message,
                                               DomainConfig dc, int source,
                                               ResourceBundle backendMessages)
      throws ObjectNotFoundException, ServiceException {
    OrderManagementService
        oms =
        Services.getService(OrderManagementServiceImpl.class, dc.getLocale());
    IOrder o = oms.getOrder(orderId, true);
    UpdatedOrder uo = new UpdatedOrder();
    if (IOrder.FULFILLED.equals(newStatus)) {
      IShipmentService ss;
      boolean updated = false;
      try {
        ss = Services.getService(ShipmentService.class);
        List<IShipment> shipments = ss.getShipmentsByOrderId(orderId);
        if (shipments != null && !shipments.isEmpty()) {
          IShipment s = shipments.get(0);
          updated = ss.fulfillShipment(s.getShipmentId(), updatingUserId).status;
        }
      } catch (Exception e) {
        uo.inventoryError = true;
        uo.message = backendMessages.getString("error.unabletofulfilorder");
      }
      if (updated) {
        uo.order = oms.getOrder(orderId, true);
      } else {
        uo.inventoryError = true;
        uo.message = backendMessages.getString("error.unabletofulfilorder");
      }
    } else if (IOrder.COMPLETED.equals(newStatus)) {
      oms.shipNow(o, null, null, null, null, updatingUserId, null);
      if (message != null && !message.isEmpty()) {
        oms.addMessageToOrder(orderId, message, updatingUserId);
      }
      uo.order = oms.getOrder(orderId, true);
    } else {
      o.setStatus(
          newStatus); // required, given isPostingInventoryTransRequired() requires an order with the current status
      uo =
          oms.updateOrderStatus(orderId, newStatus, updatingUserId, message, null, source, null,
              null);
    }
    return uo;
  }

  public static UpdatedOrder updateOrderStatus(Long orderId, String newStatus,
                                               String updatingUserId,
                                               String message, List<String> recevingUserIds,
                                               DomainConfig dc,
                                               int source, String reason)
      throws ObjectNotFoundException, ServiceException {
    OrderManagementService
        oms =
        Services.getService(OrderManagementServiceImpl.class, dc.getLocale());
    IOrder o = oms.getOrder(orderId);
    o.setStatus(
        newStatus); // required, given isPostingInventoryTransRequired() requires an order with the current status
    return oms
        .updateOrderStatus(orderId, newStatus, updatingUserId, message, recevingUserIds, source,
            null, reason);
  }

  public static UpdatedOrder updateOrdStatus(UpdateOrderStatusRequest uosReq, DomainConfig dc,
                                             int source, ResourceBundle backendMessages)
      throws ObjectNotFoundException, ServiceException {
    OrderManagementService
        oms =
        Services.getService(OrderManagementServiceImpl.class, dc.getLocale());
    IOrder o = oms.getOrder(uosReq.tid, true);
    UpdatedOrder uo = new UpdatedOrder();
    if (IOrder.FULFILLED.equals(uosReq.ost)) {
      IShipmentService ss;
      boolean updated = false;
      try {
        ShipmentMaterialsModel smm = getShipmentMaterialsModel(uosReq);
        if (smm == null) {
          uo.inventoryError = true;
          uo.message = backendMessages.getString("error.unabletofulfilorder");
          return uo;
        }
        ss = Services.getService(ShipmentService.class, dc.getLocale());
        updated = ss.fulfillShipment(smm, uosReq.uid).status;
      } catch (Exception e) {
        uo.inventoryError = true;
        uo.message = backendMessages.getString("error.unabletofulfilorder");
      }
      if (updated) {
        uo.order = oms.getOrder(uosReq.tid, true);
      } else {
        uo.inventoryError = true;
        uo.message = backendMessages.getString("error.unabletofulfilorder");
      }
    } else if (IOrder.COMPLETED.equals(uosReq.ost)) {
      oms.shipNow(o, uosReq.trsp, uosReq.trid, null, uosReq.ead, uosReq.uid, uosReq.pksz);
      if (uosReq.ms != null && !uosReq.ms.isEmpty()) {
        oms.addMessageToOrder(uosReq.tid, uosReq.ms, uosReq.uid);
      }
      uo.order = oms.getOrder(uosReq.tid, true);
    } else {
      o.setStatus(
          uosReq.ost); // required, given isPostingInventoryTransRequired() requires an order with the current status
      uo =
          oms.updateOrderStatus(uosReq.tid, uosReq.ost, uosReq.uid, uosReq.ms, null, source, null,
              uosReq.rsnco);
    }
    return uo;
  }

  private static ShipmentMaterialsModel getShipmentMaterialsModel(UpdateOrderStatusRequest uosReq) {
    if (uosReq.mt == null || uosReq.mt.isEmpty()) {
      return null;
    }
    List<FulfillmentMaterialRequest> mt = uosReq.mt;
    ShipmentMaterialsModel smm = new ShipmentMaterialsModel();
    List<ShipmentItemModel> simList = new ArrayList<>(1);
    for (FulfillmentMaterialRequest material : mt) {
      ShipmentItemModel sim = new ShipmentItemModel();
      sim.mId = material.mid;
      sim.frsn = material.rsnpf;
      sim.fmst = material.fmst;
      List<FulfillmentBatchMaterialRequest> bt = material.bt;
      if (bt != null && !bt.isEmpty()) {
        List<ShipmentItemBatchModel> sibmList = new ArrayList<>(1);
        for (FulfillmentBatchMaterialRequest bm : bt) {
          ShipmentItemBatchModel sibm = new ShipmentItemBatchModel();
          sibm.id = bm.bid;
          sibm.fq = bm.q;
          if (BigUtil.lesserThanZero(sibm.fq)) {
            xLogger.severe(
                "Exception while updating order status to fulfilled. Batch ID: {0}, Fulfilled quantity: {1}",
                sibm.id, sibm.fq);
            return null;
          }
          if (bm.fmst != null && !bm.fmst.isEmpty()) {
            sibm.fmst = bm.fmst;
          }
          if (bm.rsnpf != null && !bm.rsnpf.isEmpty()) {
            sibm.frsn = bm.rsnpf;
          }
          sibmList.add(sibm);
        }
        sim.bq = sibmList;
        sim.isBa = true;
      }
      if (material.q != null) {
        sim.fq = material.q;
        if (BigUtil.lesserThanZero(sim.fq)) {
          xLogger.severe(
              "Exception while updating order status to fulfilled. Shipment ID: {0}, Fulfilled quantity {1}",
              sim.sid, sim.fq);
          return null;
        }
      }
      simList.add(sim);

    }
    smm.items = simList;
    smm.sId = uosReq.sid;
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
    smm.afd = sdf.format(uosReq.dar);
    smm.userId = uosReq.uid;
    smm.msg = uosReq.ms;
    if (uosReq.tid != null) {
      smm.isOrderFulfil = true;
    }
    return smm;
  }

  public static UpdatedOrder updateShpStatus(UpdateOrderStatusRequest uosReq, DomainConfig dc,
                                             int source, ResourceBundle backendMessages,
                                             String previousUpdatedTime)
      throws ObjectNotFoundException, ServiceException, LogiException {
    IShipmentService ss = Services.getService(ShipmentService.class, dc.getLocale());
    OrderManagementService
        oms =
        Services.getService(OrderManagementServiceImpl.class, dc.getLocale());
    IShipment s = ss.getShipment(uosReq.sid);
    ShipmentStatus shipmentStatus;
    if (ShipmentStatus.SHIPPED.toString().equals(uosReq.ost)) {
      shipmentStatus = ShipmentStatus.SHIPPED;
    } else if (ShipmentStatus.CANCELLED.toString().equals(uosReq.ost)) {
      shipmentStatus = ShipmentStatus.CANCELLED;
    } else if (ShipmentStatus.CONFIRMED.toString().equals(uosReq.ost)) {
      shipmentStatus = ShipmentStatus.CONFIRMED;
    } else if (ShipmentStatus.FULFILLED.toString().equals(uosReq.ost)) {
      shipmentStatus = ShipmentStatus.FULFILLED;
    } else {
      throw new InvalidServiceException("Invalid status to update");
    }
    boolean updated = false;
    UpdatedOrder uo = new UpdatedOrder();
    if (shipmentStatus.toString().equals(ShipmentStatus.FULFILLED.toString())) {
      try {
        ShipmentMaterialsModel smm = getShipmentMaterialsModel(uosReq);
        if (smm == null) {
          uo.inventoryError = true;
          uo.message = backendMessages.getString("error.unabletofulfilorder");
          return uo;
        }
        ss = Services.getService(ShipmentService.class, dc.getLocale());
        updated = ss.fulfillShipment(smm, uosReq.uid).status;
      } catch (Exception e) {
        uo.inventoryError = true;
        uo.message = backendMessages.getString("error.unabletofulfilorder");
      }
    } else {
      if (uosReq.pksz != null && !uosReq.pksz.isEmpty()) {
        ss.updateShipmentData("ps", uosReq.pksz, previousUpdatedTime, uosReq.sid, uosReq.uid);
      }
      if (uosReq.trsp != null && !uosReq.trsp.isEmpty()) {
        ss.updateShipmentData("tpName", uosReq.trsp, previousUpdatedTime, uosReq.sid, uosReq.uid);
      }
      if (uosReq.trid != null && !uosReq.trid.isEmpty()) {
        ss.updateShipmentData("tId", uosReq.trid, previousUpdatedTime, uosReq.sid, uosReq.uid);
      }
      if (uosReq.ead != null) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        String eadStr = sdf.format(uosReq.ead);
        ss.updateShipmentData("date", eadStr, previousUpdatedTime, uosReq.sid, uosReq.uid);
      }
      updated =
          ss.updateShipmentStatus(uosReq.sid, shipmentStatus, uosReq.ms, uosReq.uid, uosReq.rsnco).status;
    }

    if (updated) {
      uo.order = oms.getOrder(s.getOrderId(), true);
    } else {
      uo.inventoryError = true;
      uo.message = "Error while updating shipment status";
    }
    return uo;
  }

  // Render a custom field view (in HTML) - return a <div>
  public static String renderOrderFieldsInHTML(FieldsConfig fc, Map<String, String> fieldMap,
                                               boolean readOnly, String statusCode, Locale locale) {
    String str = "";
    // Viewable block
    if (readOnly) {
      if (fieldMap == null || fieldMap.isEmpty()) {
        return str;
      }
      str = "<div style=\"margin-top:5px;\"><table>";
      for (String key : fieldMap.keySet()) {
        FieldsConfig.Field f = null;
        if (fc != null) {
          f = fc.getField(key);
        }
        String name;
        if (f == null) {
          name = key;
        } else {
          name = f.name;
        }
        str += "<tr><td>" + name + ":</td><td>" + fieldMap.get(key) + "</td></tr>";
      }
      str += "</table></div>";
      return str;
    }
    // Writable form
    if (fc == null || fc.isEmpty()) {
      return "";
    }
    // Get the HTML blocks for each order status
    for (int i = 0; i < IOrder.STATUSES.length; i++) {
      // Get the fields relevant to this status
      List<FieldsConfig.Field> list = fc.getByStatus(IOrder.STATUSES[i]);
      if (list == null || list.isEmpty()) {
        continue;
      }
      if (statusCode != null && !statusCode
          .equals(IOrder.STATUSES[i])) // in case a status code is passed, only process that code
      {
        continue;
      }
      String id = "fields_" + IOrder.STATUSES[i];
      str += "<div id=\"" + id + "\"" + "style=\"margin-top:5px;display:none;\"><table>";
      Iterator<FieldsConfig.Field> fields = list.iterator();
      boolean hasMandatory = false;
      while (fields.hasNext()) {
        FieldsConfig.Field f = fields.next();
        if (f != null) {
          if (!hasMandatory) {
            hasMandatory = f.mandatory;
          }
          String key = f.getId();
          String name;
          String value = null;
          if (fieldMap != null) {
            value = fieldMap.get(f.getId());
          }
          name = f.name;
          str +=
              "<tr><td>" + (f.mandatory ? "<b>" : "") + name + (f.mandatory ? "</b>" : "")
                  + ":</td><td>";
          if (readOnly) {
            str += (value == null ? "" : value);
          } else {
            if ("text".equals(f.type)) {
              String idStr = key + "_" + IOrder.STATUSES[i];
              str +=
                  "<input type=\"text\" id=\"" + idStr + "\" name=\"" + key + "\" value=\"" + (
                      value == null ? "" : value) + "\"" + (f.maxSize > 0 ? " maxlength=\""
                      + f.maxSize + "\"" : "");
              if (f.useInTemplates) { // add javascript to ensure message is updated
                String replaceKey = "%" + key + "%";
                str +=
                    " onblur=\"if ( this.value != '' ) updateOrderSendMessage('" + replaceKey
                        + "',this.value,document.getElementById('message1').value,null)\"";
              }
              str += "/>";
            } // else do nothing for now; TODO: later handle checkbox and radio buttons
          }
          str += "</td></tr>";
        }
      }
      str += "</table>";
      if (hasMandatory) {
        try {
          if (locale != null) {
            ResourceBundle r = Resources.get().getBundle("Messages", locale);
            str += "<i>(" + r.getString("itemsmandatorymsg") + ")</i>";
          }
        } catch (Exception e) {
          // do nothing
        }
      }
      str += "</div>";
    }
    return str;
  }

  // Get custom field map from request
  public static Map<String, String> getOrderFields(HttpServletRequest req, String statusCode,
                                                   FieldsConfig fc) {
    if (req == null || statusCode == null || statusCode.isEmpty() || fc == null || fc.isEmpty()) {
      return null;
    }
    Map<String, String> map = new HashMap<>();
    List<FieldsConfig.Field> fields = fc.getByStatus(statusCode);
    for (FieldsConfig.Field f : fields) {
      String key = f.getId();
      String val = req.getParameter(key);
      if (val != null) {
        try {
          val = URLDecoder.decode(val, "UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
          xLogger.warn("Unsupported encoding exception: {0}", e.getMessage());
        }
        if (val.isEmpty()) {
          val = null;
        }
        map.put(key, val);
      }
    } // end while
    return map;
  }


  // Check if setting re-order level is allowed
  public static boolean isReorderAllowed(String invModel) {
    return (invModel == null || invModel.isEmpty() ||
        IInvntry.MODEL_KANBAN.equals(invModel));
  }

  public static String getStatusDisplay(String status, Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    if (messages == null) {
      return "unknown";
    }
    String name = "";
    if (IOrder.CANCELLED.equals(status)) {
      name = messages.getString("order.cancelled");
    } else if (IOrder.CHANGED.equals(status)) {
      name = messages.getString("order.changed");
    } else if (IOrder.COMPLETED.equals(status)) {
      name = messages.getString("order.shipped");
    } else if (IOrder.CONFIRMED.equals(status)) {
      name = messages.getString("order.confirmed");
    } else if (IOrder.FULFILLED.equals(status)) {
      name = messages.getString("order.fulfilled");
    } else if (IOrder.PENDING.equals(status)) {
      name = messages.getString("order.pending");
    } else if (IOrder.BACKORDERED.equals(status)) {
      name = messages.getString("order.backordered");
    } else {
      name = "unknown";
    }

    return name;
  }

  public static String getShipmentStatusDisplay(ShipmentStatus status, Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    if (messages == null) {
      return "unknown";
    }
    String name = "";
    if (ShipmentStatus.CANCELLED.equals(status)) {
      name = messages.getString("order.cancelled");
    } else if (ShipmentStatus.FULFILLED.equals(status)) {
      name = messages.getString("order.fulfilled");
    } else if (ShipmentStatus.PENDING.equals(status) || ShipmentStatus.OPEN.equals(status)) {
      name = messages.getString("order.pending");
    } else if (ShipmentStatus.SHIPPED.equals(status)) {
      name = messages.getString("order.shipped");
    } else {
      name = "unknown";
    }

    return name;
  }
}