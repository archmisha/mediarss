import React, {Component, PropTypes} from 'react';
import ReactDom from 'react-dom';
import { browserHistory } from 'react-router'
import { reduxForm } from 'redux-form';
import { connect } from 'react-redux';
import './login.less';
import Header from '../../components/header/Header.jsx';

const LOGIN_ACTION = 'LOGIN';

export const loginReducer = (state = {}, action) => {
    switch (action.type) {
        case [LOGIN_ACTION]:
            browserHistory.push("home");
            return state;
        default:
            return state;
    }
};

const validate = values => {
    const errors = {};
    if (!values.username || values.username.trim().length == 0) {
        errors.username = 'Required';
    }
    if (!values.password || values.password.trim().length == 0) {
        errors.password = 'Required';
    }
    return errors;
};

class LoginForm extends Component {
    static propTypes = {
        fields: PropTypes.object.isRequired,
        error: PropTypes.string,
        handleSubmit: PropTypes.func.isRequired
    };
//var passwordRecoveryEmailField = $('.login-forgot-password-email-input');
//var passwordRecoverySubmitButton = $('.login-forgot-password-btn');
    componentDidMount() {
        ReactDom.findDOMNode(this._usernameInput).focus();
    }

    render() {
        const {fields: {username, password, rememberMe}, error, handleSubmit} = this.props;

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

        return (
            <div>
                <div className='login-form-container'>
                    <form id="login-form" action="#" className='login-form' target="temp" onSubmit={handleSubmit}>
                        <div className='login-form-row'>
                            <label htmlFor="login-username" id="login-username-label"
                                   className='login-form-label'>Email</label>
                            <input type="text" tabIndex="0" name="username" id="login-username"
                                   className='login-form-input' {...username} ref={(c) => this._usernameInput = c}/>
                            {username.touched && username.error && <div className='login-field-error'>{username.error}</div>}
                        </div>
                        <div className='login-form-row'>
                            <label htmlFor="login-password" id="login-password-label" className='login-form-label'>Password</label>
                            <input type="password" tabIndex="0" name="password" id="login-password"
                                   className='login-form-input' {...password}/>
                            {password.touched && password.error && <div className='login-field-error'>{password.error}</div>}
                        </div>
                        <div className='login-form-row'>
                            <input type="checkbox" tabIndex="0" name="rememberMe" id="login-remember-me"
                                   className='login-remember-me-input' {...rememberMe}/>
                            <label htmlFor="login-remember-me" id="login-remember-me-label" className=''>Remember
                                me</label>
                        </div>
                        <div className='login-buttons-container'>
                            <input id="login-submit" type="submit" className='btn login-btn' value='Login'/>
                            <a href="/create-account" className='login-register-link'>Create an account</a>
                        </div>
                        <div className='login-forgot-password'>Forgot your password?</div>
                    </form>
                    <iframe src="../ablankpage.html" id="temp" name="temp" style={{display:'none'}}></iframe>
                </div>
                <div className='login-status'>{this.props.error}</div>
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
}

const mapStateToProps = (state) => {
    return {
        error: state.login.error
    }
};

var LoginReduxForm = reduxForm({
    form: 'loginForm',
    fields: ['username', 'password', 'rememberMe'],
    validate
})(LoginForm);

var Login = ({username, password, rememberMe, dispatch}) => {
    function handleLogin() {
        return new Promise((resolve, reject) => {
            $.ajax({
                type: 'POST',
                url: '/rest/user/login',
                data: $.param({
                    username: username,
                    password: password,
                    rememberMe: rememberMe
                })
            }).done(function (res) {
                if (res.success === undefined) {
                    dispatch({type: LOGIN_ACTION});
                    resolve();
                } else {
                    reject({_error: res.message});
                }
            }).fail(function (jqXHR, textStatus) {
                reject({_error: textStatus});
            });
        });
    }

    return (
        <div className='login-header'>
            <Header/>
            <LoginReduxForm username={username} password={password} rememberMe={rememberMe}
                            onSubmit={handleLogin}/>
        </div>
    );
};
export default connect(mapStateToProps)(Login);
