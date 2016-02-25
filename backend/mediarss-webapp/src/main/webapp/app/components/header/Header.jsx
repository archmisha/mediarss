import React from 'react';
import './header.less';

export default () => (
    <div className='header-container'>
        <img src='../images/rss_icon.png' className='header-image'/>

        <div className='header-title-container'>
            <div className='header-title'>Personalized Media RSS</div>
            <div className='header-description'>
                Downloading TV Shows and Movies was never this easy !<br/>
                Downloading new episodes and movies completely automatically<br/>
                Search for older episodes, support subtitles, HD quality
            </div>
        </div>
    </div>
);
