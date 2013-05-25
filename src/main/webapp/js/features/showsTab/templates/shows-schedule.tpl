<span class='shows-schedule-loading'>loading ...</span>
{{#each schedule.schedules}}
    {{#isToday date}}
        <div class='shows-schedule-day'>{{dateFormat this format="DD/MM (dddd)" default='never'}} - <span class='shows-schedule-today'>Today</span></div>
    {{else}}
        <div class='shows-schedule-day'>{{dateFormat this format="DD/MM (dddd)" default='never'}}</div>
    {{/isToday}}
    {{#each shows}}
            <div class='shows-schedule-show-item'><span class='shows-schedule-show-sequence'>{{sequence}}</span>&nbsp;&nbsp;<span class='shows-schedule-show-name'>{{showName}}</span></div>
    {{/each}}
{{/each}}