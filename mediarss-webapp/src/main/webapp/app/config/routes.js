import React from 'react';
import { Route, IndexRoute } from 'react-router';
import Login from '../features/login/Login.jsx';
import Home from '../features/home/Home.jsx';

module.exports = (
    <Route path="/public">
        <IndexRoute component={Login} />
        <Route name="home" path="/home" component={Home} />
    </Route>
);
