Welcome <span class='masthead-username'>{{username}}</span>
<span class='masthead-separator'>|</span>
<span class='masthead-support'>Support</span>
<span class='masthead-separator'>|</span>
<span class='masthead-updated-on'><span class='masthead-updated-on-prefix'>Version updated on </span>{{dateFormat updated-on format="DD/MM/YYYY"}}</span>
<span class='masthead-separator'>|</span>
<a href="#logout" class='masthead-logout'>Logout</a>

<a class='masthead-support-box' href="#masthead-support-box-content"></a>
<div class='masthead-support-box-content' id='masthead-support-box-content'>
    <div class='masthead-support-title'>Submit a Feature request or a Defect</div>
    <div class='masthead-support-subtitle'>Help us improve MediaRSS !</div>
    <div class='masthead-support-type-container'>
        Type:&nbsp;
        <input type="radio" name="type" value="Feature" checked/> Feature
        <input type="radio" name="type" value="Defect"/> Defect
    </div>
    <textarea class='masthead-support-content'></textarea>

    <div class='masthead-support-button-bar'>
        <button class='btn btn-small masthead-support-cancel-button'>Cancel</button>
        <button class='btn btn-small masthead-support-submit-button'>Submit</button>
    </div>
</div>