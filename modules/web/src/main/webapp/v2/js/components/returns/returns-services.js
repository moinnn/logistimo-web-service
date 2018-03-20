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

/**
 * Created by Mohan Raja on 11/03/18.
 */

logistimoApp.service('returnsService', ['APIService', function (apiService) {
    var _items = [];
    var _order = {};

    this.setItems = function (items) {
        _items = items;
    };

    this.getItems = function() {
        var tItems = _items;
        _items = [];
        return tItems;
    };

    this.setOrder = function (order) {
        _order = order;
    };

    this.getOrder = function() {
        var tOrder = _order;
        _order = {};
        return tOrder;
    };

    this.create = function (data) {
        return apiService.post(data, '/s2/api/returns');
    };

    this.get = function (id) {
        return apiService.get('/s2/api/returns/' + id);
    };

    this.getAll = function (filters) {
        var filterArray = [];
        for (var filter in filters) {
            if(filters.hasOwnProperty(filter) && checkNotNullEmpty(filters[filter])){
                filterArray.push(filter + "=" + filters[filter]);
            }
        }
        return apiService.get('/s2/api/returns' + (filterArray.length > 0 ? '?' + filterArray.join("&") : ''));
    };

    this.ship = function (id, data) {
        return apiService.post(data, '/s2/api/returns/' + id + '/ship');
    };

    this.receive = function (id, data) {
        return apiService.post(data, '/s2/api/returns/' + id + '/receive');
    };

    this.cancel = function (id, data) {
        return apiService.post(data, '/s2/api/returns/' + id + '/cancel');
    };

}]);
