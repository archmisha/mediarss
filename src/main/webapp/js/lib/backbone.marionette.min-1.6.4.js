// MarionetteJS (Backbone.Marionette)
// ----------------------------------
// v1.6.4
//
// Copyright (c)2014 Derick Bailey, Muted Solutions, LLC.
// Distributed under MIT license
//
// http://marionettejs.com

/*!
 * Includes BabySitter
 * https://github.com/marionettejs/backbone.babysitter/
 *
 * Includes Wreqr
 * https://github.com/marionettejs/backbone.wreqr/
 */

Backbone.ChildViewContainer = function(a, b) {
	var c = function(a) {
		this._views = {}, this._indexByModel = {}, this._indexByCustom = {}, this._updateLength(), b.each(a, this.add, this)
	};
	b.extend(c.prototype, {add: function(a, b) {
		var c = a.cid;
		return this._views[c] = a, a.model && (this._indexByModel[a.model.cid] = c), b && (this._indexByCustom[b] = c), this._updateLength(), this
	}, findByModel: function(a) {
		return this.findByModelCid(a.cid)
	}, findByModelCid: function(a) {
		var b = this._indexByModel[a];
		return this.findByCid(b)
	}, findByCustom: function(a) {
		var b = this._indexByCustom[a];
		return this.findByCid(b)
	}, findByIndex: function(a) {
		return b.values(this._views)[a]
	}, findByCid: function(a) {
		return this._views[a]
	}, remove: function(a) {
		var c = a.cid;
		return a.model && delete this._indexByModel[a.model.cid], b.any(this._indexByCustom, function(a, b) {
			return a === c ? (delete this._indexByCustom[b], !0) : void 0
		}, this), delete this._views[c], this._updateLength(), this
	}, call: function(a) {
		this.apply(a, b.tail(arguments))
	}, apply: function(a, c) {
		b.each(this._views, function(d) {
			b.isFunction(d[a]) && d[a].apply(d, c || [])
		})
	}, _updateLength: function() {
		this.length = b.size(this._views)
	}});
	var d = ["forEach", "each", "map", "find", "detect", "filter", "select", "reject", "every", "all", "some", "any", "include", "contains", "invoke", "toArray", "first", "initial", "rest", "last", "without", "isEmpty", "pluck"];
	return b.each(d, function(a) {
		c.prototype[a] = function() {
			var c = b.values(this._views), d = [c].concat(b.toArray(arguments));
			return b[a].apply(b, d)
		}
	}), c
}(Backbone, _), Backbone.Wreqr = function(a, b, c) {
	"use strict";
	var d = {};
	return d.Handlers = function(a, b) {
		var c = function(a) {
			this.options = a, this._wreqrHandlers = {}, b.isFunction(this.initialize) && this.initialize(a)
		};
		return c.extend = a.Model.extend, b.extend(c.prototype, a.Events, {setHandlers: function(a) {
			b.each(a, function(a, c) {
				var d = null;
				b.isObject(a) && !b.isFunction(a) && (d = a.context, a = a.callback), this.setHandler(c, a, d)
			}, this)
		}, setHandler: function(a, b, c) {
			var d = {callback: b, context: c};
			this._wreqrHandlers[a] = d, this.trigger("handler:add", a, b, c)
		}, hasHandler: function(a) {
			return!!this._wreqrHandlers[a]
		}, getHandler: function(a) {
			var b = this._wreqrHandlers[a];
			if (!b)throw new Error("Handler not found for '" + a + "'");
			return function() {
				var a = Array.prototype.slice.apply(arguments);
				return b.callback.apply(b.context, a)
			}
		}, removeHandler: function(a) {
			delete this._wreqrHandlers[a]
		}, removeAllHandlers: function() {
			this._wreqrHandlers = {}
		}}), c
	}(a, c), d.CommandStorage = function() {
		var b = function(a) {
			this.options = a, this._commands = {}, c.isFunction(this.initialize) && this.initialize(a)
		};
		return c.extend(b.prototype, a.Events, {getCommands: function(a) {
			var b = this._commands[a];
			return b || (b = {command: a, instances: []}, this._commands[a] = b), b
		}, addCommand: function(a, b) {
			var c = this.getCommands(a);
			c.instances.push(b)
		}, clearCommands: function(a) {
			var b = this.getCommands(a);
			b.instances = []
		}}), b
	}(), d.Commands = function(a) {
		return a.Handlers.extend({storageType: a.CommandStorage, constructor: function(b) {
			this.options = b || {}, this._initializeStorage(this.options), this.on("handler:add", this._executeCommands, this);
			var c = Array.prototype.slice.call(arguments);
			a.Handlers.prototype.constructor.apply(this, c)
		}, execute: function(a, b) {
			a = arguments[0], b = Array.prototype.slice.call(arguments, 1), this.hasHandler(a) ? this.getHandler(a).apply(this, b) : this.storage.addCommand(a, b)
		}, _executeCommands: function(a, b, d) {
			var e = this.storage.getCommands(a);
			c.each(e.instances, function(a) {
				b.apply(d, a)
			}), this.storage.clearCommands(a)
		}, _initializeStorage: function(a) {
			var b, d = a.storageType || this.storageType;
			b = c.isFunction(d) ? new d : d, this.storage = b
		}})
	}(d), d.RequestResponse = function(a) {
		return a.Handlers.extend({request: function() {
			var a = arguments[0], b = Array.prototype.slice.call(arguments, 1);
			return this.getHandler(a).apply(this, b)
		}})
	}(d), d.EventAggregator = function(a, b) {
		var c = function() {
		};
		return c.extend = a.Model.extend, b.extend(c.prototype, a.Events), c
	}(a, c), d
}(Backbone, Backbone.Marionette, _);
var Marionette = function(a, b, c) {
	"use strict";
	function d(a, b) {
		var c = new Error(a);
		throw c.name = b || "Error", c
	}

	var e = {};
	b.Marionette = e, e.$ = b.$;
	var f = Array.prototype.slice;
	return e.extend = b.Model.extend, e.getOption = function(a, b) {
		if (a && b) {
			var c;
			return c = a.options && b in a.options && void 0 !== a.options[b] ? a.options[b] : a[b]
		}
	}, e.normalizeMethods = function(a) {
		var b, d = {};
		return c.each(a, function(a, e) {
			b = a, c.isFunction(b) || (b = this[b]), b && (d[e] = b)
		}, this), d
	}, e.triggerMethod = function() {
		function a(a, b, c) {
			return c.toUpperCase()
		}

		var b = /(^|:)(\w)/gi, d = function(d) {
			var e = "on" + d.replace(b, a), f = this[e];
			return c.isFunction(this.trigger) && this.trigger.apply(this, arguments), c.isFunction(f) ? f.apply(this, c.tail(arguments)) : void 0
		};
		return d
	}(), e.MonitorDOMRefresh = function(a) {
		function b(a) {
			a._isShown = !0, e(a)
		}

		function d(a) {
			a._isRendered = !0, e(a)
		}

		function e(a) {
			a._isShown && a._isRendered && f(a) && c.isFunction(a.triggerMethod) && a.triggerMethod("dom:refresh")
		}

		function f(b) {
			return a.contains(b.el)
		}

		return function(a) {
			a.listenTo(a, "show", function() {
				b(a)
			}), a.listenTo(a, "render", function() {
				d(a)
			})
		}
	}(document.documentElement), function(a) {
		function b(a, b, e, f) {
			var g = f.split(/\s+/);
			c.each(g, function(c) {
				var f = a[c];
				f || d("Method '" + c + "' was configured as an event handler, but does not exist."), a.listenTo(b, e, f)
			})
		}

		function e(a, b, c, d) {
			a.listenTo(b, c, d)
		}

		function f(a, b, d, e) {
			var f = e.split(/\s+/);
			c.each(f, function(c) {
				var e = a[c];
				a.stopListening(b, d, e)
			})
		}

		function g(a, b, c, d) {
			a.stopListening(b, c, d)
		}

		function h(a, b, d, e, f) {
			b && d && (c.isFunction(d) && (d = d.call(a)), c.each(d, function(d, g) {
				c.isFunction(d) ? e(a, b, g, d) : f(a, b, g, d)
			}))
		}

		a.bindEntityEvents = function(a, c, d) {
			h(a, c, d, e, b)
		}, a.unbindEntityEvents = function(a, b, c) {
			h(a, b, c, g, f)
		}
	}(e), e.Callbacks = function() {
		this._deferred = e.$.Deferred(), this._callbacks = []
	}, c.extend(e.Callbacks.prototype, {add: function(a, b) {
		this._callbacks.push({cb: a, ctx: b}), this._deferred.done(function(c, d) {
			b && (c = b), a.call(c, d)
		})
	}, run: function(a, b) {
		this._deferred.resolve(b, a)
	}, reset: function() {
		var a = this._callbacks;
		this._deferred = e.$.Deferred(), this._callbacks = [], c.each(a, function(a) {
			this.add(a.cb, a.ctx)
		}, this)
	}}), e.Controller = function(a) {
		this.triggerMethod = e.triggerMethod, this.options = a || {}, c.isFunction(this.initialize) && this.initialize(this.options)
	}, e.Controller.extend = e.extend, c.extend(e.Controller.prototype, b.Events, {close: function() {
		this.stopListening(), this.triggerMethod("close"), this.unbind()
	}}), e.Region = function(a) {
		if (this.options = a || {}, this.el = e.getOption(this, "el"), this.el || d("An 'el' must be specified for a region.", "NoElError"), this.initialize) {
			var b = Array.prototype.slice.apply(arguments);
			this.initialize.apply(this, b)
		}
	}, c.extend(e.Region, {buildRegion: function(a, b) {
		var e = c.isString(a), f = c.isString(a.selector), g = c.isUndefined(a.regionType), h = c.isFunction(a);
		h || e || f || d("Region must be specified as a Region type, a selector string or an object with selector property");
		var i, j;
		e && (i = a), a.selector && (i = a.selector, delete a.selector), h && (j = a), !h && g && (j = b), a.regionType && (j = a.regionType, delete a.regionType), (e || h) && (a = {}), a.el = i;
		var k = new j(a);
		return a.parentEl && (k.getEl = function(b) {
			var d = a.parentEl;
			return c.isFunction(d) && (d = d()), d.find(b)
		}), k
	}}), c.extend(e.Region.prototype, b.Events, {show: function(a) {
		this.ensureEl();
		var b = a.isClosed || c.isUndefined(a.$el), d = a !== this.currentView;
		d && this.close(), a.render(), (d || b) && this.open(a), this.currentView = a, e.triggerMethod.call(this, "show", a), e.triggerMethod.call(a, "show")
	}, ensureEl: function() {
		this.$el && 0 !== this.$el.length || (this.$el = this.getEl(this.el))
	}, getEl: function(a) {
		return e.$(a)
	}, open: function(a) {
		this.$el.empty().append(a.el)
	}, close: function() {
		var a = this.currentView;
		a && !a.isClosed && (a.close ? a.close() : a.remove && a.remove(), e.triggerMethod.call(this, "close", a), delete this.currentView)
	}, attachView: function(a) {
		this.currentView = a
	}, reset: function() {
		this.close(), delete this.$el
	}}), e.Region.extend = e.extend, e.RegionManager = function(a) {
		var b = a.Controller.extend({constructor: function(b) {
			this._regions = {}, a.Controller.prototype.constructor.call(this, b)
		}, addRegions: function(a, b) {
			var d = {};
			return c.each(a, function(a, e) {
				c.isString(a) && (a = {selector: a}), a.selector && (a = c.defaults({}, a, b));
				var f = this.addRegion(e, a);
				d[e] = f
			}, this), d
		}, addRegion: function(b, d) {
			var e, f = c.isObject(d), g = c.isString(d), h = !!d.selector;
			return e = g || f && h ? a.Region.buildRegion(d, a.Region) : c.isFunction(d) ? a.Region.buildRegion(d, a.Region) : d, this._store(b, e), this.triggerMethod("region:add", b, e), e
		}, get: function(a) {
			return this._regions[a]
		}, removeRegion: function(a) {
			var b = this._regions[a];
			this._remove(a, b)
		}, removeRegions: function() {
			c.each(this._regions, function(a, b) {
				this._remove(b, a)
			}, this)
		}, closeRegions: function() {
			c.each(this._regions, function(a) {
				a.close()
			}, this)
		}, close: function() {
			this.removeRegions(), a.Controller.prototype.close.apply(this, arguments)
		}, _store: function(a, b) {
			this._regions[a] = b, this._setLength()
		}, _remove: function(a, b) {
			b.close(), delete this._regions[a], this._setLength(), this.triggerMethod("region:remove", a, b)
		}, _setLength: function() {
			this.length = c.size(this._regions)
		}}), d = ["forEach", "each", "map", "find", "detect", "filter", "select", "reject", "every", "all", "some", "any", "include", "contains", "invoke", "toArray", "first", "initial", "rest", "last", "without", "isEmpty", "pluck"];
		return c.each(d, function(a) {
			b.prototype[a] = function() {
				var b = c.values(this._regions), d = [b].concat(c.toArray(arguments));
				return c[a].apply(c, d)
			}
		}), b
	}(e), e.TemplateCache = function(a) {
		this.templateId = a
	}, c.extend(e.TemplateCache, {templateCaches: {}, get: function(a) {
		var b = this.templateCaches[a];
		return b || (b = new e.TemplateCache(a), this.templateCaches[a] = b), b.load()
	}, clear: function() {
		var a, b = f.call(arguments), c = b.length;
		if (c > 0)for (a = 0; c > a; a++)delete this.templateCaches[b[a]]; else this.templateCaches = {}
	}}), c.extend(e.TemplateCache.prototype, {load: function() {
		if (this.compiledTemplate)return this.compiledTemplate;
		var a = this.loadTemplate(this.templateId);
		return this.compiledTemplate = this.compileTemplate(a), this.compiledTemplate
	}, loadTemplate: function(a) {
		var b = e.$(a).html();
		return b && 0 !== b.length || d("Could not find template: '" + a + "'", "NoTemplateError"), b
	}, compileTemplate: function(a) {
		return c.template(a)
	}}), e.Renderer = {render: function(a, b) {
		a || d("Cannot render the template since it's false, null or undefined.", "TemplateNotFoundError");
		var c;
		return(c = "function" == typeof a ? a : e.TemplateCache.get(a))(b)
	}}, e.View = b.View.extend({constructor: function(a) {
		c.bindAll(this, "render"), this.options = c.extend({}, c.result(this, "options"), c.isFunction(a) ? a.call(this) : a), this.events = this.normalizeUIKeys(c.result(this, "events")), b.View.prototype.constructor.apply(this, arguments), e.MonitorDOMRefresh(this), this.listenTo(this, "show", this.onShowCalled)
	}, triggerMethod: e.triggerMethod, normalizeMethods: e.normalizeMethods, getTemplate: function() {
		return e.getOption(this, "template")
	}, mixinTemplateHelpers: function(a) {
		a = a || {};
		var b = e.getOption(this, "templateHelpers");
		return c.isFunction(b) && (b = b.call(this)), c.extend(a, b)
	}, normalizeUIKeys: function(a) {
		var b = this;
		if ("undefined" != typeof a)return c.each(c.keys(a), function(d) {
			var e = /@ui.[a-zA-Z_$0-9]*/g;
			d.match(e) && (a[d.replace(e, function(a) {
				return c.result(b, "ui")[a.slice(4)]
			})] = a[d], delete a[d])
		}), a
	}, configureTriggers: function() {
		if (this.triggers) {
			var a = {}, b = this.normalizeUIKeys(c.result(this, "triggers"));
			return c.each(b, function(b, d) {
				var e = c.isObject(b), f = e ? b.event : b;
				a[d] = function(a) {
					if (a) {
						var c = a.preventDefault, d = a.stopPropagation, g = e ? b.preventDefault : c, h = e ? b.stopPropagation : d;
						g && c && c.apply(a), h && d && d.apply(a)
					}
					var i = {view: this, model: this.model, collection: this.collection};
					this.triggerMethod(f, i)
				}
			}, this), a
		}
	}, delegateEvents: function(a) {
		this._delegateDOMEvents(a), e.bindEntityEvents(this, this.model, e.getOption(this, "modelEvents")), e.bindEntityEvents(this, this.collection, e.getOption(this, "collectionEvents"))
	}, _delegateDOMEvents: function(a) {
		a = a || this.events, c.isFunction(a) && (a = a.call(this));
		var d = {}, e = this.configureTriggers();
		c.extend(d, a, e), b.View.prototype.delegateEvents.call(this, d)
	}, undelegateEvents: function() {
		var a = Array.prototype.slice.call(arguments);
		b.View.prototype.undelegateEvents.apply(this, a), e.unbindEntityEvents(this, this.model, e.getOption(this, "modelEvents")), e.unbindEntityEvents(this, this.collection, e.getOption(this, "collectionEvents"))
	}, onShowCalled: function() {
	}, close: function() {
		if (!this.isClosed) {
			var a = this.triggerMethod("before:close");
			a !== !1 && (this.isClosed = !0, this.triggerMethod("close"), this.unbindUIElements(), this.remove())
		}
	}, bindUIElements: function() {
		if (this.ui) {
			this._uiBindings || (this._uiBindings = this.ui);
			var a = c.result(this, "_uiBindings");
			this.ui = {}, c.each(c.keys(a), function(b) {
				var c = a[b];
				this.ui[b] = this.$(c)
			}, this)
		}
	}, unbindUIElements: function() {
		this.ui && this._uiBindings && (c.each(this.ui, function(a, b) {
			delete this.ui[b]
		}, this), this.ui = this._uiBindings, delete this._uiBindings)
	}}), e.ItemView = e.View.extend({constructor: function() {
		e.View.prototype.constructor.apply(this, arguments)
	}, serializeData: function() {
		var a = {};
		return this.model ? a = this.model.toJSON() : this.collection && (a = {items: this.collection.toJSON()}), a
	}, render: function() {
		this.isClosed = !1, this.triggerMethod("before:render", this), this.triggerMethod("item:before:render", this);
		var a = this.serializeData();
		a = this.mixinTemplateHelpers(a);
		var b = this.getTemplate(), c = e.Renderer.render(b, a);
		return this.$el.html(c), this.bindUIElements(), this.triggerMethod("render", this), this.triggerMethod("item:rendered", this), this
	}, close: function() {
		this.isClosed || (this.triggerMethod("item:before:close"), e.View.prototype.close.apply(this, arguments), this.triggerMethod("item:closed"))
	}}), e.CollectionView = e.View.extend({itemViewEventPrefix: "itemview", constructor: function() {
		this._initChildViewStorage(), e.View.prototype.constructor.apply(this, arguments), this._initialEvents(), this.initRenderBuffer()
	}, initRenderBuffer: function() {
		this.elBuffer = document.createDocumentFragment(), this._bufferedChildren = []
	}, startBuffering: function() {
		this.initRenderBuffer(), this.isBuffering = !0
	}, endBuffering: function() {
		this.isBuffering = !1, this.appendBuffer(this, this.elBuffer), this._triggerShowBufferedChildren(), this.initRenderBuffer()
	}, _triggerShowBufferedChildren: function() {
		this._isShown && (c.each(this._bufferedChildren, function(a) {
			e.triggerMethod.call(a, "show")
		}), this._bufferedChildren = [])
	}, _initialEvents: function() {
		this.collection && (this.listenTo(this.collection, "add", this.addChildView), this.listenTo(this.collection, "remove", this.removeItemView), this.listenTo(this.collection, "reset", this.render))
	}, addChildView: function(a) {
		this.closeEmptyView();
		var b = this.getItemView(a), c = this.collection.indexOf(a);
		this.addItemView(a, b, c)
	}, onShowCalled: function() {
		this.children.each(function(a) {
			e.triggerMethod.call(a, "show")
		})
	}, triggerBeforeRender: function() {
		this.triggerMethod("before:render", this), this.triggerMethod("collection:before:render", this)
	}, triggerRendered: function() {
		this.triggerMethod("render", this), this.triggerMethod("collection:rendered", this)
	}, render: function() {
		return this.isClosed = !1, this.triggerBeforeRender(), this._renderChildren(), this.triggerRendered(), this
	}, _renderChildren: function() {
		this.startBuffering(), this.closeEmptyView(), this.closeChildren(), this.isEmpty(this.collection) ? this.showEmptyView() : this.showCollection(), this.endBuffering()
	}, showCollection: function() {
		var a;
		this.collection.each(function(b, c) {
			a = this.getItemView(b), this.addItemView(b, a, c)
		}, this)
	}, showEmptyView: function() {
		var a = this.getEmptyView();
		if (a && !this._showingEmptyView) {
			this._showingEmptyView = !0;
			var c = new b.Model;
			this.addItemView(c, a, 0)
		}
	}, closeEmptyView: function() {
		this._showingEmptyView && (this.closeChildren(), delete this._showingEmptyView)
	}, getEmptyView: function() {
		return e.getOption(this, "emptyView")
	}, getItemView: function() {
		var a = e.getOption(this, "itemView");
		return a || d("An `itemView` must be specified", "NoItemViewError"), a
	}, addItemView: function(a, b, d) {
		var f = e.getOption(this, "itemViewOptions");
		c.isFunction(f) && (f = f.call(this, a, d));
		var g = this.buildItemView(a, b, f);
		return this.addChildViewEventForwarding(g), this.triggerMethod("before:item:added", g), this.children.add(g), this.renderItemView(g, d), this._isShown && !this.isBuffering && e.triggerMethod.call(g, "show"), this.triggerMethod("after:item:added", g), g
	}, addChildViewEventForwarding: function(a) {
		var b = e.getOption(this, "itemViewEventPrefix");
		this.listenTo(a, "all", function() {
			var d = f.call(arguments), g = d[0], h = this.normalizeMethods(this.getItemEvents());
			d[0] = b + ":" + g, d.splice(1, 0, a), "undefined" != typeof h && c.isFunction(h[g]) && h[g].apply(this, d), e.triggerMethod.apply(this, d)
		}, this)
	}, getItemEvents: function() {
		return c.isFunction(this.itemEvents) ? this.itemEvents.call(this) : this.itemEvents
	}, renderItemView: function(a, b) {
		a.render(), this.appendHtml(this, a, b)
	}, buildItemView: function(a, b, d) {
		var e = c.extend({model: a}, d);
		return new b(e)
	}, removeItemView: function(a) {
		var b = this.children.findByModel(a);
		this.removeChildView(b), this.checkEmpty()
	}, removeChildView: function(a) {
		a && (this.stopListening(a), a.close ? a.close() : a.remove && a.remove(), this.children.remove(a)), this.triggerMethod("item:removed", a)
	}, isEmpty: function() {
		return!this.collection || 0 === this.collection.length
	}, checkEmpty: function() {
		this.isEmpty(this.collection) && this.showEmptyView()
	}, appendBuffer: function(a, b) {
		a.$el.append(b)
	}, appendHtml: function(a, b) {
		a.isBuffering ? (a.elBuffer.appendChild(b.el), a._bufferedChildren.push(b)) : a.$el.append(b.el)
	}, _initChildViewStorage: function() {
		this.children = new b.ChildViewContainer
	}, close: function() {
		this.isClosed || (this.triggerMethod("collection:before:close"), this.closeChildren(), this.triggerMethod("collection:closed"), e.View.prototype.close.apply(this, arguments))
	}, closeChildren: function() {
		this.children.each(function(a) {
			this.removeChildView(a)
		}, this), this.checkEmpty()
	}}), e.CompositeView = e.CollectionView.extend({constructor: function() {
		e.CollectionView.prototype.constructor.apply(this, arguments)
	}, _initialEvents: function() {
		this.once("render", function() {
			this.collection && (this.listenTo(this.collection, "add", this.addChildView), this.listenTo(this.collection, "remove", this.removeItemView), this.listenTo(this.collection, "reset", this._renderChildren))
		})
	}, getItemView: function() {
		var a = e.getOption(this, "itemView") || this.constructor;
		return a || d("An `itemView` must be specified", "NoItemViewError"), a
	}, serializeData: function() {
		var a = {};
		return this.model && (a = this.model.toJSON()), a
	}, render: function() {
		this.isRendered = !0, this.isClosed = !1, this.resetItemViewContainer(), this.triggerBeforeRender();
		var a = this.renderModel();
		return this.$el.html(a), this.bindUIElements(), this.triggerMethod("composite:model:rendered"), this._renderChildren(), this.triggerMethod("composite:rendered"), this.triggerRendered(), this
	}, _renderChildren: function() {
		this.isRendered && (this.triggerMethod("composite:collection:before:render"), e.CollectionView.prototype._renderChildren.call(this), this.triggerMethod("composite:collection:rendered"))
	}, renderModel: function() {
		var a = {};
		a = this.serializeData(), a = this.mixinTemplateHelpers(a);
		var b = this.getTemplate();
		return e.Renderer.render(b, a)
	}, appendBuffer: function(a, b) {
		var c = this.getItemViewContainer(a);
		c.append(b)
	}, appendHtml: function(a, b) {
		if (a.isBuffering)a.elBuffer.appendChild(b.el), a._bufferedChildren.push(b); else {
			var c = this.getItemViewContainer(a);
			c.append(b.el)
		}
	}, getItemViewContainer: function(a) {
		if ("$itemViewContainer"in a)return a.$itemViewContainer;
		var b, f = e.getOption(a, "itemViewContainer");
		if (f) {
			var g = c.isFunction(f) ? f.call(this) : f;
			b = a.$(g), b.length <= 0 && d("The specified `itemViewContainer` was not found: " + a.itemViewContainer, "ItemViewContainerMissingError")
		} else b = a.$el;
		return a.$itemViewContainer = b, b
	}, resetItemViewContainer: function() {
		this.$itemViewContainer && delete this.$itemViewContainer
	}}), e.Layout = e.ItemView.extend({regionType: e.Region, constructor: function(a) {
		a = a || {}, this._firstRender = !0, this._initializeRegions(a), e.ItemView.prototype.constructor.call(this, a)
	}, render: function() {
		return this.isClosed && this._initializeRegions(), this._firstRender ? this._firstRender = !1 : this.isClosed || this._reInitializeRegions(), e.ItemView.prototype.render.apply(this, arguments)
	}, close: function() {
		this.isClosed || (this.regionManager.close(), e.ItemView.prototype.close.apply(this, arguments))
	}, addRegion: function(a, b) {
		var c = {};
		return c[a] = b, this._buildRegions(c)[a]
	}, addRegions: function(a) {
		return this.regions = c.extend({}, this.regions, a), this._buildRegions(a)
	}, removeRegion: function(a) {
		return delete this.regions[a], this.regionManager.removeRegion(a)
	}, _buildRegions: function(a) {
		var b = this, c = {regionType: e.getOption(this, "regionType"), parentEl: function() {
			return b.$el
		}};
		return this.regionManager.addRegions(a, c)
	}, _initializeRegions: function(a) {
		var b;
		this._initRegionManager(), b = c.isFunction(this.regions) ? this.regions(a) : this.regions || {}, this.addRegions(b)
	}, _reInitializeRegions: function() {
		this.regionManager.closeRegions(), this.regionManager.each(function(a) {
			a.reset()
		})
	}, _initRegionManager: function() {
		this.regionManager = new e.RegionManager, this.listenTo(this.regionManager, "region:add", function(a, b) {
			this[a] = b, this.trigger("region:add", a, b)
		}), this.listenTo(this.regionManager, "region:remove", function(a, b) {
			delete this[a], this.trigger("region:remove", a, b)
		})
	}}), e.AppRouter = b.Router.extend({constructor: function(a) {
		b.Router.prototype.constructor.apply(this, arguments), this.options = a || {};
		var c = e.getOption(this, "appRoutes"), d = this._getController();
		this.processAppRoutes(d, c)
	}, appRoute: function(a, b) {
		var c = this._getController();
		this._addAppRoute(c, a, b)
	}, processAppRoutes: function(a, b) {
		if (b) {
			var d = c.keys(b).reverse();
			c.each(d, function(c) {
				this._addAppRoute(a, c, b[c])
			}, this)
		}
	}, _getController: function() {
		return e.getOption(this, "controller")
	}, _addAppRoute: function(a, b, e) {
		var f = a[e];
		f || d("Method '" + e + "' was not found on the controller"), this.route(b, e, c.bind(f, a))
	}}), e.Application = function(a) {
		this._initRegionManager(), this._initCallbacks = new e.Callbacks, this.vent = new b.Wreqr.EventAggregator, this.commands = new b.Wreqr.Commands, this.reqres = new b.Wreqr.RequestResponse, this.submodules = {}, c.extend(this, a), this.triggerMethod = e.triggerMethod
	}, c.extend(e.Application.prototype, b.Events, {execute: function() {
		this.commands.execute.apply(this.commands, arguments)
	}, request: function() {
		return this.reqres.request.apply(this.reqres, arguments)
	}, addInitializer: function(a) {
		this._initCallbacks.add(a)
	}, start: function(a) {
		this.triggerMethod("initialize:before", a), this._initCallbacks.run(a, this), this.triggerMethod("initialize:after", a), this.triggerMethod("start", a)
	}, addRegions: function(a) {
		return this._regionManager.addRegions(a)
	}, closeRegions: function() {
		this._regionManager.closeRegions()
	}, removeRegion: function(a) {
		this._regionManager.removeRegion(a)
	}, getRegion: function(a) {
		return this._regionManager.get(a)
	}, module: function(a, b) {
		var c = e.Module.getClass(b), d = f.call(arguments);
		return d.unshift(this), c.create.apply(c, d)
	}, _initRegionManager: function() {
		this._regionManager = new e.RegionManager, this.listenTo(this._regionManager, "region:add", function(a, b) {
			this[a] = b
		}), this.listenTo(this._regionManager, "region:remove", function(a) {
			delete this[a]
		})
	}}), e.Application.extend = e.extend, e.Module = function(a, b, d) {
		this.moduleName = a, this.options = c.extend({}, this.options, d), this.initialize = d.initialize || this.initialize, this.submodules = {}, this._setupInitializersAndFinalizers(), this.app = b, this.startWithParent = !0, this.triggerMethod = e.triggerMethod, c.isFunction(this.initialize) && this.initialize(this.options, a, b)
	}, e.Module.extend = e.extend, c.extend(e.Module.prototype, b.Events, {initialize: function() {
	}, addInitializer: function(a) {
		this._initializerCallbacks.add(a)
	}, addFinalizer: function(a) {
		this._finalizerCallbacks.add(a)
	}, start: function(a) {
		this._isInitialized || (c.each(this.submodules, function(b) {
			b.startWithParent && b.start(a)
		}), this.triggerMethod("before:start", a), this._initializerCallbacks.run(a, this), this._isInitialized = !0, this.triggerMethod("start", a))
	}, stop: function() {
		this._isInitialized && (this._isInitialized = !1, e.triggerMethod.call(this, "before:stop"), c.each(this.submodules, function(a) {
			a.stop()
		}), this._finalizerCallbacks.run(void 0, this), this._initializerCallbacks.reset(), this._finalizerCallbacks.reset(), e.triggerMethod.call(this, "stop"))
	}, addDefinition: function(a, b) {
		this._runModuleDefinition(a, b)
	}, _runModuleDefinition: function(a, d) {
		if (a) {
			var f = c.flatten([this, this.app, b, e, e.$, c, d]);
			a.apply(this, f)
		}
	}, _setupInitializersAndFinalizers: function() {
		this._initializerCallbacks = new e.Callbacks, this._finalizerCallbacks = new e.Callbacks
	}}), c.extend(e.Module, {create: function(a, b, d) {
		var e = a, g = f.call(arguments);
		g.splice(0, 3), b = b.split(".");
		var h = b.length, i = [];
		return i[h - 1] = d, c.each(b, function(b, c) {
			var f = e;
			e = this._getModule(f, b, a, d), this._addModuleDefinition(f, e, i[c], g)
		}, this), e
	}, _getModule: function(a, b, d, e) {
		var f = c.extend({}, e), g = this.getClass(e), h = a[b];
		return h || (h = new g(b, d, f), a[b] = h, a.submodules[b] = h), h
	}, getClass: function(a) {
		var b = e.Module;
		return a ? a.prototype instanceof b ? a : a.moduleClass || b : b
	}, _addModuleDefinition: function(a, b, d, f) {
		var g, h;
		!c.isFunction(d) || d.prototype instanceof e.Module ? c.isObject(d) ? (g = d.define, h = c.isUndefined(d.startWithParent) ? !0 : d.startWithParent) : h = !0 : (g = d, h = !0), g && b.addDefinition(g, f), b.startWithParent = b.startWithParent && h, b.startWithParent && !b.startWithParentIsConfigured && (b.startWithParentIsConfigured = !0, a.addInitializer(function(a) {
			b.startWithParent && b.start(a)
		}))
	}}), e
}(this, Backbone, _);
//# sourceMappingURL=backbone.marionette.map