import React from 'react';
import ReactDOM from 'react-dom';
import { Provider, connect } from 'react-redux';
import { Router, browserHistory } from 'react-router';
import routes from './config/routes';
import { createStore, combineReducers, applyMiddleware, compose } from 'redux'
import thunk from 'redux-thunk'
import createLogger from 'redux-logger';
import {reducer as formReducer} from 'redux-form';
import {loginReducer} from './features/login/Login.jsx';

import '../style/3rd-party/bootstrap.lite.less';

global.$ = global.jQuery = require('jquery');

const reducers = {
    login: loginReducer,
    form: formReducer
};
const reducer = combineReducers(reducers);
ReactDOM.render(
    <Provider store={compose(
                        applyMiddleware(thunk, createLogger()),
                        window.devToolsExtension ? window.devToolsExtension() : f => f
                        )(createStore)(reducer)}>
        <Router history={browserHistory}>{routes}</Router>
    </Provider>,
    document.getElementById('app')
);