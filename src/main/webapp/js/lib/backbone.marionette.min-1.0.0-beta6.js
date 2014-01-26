/*!
 * Backbone.Marionette, v1.0.0-beta6
 * Copyright (c)2012 Derick Bailey, Muted Solutions, LLC.
 * Distributed under MIT license
 * http://github.com/marionettejs/backbone.marionette
 */
/*!
 * Includes BabySitter
 * https://github.com/marionettejs/backbone.babysitter/
 * Includes Wreqr
 * https://github.com/marionettejs/backbone.wreqr/
 * Includes EventBinder
 * https://github.com/marionettejs/backbone.eventbinder/
 */
Backbone.ChildViewContainer = function(e, t) {
	var n = function(e) {
		this._views = {}, this._indexByModel = {}, this._indexByCollection = {}, this._indexByCustom = {}, this._updateLength()
	};
	t.extend(n.prototype, {add: function(e, t) {
		var n = e.cid;
		this._views[n] = e, e.model && (this._indexByModel[e.model.cid] = n), e.collection && (this._indexByCollection[e.collection.cid] = n), t && (this._indexByCustom[t] = n), this._updateLength()
	}, findByModel: function(e) {
		var t = this._indexByModel[e.cid];
		return this.findByCid(t)
	}, findByCollection: function(e) {
		var t = this._indexByCollection[e.cid];
		return this.findByCid(t)
	}, findByCustom: function(e) {
		var t = this._indexByCustom[e];
		return this.findByCid(t)
	}, findByIndex: function(e) {
		return t.values(this._views)[e]
	}, findByCid: function(e) {
		return this._views[e]
	}, remove: function(e) {
		var t = e.cid;
		e.model && delete this._indexByModel[e.model.cid], e.collection && delete this._indexByCollection[e.collection.cid];
		var n;
		for (var r in this._indexByCustom)if (this._indexByCustom.hasOwnProperty(r) && this._indexByCustom[r] === t) {
			n = r;
			break
		}
		n && delete this._indexByCustom[n], delete this._views[t], this._updateLength()
	}, call: function(e, t) {
		t = Array.prototype.slice.call(arguments, 1), this.apply(e, t)
	}, apply: function(e, n) {
		var r;
		t.each(this._views, function(r, i) {
			t.isFunction(r[e]) && r[e].apply(r, n)
		})
	}, _updateLength: function() {
		this.length = t.size(this._views)
	}});
	var r = ["forEach", "each", "map", "find", "detect", "filter", "select", "reject", "every", "all", "some", "any", "include", "contains", "invoke", "toArray", "first", "initial", "rest", "last", "without", "isEmpty"];
	return t.each(r, function(e) {
		n.prototype[e] = function() {
			var n = t.values(this._views), r = [n].concat(t.toArray(arguments));
			return t[e].apply(t, r)
		}
	}), n
}(Backbone, _), Backbone.EventBinder = function(e, t) {
	"use strict";
	function r(e) {
		return e.jquery ? n.jquery : n["default"]
	}

	var n = {"default": {bindTo: function(e, t, n, r) {
		r = r || this, e.on(t, n, r);
		var i = {type: "default", obj: e, eventName: t, callback: n, context: r};
		return i
	}, unbindFrom: function(e) {
		e.obj.off(e.eventName, e.callback, e.context)
	}}, jquery: {bindTo: function(e, n, r, i) {
		i = i || this, r = t(r).bind(i), e.on(n, r);
		var s = {type: "jquery", obj: e, eventName: n, callback: r, context: i};
		return s
	}, unbindFrom: function(e) {
		e.obj.off(e.eventName, e.callback)
	}}}, i = function() {
		this._eventBindings = []
	};
	return i.extend = e.View.extend, t.extend(i.prototype, {bindTo: function() {
		var e = arguments[0], t = r(e), n = t.bindTo.apply(this, arguments);
		return this._eventBindings.push(n), n
	}, unbindFrom: function(e) {
		n[e.type].unbindFrom.apply(this, arguments), this._eventBindings = t.reject(this._eventBindings, function(t) {
			return t === e
		})
	}, unbindAll: function() {
		var e = t.map(this._eventBindings, t.identity);
		t.each(e, this.unbindFrom, this)
	}}), i
}(Backbone, _), Backbone.Wreqr = function(e, t, n) {
	"option strict";
	var r = {};
	return r.Handlers = function(e, t) {
		var n = function() {
			"use strict";
			this._handlers = {}
		};
		return n.extend = e.Model.extend, t.extend(n.prototype, {addHandler: function(e, t, n) {
			var r = {callback: t, context: n};
			this._handlers[e] = r
		}, getHandler: function(e) {
			var t = this._handlers[e];
			if (!t)throw new Error("Handler not found for '" + e + "'");
			return function() {
				return t.callback.apply(t.context, arguments)
			}
		}, removeHandler: function(e) {
			delete this._handlers[e]
		}, removeAllHandlers: function() {
			this._handlers = {}
		}}), n
	}(e, n), r.Commands = function(e) {
		return e.Handlers.extend({execute: function(e, t) {
			this.getHandler(e)(t)
		}})
	}(r), r.RequestResponse = function(e) {
		return e.Handlers.extend({request: function(e, t) {
			return this.getHandler(e)(t)
		}})
	}(r), r.EventAggregator = function(e, t) {
		var n = function() {
		};
		return n.extend = e.Model.extend, t.extend(n.prototype, e.Events), n
	}(e, n), r
}(Backbone, Backbone.Marionette, _), Backbone.Marionette = Marionette = function(e, t, n) {
	var r = {}, i = Array.prototype.slice;
	return r.extend = e.Model.extend, r.getOption = function(e, t) {
		if (!e || !t)return;
		var n;
		return e.options && e.options[t] ? n = e.options[t] : n = e[t], n
	}, r.createObject = function() {
		function t() {
		}

		var e;
		return typeof Object.create == "function" ? e = Object.create : e = function(e) {
			t.prototype = e;
			var n = new t;
			return t.prototype = null, n
		}, e
	}(), r.triggerMethod = function() {
		var e = Array.prototype.slice.apply(arguments), n = e[0], r = n.split(":"), i, s, o = "on";
		for (var u = 0; u < r.length; u++)i = r[u], s = i.charAt(0).toUpperCase(), o += s + i.slice(1);
		this.trigger.apply(this, arguments);
		if (t.isFunction(this[o]))return e.shift(), this[o].apply(this, e)
	}, r.EventBinder = e.EventBinder.extend({augment: function(e) {
		var n = new r.EventBinder;
		e.eventBinder = n, e.bindTo = t.bind(n.bindTo, n), e.unbindFrom = t.bind(n.unbindFrom, n), e.unbindAll = t.bind(n.unbindAll, n)
	}}), r.addEventBinder = function(e) {
		var n = new r.EventBinder;
		e.eventBinder = n, e.bindTo = t.bind(n.bindTo, n), e.unbindFrom = t.bind(n.unbindFrom, n), e.unbindAll = t.bind(n.unbindAll, n)
	}, r.EventAggregator = e.Wreqr.EventAggregator.extend({constructor: function() {
		r.addEventBinder(this), e.Wreqr.EventAggregator.prototype.constructor.apply(this, arguments)
	}}), r.Callbacks = function() {
		this._deferred = n.Deferred(), this._callbacks = []
	}, t.extend(r.Callbacks.prototype, {add: function(e, t) {
		this._callbacks.push({cb: e, ctx: t}), this._deferred.done(function(n, r) {
			t && (n = t), e.call(n, r)
		})
	}, run: function(e, t) {
		this._deferred.resolve(t, e)
	}, reset: function() {
		var e = this, r = this._callbacks;
		this._deferred = n.Deferred(), this._callbacks = [], t.each(r, function(t) {
			e.add(t.cb, t.ctx)
		})
	}}), r.TemplateCache = function(e) {
		this.templateId = e
	}, t.extend(r.TemplateCache, {templateCaches: {}, get: function(e) {
		var t = this, n = this.templateCaches[e];
		return n || (n = new r.TemplateCache(e), this.templateCaches[e] = n), n.load()
	}, clear: function() {
		var e, t = arguments.length;
		if (t > 0)for (e = 0; e < t; e++)delete this.templateCaches[arguments[e]]; else this.templateCaches = {}
	}}), t.extend(r.TemplateCache.prototype, {load: function() {
		var e = this;
		if (this.compiledTemplate)return this.compiledTemplate;
		var t = this.loadTemplate(this.templateId);
		return this.compiledTemplate = this.compileTemplate(t), this.compiledTemplate
	}, loadTemplate: function(e) {
		var t = n(e).html();
		if (!t || t.length === 0) {
			var r = "Could not find template: '" + e + "'", i = new Error(r);
			throw i.name = "NoTemplateError", i
		}
		return t
	}, compileTemplate: function(e) {
		return t.template(e)
	}}), r.Renderer = {render: function(e, t) {
		var n = typeof e == "function" ? e : r.TemplateCache.get(e), i = n(t);
		return i
	}}, r.Controller = function(e) {
		this.triggerMethod = r.triggerMethod, this.options = e || {}, r.addEventBinder(this), t.isFunction(this.initialize) && this.initialize(this.options)
	}, r.Controller.extend = r.extend, t.extend(r.Controller.prototype, e.Events, {close: function() {
		this.unbindAll(), this.triggerMethod("close"), this.unbind()
	}}), r.Region = function(e) {
		this.options = e || {}, r.addEventBinder(this), this.el = r.getOption(this, "el");
		if (!this.el) {
			var t = new Error("An 'el' must be specified for a region.");
			throw t.name = "NoElError", t
		}
		this.initialize && this.initialize.apply(this, arguments)
	}, t.extend(r.Region, {buildRegion: function(e, t) {
		var n = typeof e == "string", r = typeof e.selector == "string", i = typeof e.regionType == "undefined", s = typeof e == "function";
		if (!s && !n && !r)throw new Error("Region must be specified as a Region type, a selector string or an object with selector property");
		var o, u;
		n && (o = e), e.selector && (o = e.selector), s && (u = e), !s && i && (u = t), e.regionType && (u = e.regionType);
		var a = new u({el: o});
		return a
	}}), t.extend(r.Region.prototype, e.Events, {show: function(e) {
		this.ensureEl(), this.close(), e.render(), this.open(e), r.triggerMethod.call(e, "show"), r.triggerMethod.call(this, "show", e), this.currentView = e
	}, ensureEl: function() {
		if (!this.$el || this.$el.length === 0)this.$el = this.getEl(this.el)
	}, getEl: function(e) {
		return n(e)
	}, open: function(e) {
		this.$el.empty().append(e.el)
	}, close: function() {
		var e = this.currentView;
		if (!e || e.isClosed)return;
		e.close && e.close(), r.triggerMethod.call(this, "close"), delete this.currentView
	}, attachView: function(e) {
		this.currentView = e
	}, reset: function() {
		this.close(), delete this.$el
	}}), r.Region.extend = r.extend, r.View = e.View.extend({constructor: function() {
		t.bindAll(this, "render"), r.addEventBinder(this), e.View.prototype.constructor.apply(this, arguments), this.bindBackboneEntityTo(this.model, this.modelEvents), this.bindBackboneEntityTo(this.collection, this.collectionEvents), this.bindTo(this, "show", this.onShowCalled, this)
	}, triggerMethod: r.triggerMethod, getTemplate: function() {
		return r.getOption(this, "template")
	}, mixinTemplateHelpers: function(e) {
		e = e || {};
		var n = this.templateHelpers;
		return t.isFunction(n) && (n = n.call(this)), t.extend(e, n)
	}, configureTriggers: function() {
		if (!this.triggers)return;
		var e = this, n = {}, r = t.result(this, "triggers");
		return t.each(r, function(t, r) {
			n[r] = function(n) {
				n && n.preventDefault && n.preventDefault(), n && n.stopPropagation && n.stopPropagation(), e.trigger(t)
			}
		}), n
	}, delegateEvents: function(n) {
		n = n || this.events, t.isFunction(n) && (n = n.call(this));
		var r = {}, i = this.configureTriggers();
		t.extend(r, n, i), e.View.prototype.delegateEvents.call(this, r)
	}, onShowCalled: function() {
	}, close: function() {
		if (this.isClosed)return;
		this.triggerMethod("before:close"), this.remove(), this.unbindAll(), this.triggerMethod("close"), this.isClosed = !0
	}, bindUIElements: function() {
		if (!this.ui)return;
		var e = this;
		this.uiBindings || (this.uiBindings = this.ui), this.ui = {}, t.each(t.keys(this.uiBindings), function(t) {
			var n = e.uiBindings[t];
			e.ui[t] = e.$(n)
		})
	}, bindBackboneEntityTo: function(e, n) {
		if (!e || !n)return;
		var r = this;
		t.each(n, function(t, n) {
			var i = r[t];
			if (!i)throw new Error("View method '" + t + "' was configured as an event handler, but does not exist.");
			r.bindTo(e, n, i, r)
		})
	}}), r.ItemView = r.View.extend({constructor: function() {
		r.View.prototype.constructor.apply(this, arguments), this.initialEvents && this.initialEvents()
	}, serializeData: function() {
		var e = {};
		return this.model ? e = this.model.toJSON() : this.collection && (e = {items: this.collection.toJSON()}), e
	}, render: function() {
		this.isClosed = !1, this.triggerMethod("before:render", this), this.triggerMethod("item:before:render", this);
		var e = this.serializeData();
		e = this.mixinTemplateHelpers(e);
		var t = this.getTemplate(), n = r.Renderer.render(t, e);
		return this.$el.html(n), this.bindUIElements(), this.triggerMethod("render", this), this.triggerMethod("item:rendered", this), this
	}, close: function() {
		if (this.isClosed)return;
		this.triggerMethod("item:before:close"), r.View.prototype.close.apply(this, arguments), this.triggerMethod("item:closed")
	}}), r.CollectionView = r.View.extend({constructor: function(e) {
		this.initChildViewStorage(), r.View.prototype.constructor.apply(this, arguments), this.initialEvents(), this.onShowCallbacks = new r.Callbacks, e && e.itemViewOptions && (this.itemViewOptions = e.itemViewOptions)
	}, initialEvents: function() {
		this.collection && (this.bindTo(this.collection, "add", this.addChildView, this), this.bindTo(this.collection, "remove", this.removeItemView, this), this.bindTo(this.collection, "reset", this.render, this))
	}, addChildView: function(e, t, n) {
		this.closeEmptyView();
		var r = this.getItemView(e), i;
		return n && n.index ? i = n.index : i = 0, this.addItemView(e, r, i)
	}, onShowCalled: function() {
		this.onShowCallbacks.run()
	}, triggerBeforeRender: function() {
		this.triggerMethod("before:render", this), this.triggerMethod("collection:before:render", this)
	}, triggerRendered: function() {
		this.triggerMethod("render", this), this.triggerMethod("collection:rendered", this)
	}, render: function() {
		return this.isClosed = !1, this.triggerBeforeRender(), this.closeEmptyView(), this.closeChildren(), this.collection && this.collection.length > 0 ? this.showCollection() : this.showEmptyView(), this.triggerRendered(), this
	}, showCollection: function() {
		var e = this, t;
		this.collection.each(function(n, r) {
			t = e.getItemView(n), e.addItemView(n, t, r)
		})
	}, showEmptyView: function() {
		var t = r.getOption(this, "emptyView");
		if (t && !this._showingEmptyView) {
			this._showingEmptyView = !0;
			var n = new e.Model;
			this.addItemView(n, t, 0)
		}
	}, closeEmptyView: function() {
		this._showingEmptyView && (this.closeChildren(), delete this._showingEmptyView)
	}, getItemView: function(e) {
		var t = r.getOption(this, "itemView");
		if (!t) {
			var n = new Error("An `itemView` must be specified");
			throw n.name = "NoItemViewError", n
		}
		return t
	}, addItemView: function(e, n, r) {
		var s = this, o;
		t.isFunction(this.itemViewOptions) ? o = this.itemViewOptions(e) : o = this.itemViewOptions;
		var u = this.buildItemView(e, n, o);
		this.children.add(u), this.triggerMethod("item:added", u);
		var a = this.bindTo(u, "all", function() {
			var e = i.call(arguments);
			e[0] = "itemview:" + e[0], e.splice(1, 0, u), s.triggerMethod.apply(s, e)
		});
		this.childBindings = this.childBindings || {}, this.childBindings[u.cid] = a;
		var f = this.renderItemView(u, r);
		return u.onShow && this.onShowCallbacks.add(u.onShow, u), f
	}, renderItemView: function(e, t) {
		e.render(), this.appendHtml(this, e, t)
	}, buildItemView: function(e, n, r) {
		var i = t.extend({model: e}, r), s = new n(i);
		return s
	}, removeItemView: function(e) {
		var t = this.children.findByModel(e);
		if (t) {
			var n = this.childBindings[t.cid];
			n && (this.unbindFrom(n), delete this.childBindings[t.cid]), t.close && t.close(), this.children.remove(t)
		}
		(!this.collection || this.collection.length === 0) && this.showEmptyView(), this.triggerMethod("item:removed", t)
	}, appendHtml: function(e, t, n) {
		e.$el.append(t.el)
	}, initChildViewStorage: function() {
		this.children = new e.ChildViewContainer
	}, close: function() {
		if (this.isClosed)return;
		this.triggerMethod("collection:before:close"), this.closeChildren(), this.triggerMethod("collection:closed"), r.View.prototype.close.apply(this, arguments)
	}, closeChildren: function() {
		var e = this;
		this.children.apply("close"), this.initChildViewStorage()
	}}), r.CompositeView = r.CollectionView.extend({constructor: function(e) {
		r.CollectionView.apply(this, arguments), this.itemView = this.getItemView()
	}, initialEvents: function() {
		this.collection && (this.bindTo(this.collection, "add", this.addChildView, this), this.bindTo(this.collection, "remove", this.removeItemView, this), this.bindTo(this.collection, "reset", this.renderCollection, this))
	}, getItemView: function(e) {
		var t = r.getOption(this, "itemView") || this.constructor;
		if (!t) {
			var n = new Error("An `itemView` must be specified");
			throw n.name = "NoItemViewError", n
		}
		return t
	}, serializeData: function() {
		var e = {};
		return this.model && (e = this.model.toJSON()), e
	}, render: function() {
		this.isClosed = !1, this.resetItemViewContainer();
		var e = this.renderModel();
		return this.$el.html(e), this.bindUIElements(), this.triggerMethod("composite:model:rendered"), this.renderCollection(), this.triggerMethod("composite:rendered"), this
	}, renderCollection: function() {
		r.CollectionView.prototype.render.apply(this, arguments), this.triggerMethod("composite:collection:rendered")
	}, renderModel: function() {
		var e = {};
		e = this.serializeData(), e = this.mixinTemplateHelpers(e);
		var t = this.getTemplate();
		return r.Renderer.render(t, e)
	}, appendHtml: function(e, t) {
		var n = this.getItemViewContainer(e);
		n.append(t.el)
	}, getItemViewContainer: function(e) {
		if ("$itemViewContainer"in e)return e.$itemViewContainer;
		var n;
		if (e.itemViewContainer) {
			var r = t.result(e, "itemViewContainer");
			n = e.$(r);
			if (n.length <= 0) {
				var i = new Error("The specified `itemViewContainer` was not found: " + e.itemViewContainer);
				throw i.name = "ItemViewContainerMissingError", i
			}
		} else n = e.$el;
		return e.$itemViewContainer = n, n
	}, resetItemViewContainer: function() {
		this.$itemViewContainer && delete this.$itemViewContainer
	}}), r.Layout = r.ItemView.extend({regionType: r.Region, constructor: function() {
		this._firstRender = !0, this.initializeRegions(), e.Marionette.ItemView.apply(this, arguments)
	}, render: function() {
		this._firstRender ? this._firstRender = !1 : (this.closeRegions(), this.reInitializeRegions());
		var e = r.ItemView.prototype.render.apply(this, arguments);
		return e
	}, close: function() {
		if (this.isClosed)return;
		this.closeRegions(), this.destroyRegions(), e.Marionette.ItemView.prototype.close.call(this, arguments)
	}, initializeRegions: function() {
		this.regionManagers || (this.regionManagers = {});
		var e = this, n = this.regions || {};
		t.each(n, function(t, n) {
			var i = r.Region.buildRegion(t, e.regionType);
			i.getEl = function(t) {
				return e.$(t)
			}, e.regionManagers[n] = i, e[n] = i
		})
	}, reInitializeRegions: function() {
		this.regionManagers && t.size(this.regionManagers) === 0 ? this.initializeRegions() : t.each(this.regionManagers, function(e) {
			e.reset()
		})
	}, closeRegions: function() {
		var e = this;
		t.each(this.regionManagers, function(e, t) {
			e.close()
		})
	}, destroyRegions: function() {
		var e = this;
		t.each(this.regionManagers, function(t, n) {
			delete e[n]
		}), this.regionManagers = {}
	}}), r.AppRouter = e.Router.extend({constructor: function(t) {
		e.Router.prototype.constructor.apply(this, arguments), this.options = t;
		if (this.appRoutes) {
			var n = r.getOption(this, "controller");
			this.processAppRoutes(n, this.appRoutes)
		}
	}, processAppRoutes: function(e, n) {
		var r, i, s, o, u, a = [], f = this;
		for (s in n)n.hasOwnProperty(s) && a.unshift([s, n[s]]);
		o = a.length;
		for (u = 0; u < o; u++) {
			s = a[u][0], i = a[u][1], r = e[i];
			if (!r) {
				var l = "Method '" + i + "' was not found on the controller", c = new Error(l);
				throw c.name = "NoMethodError", c
			}
			r = t.bind(r, e), f.route(s, i, r)
		}
	}}), r.Application = function(n) {
		this.initCallbacks = new r.Callbacks, this.vent = new r.EventAggregator, this.commands = new e.Wreqr.Commands, this.reqres = new e.Wreqr.RequestResponse, this.submodules = {}, t.extend(this, n), r.addEventBinder(this), this.triggerMethod = r.triggerMethod
	}, t.extend(r.Application.prototype, e.Events, {execute: function() {
		this.commands.execute.apply(this.commands, arguments)
	}, request: function() {
		return this.reqres.request.apply(this.reqres, arguments)
	}, addInitializer: function(e) {
		this.initCallbacks.add(e)
	}, start: function(e) {
		this.triggerMethod("initialize:before", e), this.initCallbacks.run(e, this), this.triggerMethod("initialize:after", e), this.triggerMethod("start", e)
	}, addRegions: function(e) {
		var n = this;
		t.each(e, function(e, t) {
			var i = r.Region.buildRegion(e, r.Region);
			n[t] = i
		})
	}, removeRegion: function(e) {
		this[e].close(), delete this[e]
	}, module: function(e, t) {
		var n = i.call(arguments);
		return n.unshift(this), r.Module.create.apply(r.Module, n)
	}}), r.Application.extend = r.extend, r.Module = function(e, t) {
		this.moduleName = e, this.submodules = {}, this._setupInitializersAndFinalizers(), this.config = {}, this.config.app = t, r.addEventBinder(this)
	}, t.extend(r.Module.prototype, e.Events, {addInitializer: function(e) {
		this._initializerCallbacks.add(e)
	}, addFinalizer: function(e) {
		this._finalizerCallbacks.add(e)
	}, start: function(e) {
		if (this._isInitialized)return;
		t.each(this.submodules, function(t) {
			var n = !0;
			t.config && t.config.options && (n = t.config.options.startWithParent), n && t.start(e)
		}), this._initializerCallbacks.run(e, this), this._isInitialized = !0
	}, stop: function() {
		if (!this._isInitialized)return;
		this._isInitialized = !1, t.each(this.submodules, function(e) {
			e.stop()
		}), this._finalizerCallbacks.run(), this._initializerCallbacks.reset(), this._finalizerCallbacks.reset()
	}, addDefinition: function(e, t) {
		this._runModuleDefinition(e, t)
	}, _runModuleDefinition: function(i, s) {
		if (!i)return;
		var o = t.flatten([this, this.config.app, e, r, n, t, s]);
		i.apply(this, o)
	}, _setupInitializersAndFinalizers: function() {
		this._initializerCallbacks = new r.Callbacks, this._finalizerCallbacks = new r.Callbacks
	}}), t.extend(r.Module, {create: function(e, n, r) {
		var s = this, o = e;
		n = n.split(".");
		var u = i.apply(arguments);
		u.splice(0, 3);
		var a = n.length;
		return t.each(n, function(t, n) {
			var i = n === a - 1, f = n === 0, l = s._getModuleDefinition(o, t, e);
			i && (l.config.options = s._getModuleOptions(l, o, r), l.config.options.hasDefinition && l.addDefinition(l.config.options.definition, u)), f && i && s._configureStartWithApp(e, l), o = l
		}), o
	}, _configureStartWithApp: function(e, t) {
		if (t.config.startWithAppIsConfigured)return;
		e.addInitializer(function(e) {
			t.config.options.startWithParent && t.start(e)
		}), t.config.startWithAppIsConfigured = !0
	}, _getModuleDefinition: function(e, t, n) {
		var i = e[t];
		return i || (i = new r.Module(t, n), e[t] = i, e.submodules[t] = i), i
	}, _getModuleOptions: function(e, n, r) {
		var i = !0;
		e.config.options && !e.config.options.startWithParent && (i = !1);
		var s = {startWithParent: i, hasDefinition: !!r};
		return s.hasDefinition ? (t.isFunction(r) ? s.definition = r : (s.hasDefinition = !!r.define, s.definition = r.define, r.hasOwnProperty("startWithParent") && (s.startWithParent = r.startWithParent)), s) : s
	}}), r
}(Backbone, _, $ || window.jQuery || window.Zepto || window.ender);