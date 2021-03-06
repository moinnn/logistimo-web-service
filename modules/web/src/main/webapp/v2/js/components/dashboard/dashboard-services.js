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

var dashboardServices = angular.module('dashboardServices', []);
dashboardServices.factory('dashboardService', ['APIService','$q', function (apiService,$q) {
    var queue = {};
    return {
        getMonthlyStats: function (numMonths) {
            return apiService.get('/s/dashboard?action=getmonthlystats&months=' + numMonths);
        },
        create: function (data) {
            return apiService.post(data, '/s2/api/dashboard/');
        },
        getById: function (dbId, withConfig) {
            return apiService.get('/s2/api/dashboard/' + dbId + (withConfig ? ('?wc=' + withConfig) : ''));
        },
        getAll: function () {
            return apiService.get('/s2/api/dashboard/all/');
        },
        getAssetStatus: function(filter, monitoringType, assetType, entityTag, excludeEntityTag, period, status, level, skipCache) {
          var url = '';
            if (checkNotNullEmpty(filter)) {
                url = '?location=' + filter;
                if (checkNotNullEmpty(level)) {
                    url += '&level=' + level;
                }
            }
            if (checkNotNullEmpty(monitoringType)) {
                url += (url == '' ? '?' : '&') + 'monitoringType=' + monitoringType;
            }
            if (checkNotNullEmpty(assetType)) {
                url += (url == '' ? '?' : '&') + 'assetType=' + assetType;
            }
            if (checkNotNullEmpty(entityTag)) {
                url += (url == '' ? '?' : '&') + 'eTag=' + entityTag;
            }
            if (checkNotNullEmpty(excludeEntityTag)) {
                url += (url == '' ? '?' : '&') + 'eeTag=' + excludeEntityTag;
            }
            if (checkNotNullEmpty(status) && (status * 1) > 0) {
                url += (url == '' ? '?' : '&') + 'status=' + (status * 1 - 1);
            }
            if (checkNotNullEmpty(period)) {
                url += (url == '' ? '?' : '&') + 'period=' + period;
            }
            if (checkNotNullEmpty(skipCache)) {
                url += (url == '' ? '?' : '&') + 'skipCache=' + skipCache;
            }
            return apiService.get('/s2/api/dashboards/assets/status' + url)
        },
        get: function (filter, level, exFilter, exType, period, tPeriod,aType, eTag, date, excludeETag, skipCache) {
            var url = '';
            if (checkNotNullEmpty(filter)) {
                url = '?filter=' + filter;
                if (checkNotNullEmpty(level)) {
                    url += '&level=' + level;
                }
            }
            if (checkNotNullEmpty(exFilter)) {
                url += (url == '' ? '?' : '&') + 'extraFilter=' + exFilter;
                if (checkNotNullEmpty(exType)) {
                    url += '&exType=' + exType;
                }
            }
            url += (url == '' ? '?' : '&') + 'period=' + (period || 0);
            if (checkNotNullEmpty(tPeriod)) {
                url += (url == '' ? '?' : '&') + 'tPeriod=' + tPeriod;
            }
            if (checkNotNullEmpty(eTag)) {
                url += (url == '' ? '?' : '&') + 'eTag=' + eTag;
            }
            if (checkNotNullEmpty(aType)) {
                url += (url == '' ? '?' : '&') + 'aType=' + aType;
            }
            if (checkNotNullEmpty(date)) {
                url += (url == '' ? '?' : '&') + 'date=' + date;
            }
            if(checkNotNullEmpty(excludeETag)) {
                url += (url == ''? '?' : '&') + 'excludeETag=' + excludeETag;
            }
            if(checkNotNullEmpty(skipCache)) {
                url += (url == ''? '?' : '&') + 'skipCache=' + skipCache;
            }
            var defer = $q.defer();
            if(!queue[url]){
                queue[url] = [];
            }
            queue[url].push(defer);
            if(queue[url].length == 1) {
                apiService.get('/s2/api/dashboard/' + url).then(function (data) {
                    queue[url].forEach(function(defer){
                        defer.resolve(data);
                    })
                }).catch(function (err) {
                    queue[url].forEach(function(defer){
                        defer.reject(err);
                    })
                }).finally(function(){
                    queue[url] = [];
                });
            }
            return defer.promise;
        },
        getInv: function (state,district,period,eTag,skipCache) {
            var url = '';
            if(checkNotNullEmpty(state)){
                url = '?state=' + state;
            }
            if(checkNotNullEmpty(district)){
                url = url+(checkNullEmpty(url)?'?':'&')+'district=' + district;
            }
            if(checkNotNullEmpty(period)){
                url = url+(checkNullEmpty(url)?'?':'&')+'period=' + period;
            }
            if (checkNotNullEmpty(eTag)) {
                url += (url == '' ? '?' : '&') + 'eTag=' + eTag;
            }
            if(checkNotNullEmpty(skipCache)) {
                url += (url == ''? '?' : '&') + 'skipCache=' + skipCache;
            }
            return apiService.get('/s2/api/dashboard/inv' + url);
        },
        getSessionData: function(filter, level, exFilter, exType, period, eTag, date, type, skipCache) {
            var url = '';
            if (checkNotNullEmpty(filter)) {
                url = '?filter=' + filter;
                if (checkNotNullEmpty(level)) {
                    url += '&level=' + level;
                }
            }
            if (checkNotNullEmpty(exFilter)) {
                url += (url == '' ? '?' : '&') + 'extraFilter=' + exFilter;
                if (checkNotNullEmpty(exType)) {
                    url += '&exType=' + exType;
                }
            }
            if (checkNotNullEmpty(period)) {
                url += (url == '' ? '?' : '&') + 'period=' + period;
            }
            if (checkNotNullEmpty(eTag)) {
                url += (url == '' ? '?' : '&') + 'eTag=' + eTag;
            }
            if (checkNotNullEmpty(date)) {
                url += (url == '' ? '?' : '&') + 'date=' + date;
            }
            if (checkNotNullEmpty(type)) {
                url += (url == '' ? '?' : '&') + 'type=' + type;
            }
            if(checkNotNullEmpty(skipCache)) {
                url += (url == ''? '?' : '&') + 'skipCache=' + skipCache;
            }
            return apiService.get('/s2/api/dashboard/session' + url);
        },
        getPredictive: function (filter, level, exFilter, exType, eTag, excludeETag, skipCache) {
            var url = '';
            if (checkNotNullEmpty(filter)) {
                url = '?filter=' + filter;
                if (checkNotNullEmpty(level)) {
                    url += '&level=' + level;
                }
            }
            if (checkNotNullEmpty(exFilter)) {
                url += (url == '' ? '?' : '&') + 'extraFilter=' + exFilter;
                if (checkNotNullEmpty(exType)) {
                    url += '&exType=' + exType;
                }
            }
            if (checkNotNullEmpty(eTag)) {
                url += (url == '' ? '?' : '&') + 'eTag=' + eTag;
            }
            if(checkNotNullEmpty(excludeETag)) {
                url += (url == ''? '?' : '&') + 'excludeETag=' + excludeETag;
            }
            if(checkNotNullEmpty(skipCache)) {
                url += (url == ''? '?' : '&') + 'skipCache=' + skipCache;
            }
            return apiService.get('/s2/api/dashboard/predictive' + url);
        },
        delete: function (id) {
            return apiService.post(null, '/s2/api/dashboard/delete?id=' + id);
        },
        setAsDefault: function (curId, id) {
            return apiService.get('/s2/api/dashboard/setdefault?oid=' + curId + '&id=' + id);
        },
        update: function (data) {
            return apiService.post(data, '/s2/api/dashboard/update');
        },
        saveConfig: function (data) {
            return apiService.post(data, '/s2/api/dashboard/saveconfig');
        },
        getEntInvData: function(eid, mTag) {
            var url = '/s2/api/dashboard/ent/?eid=' + eid;
            if(checkNotNullEmpty(mTag)) {
                url += '&mTag=' + mTag;
            }
            return apiService.get(url);
        }
    }
}]);

