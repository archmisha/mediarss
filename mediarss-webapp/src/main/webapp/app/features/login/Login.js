var React = require('react');
require('./login.less');

var Login = React.createClass({
    render: function(){
        return (
            <div>
                <div className='login-form-container'>
                    <form id="login-form" action="#" className='login-form' target="temp">
                        <div className='login-form-row'>
                            <label htmlFor="login-username" id="login-username-label" className='login-form-label'>Email</label>
                            <input type="text" tabIndex="0" name="username" id="login-username" className='login-form-input'/>
                        </div>
                        <div className='login-form-row'>
                            <label htmlFor="login-password" id="login-password-label" className='login-form-label'>Password</label>
                            <input type="password" tabIndex="0" name="password" id="login-password" className='login-form-input'/>
                        </div>
                        <div className='login-form-row'>
                            <input type="checkbox" tabIndex="0" name="rememberMe" id="login-remember-me"
                                   className='login-remember-me-input'/>
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
                <div className='login-status'></div>
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

module.exports = Login;
