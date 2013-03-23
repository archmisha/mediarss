{{#each schedule.schedules}}
<div class='shows-schedule-day'>{{dateFormat date format="DD/MM (dddd)" default='never'}}</div>
{{#each shows}}
        <div class='shows-schedule-show-item'><span class='shows-schedule-show-sequence'>{{sequence}}</span>&nbsp;&nbsp;<span class='shows-schedule-show-name'>{{showName}}</span></div>
{{/each}}
{{/each}}