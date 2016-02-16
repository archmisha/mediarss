import React from 'react';
import ReactDOM from 'react-dom';
var Router = require('react-router').Router; // todo: how to remove the required
import routes from './config/routes';

import '../style/3rd-party/bootstrap.lite.less';

global.$ = global.jQuery = require('jquery');

ReactDOM.render(
    <Router>{routes}</Router>,
    document.getElementById('app')
);