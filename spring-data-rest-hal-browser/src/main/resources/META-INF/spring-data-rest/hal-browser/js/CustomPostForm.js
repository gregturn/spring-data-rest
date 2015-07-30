/**
 * Custom Backbone view that uses JSON Schema metadata to create pop-up dialog with actual field names instead of
 * asking user to input raw JSON.
 *
 * NOTE: Because JSON Schema lists all properties, including those that are links, they have to be filtered out.
 * Links have to be set via a PUT operation with the proper media type.
 *
 * @author Greg Turnquist
 * @since 2.4
 * @see DATAREST-627
 */
/* jshint strict: true */
/* globals HAL, Backbone, _, $, window, jqxhr */

'use strict';

var CustomPostForm = Backbone.View.extend({
	initialize: function (opts) {
		this.href = opts.href.split('{')[0];
		this.vent = opts.vent;
		_.bindAll(this, 'createNewResource');
	},

	events: {
		'submit form': 'createNewResource'
	},

	className: 'modal fade',

	createNewResource: function (e) {
		e.preventDefault();

		var self = this;

		var data = {};
		Object.keys(this.schema.properties).forEach(function (property) {
			if (!(self.schema.properties[property]).hasOwnProperty('format')) {
				data[property] = self.$('input[name=' + property + ']').val();
			}
		});

		var opts = {
			url: this.$('.url').val(),
			headers: HAL.parseHeaders(this.$('.headers').val()),
			method: this.$('.method').val(),
			data: JSON.stringify(data)
		};

		var request = HAL.client.request(opts);
		request.done(function (response) {
			self.vent.trigger('response', {resource: response, jqxhr: jqxhr});
		}).fail(function () {
			self.vent.trigger('fail-response', {jqxhr: jqxhr});
		}).always(function () {
			self.vent.trigger('response-headers', {jqxhr: jqxhr});
			window.location.hash = 'NON-GET:' + opts.url;
		});

		this.$el.modal('hide');
	},

	render: function (opts) {
		var self = this;

		var headersString = '';
		_.each(HAL.client.getHeaders(), function (value, name) {
			headersString += name + ': ' + value + '\n';
		});

		HAL.client.request({
			method: 'GET',
			url: this.href
		}).then(function (hal) {
			return HAL.client.request({
				method: 'GET',
				url: hal._links.profile.href,
				headers: {'Accept': 'application/schema+json'}
			});
		}).done(function (schema) {
			self.schema = schema;
			self.$el.html(self.template({href: self.href, schema: self.schema, userDefinedHeaders: headersString}));
			self.$el.modal();
		});

		return this;
	},

	template: _.template($('#dynamic-request-template').html())
});

/**
 * Inject the form into the HAL Browser.
 */
HAL.customPostForm = CustomPostForm;