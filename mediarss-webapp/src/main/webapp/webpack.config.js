const ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
    entry: {
        home: "./app/App.js"
    },
    output: {
        filename: "public/[name].js"
    },
    plugins: [
        new ExtractTextPlugin('public/[name].css', {
            allChunks: true
        })
    ],
    module: {
        loaders: [
            {
                test: /\.jsx?$/,
                //loaders: ['react-hot', 'jsx', 'babel'],
                loader: 'babel',
                exclude: /(node_modules|bower_components)/,
                query: {
                    presets: ['react', 'es2015']
                }
            },
            {
                test: /\.css$/,
                loader: ExtractTextPlugin.extract("style-loader", "css-loader")
            },
            {
                test: /\.less$/,
                loader: ExtractTextPlugin.extract("style-loader", "css-loader!less-loader")
            }
        ]
    }
}