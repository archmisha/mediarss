{{#each schedule.schedules}}
    {{#isToday date}}
        <div class='shows-schedule-day-today'>{{dateFormat this format="DD/MM (dddd)" default='never'}} - Today</div>
    {{else}}
        <div class='shows-schedule-day'>{{dateFormat this format="DD/MM (dddd)" default='never'}}</div>
    {{/isToday}}
    {{#each shows}}
            <div class='shows-schedule-show-item'><span class='shows-schedule-show-sequence'>{{sequence}}</span>&nbsp;&nbsp;<span class='shows-schedule-show-name'>{{showName}}</span></div>
    {{/each}}
{{/each}}