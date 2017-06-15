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

var invServices = angular.module('invServices', []);

invServices.factory('invService', ['$http', function ($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        getInventory: function (entityId, tag, offset, size, fetchTemp, matType, onlyNZStk, pdos) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            matType = typeof matType !== 'undefined' ? matType : 0;
            var urlStr = '/s2/api/inventory/entity/' + entityId
                + "?offset=" + offset + "&size=" + size;
            if (checkNotNullEmpty(tag)) {
                urlStr = urlStr + "&tag=" + tag;
            }
            if (checkNotNullEmpty(fetchTemp)) {
                urlStr = urlStr + "&fetchTemp=" + fetchTemp;
            }
            if (checkNotNullEmpty(matType)) {
                urlStr = urlStr + "&matType=" + matType;
            }
            if (checkNotNullEmpty(onlyNZStk)) {
                urlStr = urlStr + "&onlyNZStk=" + onlyNZStk;
            }
            if (checkNotNullEmpty(pdos)) {
                urlStr = urlStr + "&pdos=" + pdos;
            }
            return this.fetch(urlStr);
        },
        getInventoryByLocation: function (kioskTags, materialTag, offset, size, loc, pdos) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            matType = typeof matType !== 'undefined' ? matType : 0;
            var urlStr = '/s2/api/inventory/location/'
                + "?offset=" + offset + "&size=" + size;
            if (checkNotNullEmpty(kioskTags)) {
                urlStr = urlStr + "&kioskTags=" + kioskTags;
            }
            if (checkNotNullEmpty(materialTag)) {
                urlStr = urlStr + "&materialTag=" + materialTag;
            }
            if (checkNotNullEmpty(pdos)) {
                urlStr = urlStr + "&pdos=" + pdos;
            }
            urlStr = urlStr + this.getParsedLocation(loc);
            return this.fetch(urlStr);
        },
        getInventoryStartsWith: function (entityId, tag, q, offset, size) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/inventory/entity/' + entityId
                + "?offset=" + offset + "&size=" + size;
            if(checkNotNullEmpty(q)){
                urlStr = urlStr + '&startsWith='+q;
            }
            if (checkNotNullEmpty(tag)) {
                urlStr = urlStr + "&tag=" + tag;
            }
            return this.fetch(urlStr);
        },
        getInventoryDomainConfig: function (entityId) {
            if(checkNullEmpty(entityId)){
                return this.fetch('/s2/api/inventory/domain/');
            }
            return this.fetch('/s2/api/inventory/domain/' + entityId);
        },
        getMaterialInventory: function (materialId, tag, offset, size, matType, onlyNZStk, loc, pdos) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/inventory/material/' + materialId
                + "?offset=" + offset + "&size=" + size;
            if (checkNotNullEmpty(tag)) {
                urlStr = urlStr + "&tag=" + tag;
            }
            if (checkNotNullEmpty(matType)) {
                urlStr = urlStr + "&matType=" + matType;
            }
            if (checkNotNullEmpty(onlyNZStk)) {
                urlStr = urlStr + "&onlyNZStk=" + onlyNZStk;
            }
            if (checkNotNullEmpty(pdos)) {
                urlStr = urlStr + "&pdos=" + pdos;
            }
            urlStr = urlStr + this.getParsedLocation(loc);
            return this.fetch(urlStr);
        },
        getBatchMaterial: function (data, offset, size, loc) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var url = '/s2/api/inventory/batchmaterial/'
                + "?offset=" + offset + "&size=" + size;
            if (checkNotNullEmpty(data.exp)) {
                url = url + "&ebf=" + data.exp;
            } else if (checkNotNullEmpty(data.bno)) {
                url = url + "&bno=" + data.bno;
            }
            if (checkNotNullEmpty(data.mid)) {
                url = url + "&mid=" + data.mid;
            }
            if (checkNotNullEmpty(data.tt)) {
                url = url + "&ttype=" + data.tt;
            }
            if (checkNotNullEmpty(data.t)) {
                url = url + "&tag=" + data.t;
            }
            url = url + this.getParsedLocation(loc);
            return this.fetch(url);
        },
        getBatchDetail: function (mid, kid, allBatch,allocOrderId) {
            var url = '/s2/api/inventory/batchmaterialbyid/?size=50';
            if (checkNotNullEmpty(kid)) {
                url = url + "&kid=" + kid;
            }
            if (checkNotNullEmpty(mid)) {
                url = url + "&mid=" + mid;
            }
            if (checkNotNullEmpty(allBatch)) {
                url = url + "&allBatch=" + allBatch;
            }
            if (checkNotNullEmpty(allocOrderId)) {
                url = url + "&allocOrderId=" + allocOrderId;
            }
            return this.fetch(url);
        },
        checkDuplicateBatch: function(bid, mid, kid, expired) {
            var url = '/s2/api/inventory/batchmaterialcheck/?size=50';
            if (checkNotNullEmpty(bid)) {
                url = url + "&bid=" + bid;
            }
            if (checkNotNullEmpty(mid)) {
                url = url + "&mid=" + mid;
            }
            if (checkNotNullEmpty(kid)) {
                url = url + "&kid=" + kid;
            }
            url = url + "&expired=" + expired;
            return this.fetch(url);
        },
        getAbnormalStockDetail: function (data, offset, size) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var url = "/s2/api/inventory/abnormalstock/?offset=" + offset + "&size=" + size;
            if (checkNotNullEmpty(data.tt)) {
                url = url + "&ttype=" + data.tt;
            }
            if (checkNotNullEmpty(data.et)) {
                url = url + "&eventType=" + data.et;
            }
            if (checkNotNullEmpty(data.t)) {
                url = url + "&tag=" + data.t;
            }
            return this.fetch(url);
        },
        getAbnormalInventory: function (data, offset, size, inDetail) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var url = "/s2/api/inventory/abnormalstock/?offset=" + offset + "&size=" + size;
            if (checkNotNullEmpty(data.tt)) {
                url = url + "&ttype=" + data.tt;
            }
            if (checkNotNullEmpty(data.et)) {
                url = url + "&eventType=" + data.et;
            }
            if (checkNotNullEmpty(data.t)) {
                url = url + "&tag=" + data.t;
            }
            if (checkNotNullEmpty(data.entityId)) {
                url = url + "&entityId=" + data.entityId;
            }
            if (checkNotNullEmpty(data.mid)) {
                url = url + "&mid=" + data.mid;
            }
            if (checkNotNullEmpty(data.abnBeforeDate)) {
                url = url + "&abnBeforeDate=" + data.abnBeforeDate;
            }
            if (checkNotNullEmpty(inDetail)) {
                url = url + "&inDetail=" + inDetail;
            }
            url = url + this.getParsedLocation(data.loc);
            return this.fetch(url);
        },
        getInventoryByMaterial: function (kioskId, materials) {
            return this.fetch('/s2/api/inventory/inventoryByMaterial/?kioskId=' + kioskId + '&materials=' + materials);
        },
        getPredictiveStock: function (kioskId, material) {
            return this.fetch('/s2/api/inventory/predictiveStk?kioskId=' + kioskId + '&materialId=' + material);
        },
        getActualRoute: function(userId,from,to){
            return this.fetch('/s2/api/inventory/actualroute?userId=' + userId + '&from=' + from + '&to=' + to);
        },
        getInventoryHistory: function (invId){
            return this.fetch('/s2/api/inventory/invHistory?invId=' + invId);
        },
        getInvEventHistory: function (url){
            return this.fetch(url);
        },
        getParsedLocation: function (loc){
            if(checkNotNullEmpty(loc)){
                var location = {};
                if(loc.state != undefined) {
                    location['state'] = loc.state.label;
                }
                if(loc.district != undefined) {
                    location['district'] = loc.district.label;
                }
                if(loc.taluk != undefined) {
                    location['taluk'] = loc.taluk.label;
                }
                return "&loc=" + JSON.stringify(location);
            }
            return "";
        }

    }
}]);