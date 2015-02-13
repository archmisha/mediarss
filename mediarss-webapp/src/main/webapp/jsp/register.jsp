<%@ include file="prefix.jsp" %>
<script src="../js/register.js"></script>

<div class='register-container'>
    <div class="register-header-subtitle">
        Create an account
    </div>

    <div class='register-form-row'>
        <label for="register-username" id="register-firstname-label" class='register-form-label'>First Name</label>
        <input type="text" tabindex="0" name="firstName" id="register-firstname" class='register-form-input'/>
    </div>
    <div class='register-form-row'>
        <label for="register-username" id="register-lastname-label" class='register-form-label'>Last Name</label>
        <input type="text" tabindex="0" name="lastName" id="register-lastname" class='register-form-input'/>
    </div>
    <div class='register-form-row'>
        <label for="register-username" id="register-username-label" class='register-form-label'>Email</label>
        <input type="text" tabindex="0" name="username" id="register-username" class='register-form-input'/>
    </div>
    <div class='register-form-row'>
        <label for="register-password" id="register-password-label" class='register-form-label'>Password</label>
        <input type="password" tabindex="0" name="password" id="register-password" class='register-form-input'/>
    </div>
    <button class='btn register-btn'>Proceed</button>
    <div class='register-status'></div>
</div>

<%@ include file="suffix.jsp" %>