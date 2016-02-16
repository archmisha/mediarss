import React from 'react';
import ReactDom from 'react-dom';
import './login.less';
import Header from '../../components/header/Header.jsx';

var Login = React.createClass({
    //var status = $('.login-status');
//var usernameInput = $('input[name=username]');
//var passwordInput = $('input[name=password]');
//var rememberMeInput = $('input[name=rememberMe]');
//var passwordRecoveryEmailField = $('.login-forgot-password-email-input');
//var passwordRecoverySubmitButton = $('.login-forgot-password-btn');

    getInitialState: function() {
        return {username: '', password: ''};
    },

    showStatusMessage: function(msg) {
        this._status.text(msg);
        this._status.fadeIn('slow');
    },

    hideStatusMessage: function() {
        this._status.fadeOut('slow');
        this._status.val('');
    },

    login: function(e) {
        e.preventDefault();
        this.hideStatusMessage();
        var username = this.state.username.trim();
        var password = this.state.password.trim();
        var rememberMe = this._rememberMeInput.is(':checked');

        if (!username || username.length == 0 || !password || password.length == 0) {
            this.showStatusMessage('Invalid email or password');
            return false;
        }

        $.post("rest/user/login", {
            username: username,
            password: password,
            rememberMe: rememberMe
        }, function(res) {
            if (res.success === undefined) {
                var str = window.location.href;
                window.location.href = str.substring(0, str.lastIndexOf('/') + 1) + 'main';
            } else {
                this.showStatusMessage(res.message);
            }
        });

        return false;
    },

    //onForgotPasswordButtonClick: function() {
    //    var email = passwordRecoveryEmailField.val();
    //
    //    if (!email || email.trim().length == 0) {
    //        return;
    //    }
    //
    //    $.fancybox.close();
    //
    //    $.post("rest/user/forgot-password", {
    //        email: email
    //    }, function(res) {
    //        this.showStatusMessage(res.message);
    //    }, false);
    //},

    //onForgotPasswordInputKeyPress: function(event) {
    //    var ENTER_KEY = 13;
    //    if (event.which === ENTER_KEY) {
    //        this.onForgotPasswordButtonClick();
    //    }
    //},

//[usernameInput, passwordInput].forEach(function(inputEl) {
//    inputEl.keypress(function(event) {
//        var ENTER_KEY = 13;
//        if (event.which === ENTER_KEY) {
//            login();
//        }
//    });
//});

//$('.login-form').submit(function() {
//    return login();
//});
//
//$('.login-forgot-password').click(function() {
//    hideStatusMessage();
//    passwordRecoveryDialog.click();
//});

//var passwordRecoveryDialog = $('.login-forgot-password-box').fancybox({
//    hideOnContentClick: true,
//    afterShow: function() {
//        passwordRecoveryEmailField.on('keypress', function(event) {
//            onForgotPasswordInputKeyPress(event);
//        });
//        passwordRecoverySubmitButton.on('click', function() {
//            onForgotPasswordButtonClick();
//        });
//        setTimeout(function() {
//            passwordRecoveryEmailField.focus();
//        }, 50);
//    },
//    afterClose: function() {
//        passwordRecoveryEmailField.off('keypress');
//        passwordRecoverySubmitButton.off('click');
//        passwordRecoveryEmailField.val('');
//    }
//});

    componentDidMount: function() {
        ReactDom.findDOMNode(this._usernameInput).focus();
    },

    onUsernameChange: function(e) {
        this.setState({username: e.target.value});
    },

    onPasswordChange: function(e) {
        this.setState({password: e.target.value});
    },

    render: function(){
        return (
            <div>
                <Header/>
                <div className='login-form-container'>
                    <form id="login-form" action="#" className='login-form' target="temp" onSubmit={this.login}>
                        <div className='login-form-row'>
                            <label htmlFor="login-username" id="login-username-label" className='login-form-label'>Email</label>
                            <input type="text" tabIndex="0" name="username" id="login-username" className='login-form-input'
                                   onChange={this.onUsernameChange} ref={(c) => this._usernameInput = c}/>
                        </div>
                        <div className='login-form-row'>
                            <label htmlFor="login-password" id="login-password-label" className='login-form-label'>Password</label>
                            <input type="password" tabIndex="0" name="password" id="login-password" className='login-form-input'
                                   onChange={this.onPasswordChange}/>
                        </div>
                        <div className='login-form-row'>
                            <input type="checkbox" tabIndex="0" name="rememberMe" id="login-remember-me"
                                   className='login-remember-me-input' ref={(c) => this._rememberMeInput = c}/>
                            <label htmlFor="login-remember-me" id="login-remember-me-label" className=''>Remember me</label>
                        </div>
                        <div className='login-buttons-container'>
                            <input id="login-submit" type="submit" className='btn login-btn' value='Login'/>
                            <a href="/create-account" className='login-register-link'>Create an account</a>
                        </div>
                        <div className='login-forgot-password'>Forgot your password?</div>
                    </form>
                    <iframe src="../ablankpage.html" id="temp" name="temp" style={{display:'none'}}></iframe>
                </div>
                <div className='login-status' ref={(c) => this._status = c}></div>
                <a className='login-forgot-password-box' href="#login-forgot-password-box-content"></a>

                <div className='login-forgot-password-box-content' id='login-forgot-password-box-content'>
                    <div className='login-forgot-password-box-title'>Password recovery</div>
                    <div className='login-forgot-password-email-label'>Please insert your email address:</div>
                    <input className='login-forgot-password-email-input' type='text'/><br/>
                    <input type='submit' className='btn login-forgot-password-btn' value='Submit'/>
                </div>
            </div>
        )
    }
});

export default Login;
