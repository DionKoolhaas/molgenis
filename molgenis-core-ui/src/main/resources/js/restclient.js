// molgenis entity REST API client
(function($, molgenis) {
	"use strict";
	var self = molgenis.RestClient = molgenis.RestClient || {};
	
	molgenis.RestClient = function RestClient() {};

	molgenis.RestClient.prototype.get = function(resourceUri, options) {
		return this._get(resourceUri, options);
	};
	
	molgenis.RestClient.prototype.getAsync = function(resourceUri, options, callback) {
		this._get(resourceUri, options, callback);
	};

	molgenis.RestClient.prototype._get = function(resourceUri, options, callback) {
		var resource = null;

		var async = callback !== undefined;
		
		var config = {
			'dataType' : 'json',
			'url' : this._toApiUri(resourceUri, options),
			'cache' : true,
			'async' : async,
			'success' : function(data) {
				if (async)
					callback(data);
				else
					resource = data;
			}
		};
		
		// tunnel get requests with query through a post,
		// because it might not fit in the URL
		if (options && options.q) {
			$.extend(config, {
				'type' : 'POST',
				'data' : JSON.stringify(options.q),
				'contentType' : 'application/json'
			});
		}
		
		this._ajax(config);
		
		if (!async)
			return resource;
	};
	
	molgenis.RestClient.prototype._ajax = function(config) {
		if (self.token) {
			$.extend(config, {
				headers: {'x-molgenis-token': self.token}
			});
		}
		
		$.ajax(config);
	}
	
	molgenis.RestClient.prototype._toApiUri = function(resourceUri, options) {
		var qs = "";
		if (resourceUri.indexOf('?') != -1) {
			var uriParts = resourceUri.split('?');
			resourceUri = uriParts[0];
			qs = '?' + uriParts[1];
		}
		if (options && options.attributes && options.attributes.length > 0)
			qs += (qs.length === 0 ? '?' : '&') + 'attributes=' + encodeURIComponent(options.attributes.join(','));
		if (options && options.expand && options.expand.length > 0)
			qs += (qs.length === 0 ? '?' : '&') + 'expand=' + encodeURIComponent(options.expand.join(','));
		if (options && options.q)
			qs += (qs.length === 0 ? '?' : '&') + '_method=GET';
		return resourceUri + qs;
	};

	molgenis.RestClient.prototype.getPrimaryKeyFromHref = function(href) {
		return href.substring(href.lastIndexOf('/') + 1);
	};

	molgenis.RestClient.prototype.getHref = function(entityName, primaryKey) {
		return '/api/v1/' + entityName + (primaryKey ? '/' + primaryKey : '');
	};
	
	molgenis.RestClient.prototype.remove = function(href, callback) {
		this._ajax({
			type : 'POST',
			url : href,
			data : '_method=DELETE',
			async : false,
			success : callback.success,
			error : callback.error
		});
	};
	
	molgenis.RestClient.prototype.update = function(href, entity, callback) {
		this._ajax({
			type : 'POST',
			url : href + '?_method=PUT',
			contentType : 'application/json',
			data : JSON.stringify(entity),
			async : false,
			success : callback.success,
			error : callback.error
		});
	};
	
	molgenis.RestClient.prototype.entityExists = function(resourceUri) {
		var result = false;
		this._ajax({
			dataType : 'json',
			url : resourceUri + '/exist',
			async : false,
			success : function(exists) {
				result = exists;
			}
		});
		
		return result;
	};
	
	molgenis.RestClient.prototype.login = function(username, password, callback) {
		$.ajax({
			type: 'POST',
			dataType : 'json',
			url : '/api/v1/login',
			contentType : 'application/json',
			async : true,
			data: JSON.stringify({username: username, password: password}),
			success : function(loginResult) {
				self.token = loginResult.token;
				callback.success({
					username: loginResult.username,
					firstname: loginResult.firstname,
					lastname: loginResult.lastname
				});
			},
			error : callback.error
		});
	};
	
	molgenis.RestClient.prototype.logout = function(callback) {
		this._ajax({
			url : '/api/v1/logout',
			async : true,
			success : function() {
				self.token = null;
				callback();
			}
		});
	};
	
}($, window.top.molgenis = window.top.molgenis || {}));
