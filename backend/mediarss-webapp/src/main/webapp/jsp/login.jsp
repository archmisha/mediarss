<%@ include file="prefix.jsp" %>
<script src="../js/lib/jquery.fancybox-2.1.4.min.js"></script>
<script src="../js/login.js"></script>

<div class='login-form-container'>
    <form id="login-form" action="#" class='login-form' target="temp">
        <div class='login-form-row'>
            <label for="login-username" id="login-username-label" class='login-form-label'>Email</label>
            <input type="text" tabindex="0" name="username" id="login-username" class='login-form-input'/>
        </div>
        <div class='login-form-row'>
            <label for="login-password" id="login-password-label" class='login-form-label'>Password</label>
            <input type="password" tabindex="0" name="password" id="login-password" class='login-form-input'/>
        </div>
        <div class='login-form-row'>
            <input type="checkbox" tabindex="0" name="rememberMe" id="login-remember-me"
                   class='login-remember-me-input'/>
            <label for="login-remember-me" id="login-remember-me-label" class=''>Remember me</label>
        </div>
        <div class='login-buttons-container'>
            <input id="login-submit" type="submit" class='btn login-btn' value='Login'/>
            <a href="/create-account" class='login-register-link'>Create an account</a>
        </div>
        <div class='login-forgot-password'>Forgot your password?</div>
    </form>
    <iframe src="../ablankpage.html" id="temp" name="temp" style="display:none"></iframe>
</div>
<div class='login-status'></div>
<a class='login-forgot-password-box' href="#login-forgot-password-box-content"></a>

<div class='login-forgot-password-box-content' id='login-forgot-password-box-content'>
    <div class='login-forgot-password-box-title'>Password recovery</div>
    <div class='login-forgot-password-email-label'>Please insert your email address:</div>
    <input class='login-forgot-password-email-input' type='text'/></br>
    <input type='submit' class='btn login-forgot-password-btn' value='Submit'/>
</div>

<%@ include file="suffix.jsp" %>